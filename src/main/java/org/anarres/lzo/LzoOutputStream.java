/*
 * This file is part of Hadoop-Gpl-Compression.
 *
 * Hadoop-Gpl-Compression is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Hadoop-Gpl-Compression is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hadoop-Gpl-Compression.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.anarres.lzo;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LzoOutputStream extends OutputStream {

  private static final Log LOG = LogFactory.getLog(
      LzoOutputStream.class.getName());
  protected final OutputStream out;
  private final LzoCompressor compressor; // Replace with BlockCompressor.
  private final byte[] inputBuffer;
  private int inputBufferLen;
  private byte[] inputHoldoverBuffer;
  private int inputHoldoverBufferPos;
  private int inputHoldoverBufferLen;
  private final byte[] outputBuffer;
  // Also, end, since we base outputBuffer at 0.
  private final lzo_uintp outputBufferLen = new lzo_uintp();
  private final OutputStream indexOut;
  protected long offset;

  /**
   * Creates a new compressor using the specified {@link LzoCompressor}.
   *
   * @param out the OutputStream to write to
   * @param compressor the compressor to use
   * @param inputBufferSize size of the input buffer to be used.
   * @param indexOut a stream that will get the index of the block start 
   *    offset
   */
  public LzoOutputStream(OutputStream out, LzoCompressor compressor, 
                         int inputBufferSize, OutputStream indexOut) {
    this.out = out;
    this.compressor = compressor;
    this.inputBuffer = new byte[inputBufferSize];
    this.outputBuffer = 
      new byte[inputBufferSize + 
               compressor.getCompressionOverhead(inputBufferSize)];
    this.indexOut = indexOut;
    reset();
  }

  /**
   * Creates a new compressor with the default lzo1x_1 compression.
   */
  public LzoOutputStream(OutputStream out) {
    this(out, LzoLibrary.getInstance().newCompressor(null, null), 256 * 1024,
	 null);
  }

  public LzoCompressor getCompressor() {
    return compressor;
  }

  public LzoAlgorithm getAlgorithm() {
    return getCompressor().getAlgorithm();
  }

  public LzoConstraint[] getConstraints() {
    return getCompressor().getConstraints();
  }

  /**
   * Reset the current state of the stream
   */
  protected void reset() {
    inputBufferLen = 0;
    inputHoldoverBuffer = null;
    inputHoldoverBufferPos = -1;
    inputHoldoverBufferLen = -1;
    outputBufferLen.value = 0;
    offset = 0;
  }

  private void logState(String when) {
    LOG.info("\n");
    LOG.info(
        when + " Input buffer length=" + inputBufferLen + "/" +
        inputBuffer.length);
    if (inputHoldoverBuffer == null) {
      LOG.info(when + " Input holdover = null");
    } else {
      LOG.info(when + " Input holdover pos=" + inputHoldoverBufferPos + 
               "; length=" + inputHoldoverBufferLen);
    }
    LOG.info(when + " Output buffer length=" + outputBufferLen + "/" +
             outputBuffer.length);
    testInvariants();
  }

  private boolean testInvariants() {
    if (inputHoldoverBuffer != null) {
      if (inputBufferLen != 0 && inputBufferLen != inputBuffer.length) {
        throw new IllegalStateException(
            "Funny input buffer length " + inputBufferLen + 
            " with array size " + inputBuffer.length + " and holdover.");
      }
      if (inputHoldoverBufferPos < 0) {
        throw new IllegalStateException(
            "Using holdover buffer, but invalid holdover position " +
                inputHoldoverBufferPos);
      }
      if (inputHoldoverBufferLen < 0) {
        throw new IllegalStateException(
            "Using holdover buffer, but invalid holdover length " +
                inputHoldoverBufferLen);
      }
    } else {
      if (inputHoldoverBufferPos != -1) {
        throw new IllegalStateException(
            "No holdover buffer, but valid holdover position " +
                inputHoldoverBufferPos);
      }
      if (inputHoldoverBufferLen != -1) {
        throw new IllegalStateException(
            "No holdover buffer, but valid holdover length " +
                inputHoldoverBufferLen);
      }
    }

    if (outputBufferLen.value < 0) {
      throw new IllegalStateException(
          "Output buffer overrun length=" + outputBufferLen);
    }

    return true;
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[] { (byte) b});
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    // logState("Before setInput");
    if (b == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || off > b.length - len) {
      throw new ArrayIndexOutOfBoundsException(
          "Illegal range in buffer: Buffer length=" + b.length + ", offset=" +
          off + ", length=" + len);
    }
    if (inputHoldoverBuffer != null) {
      throw new IllegalStateException(
          "Cannot accept input while holdover is present.");
    }

    inputHoldoverBuffer = b;
    inputHoldoverBufferPos = off;
    inputHoldoverBufferLen = len;

    // keep iterating until we have compressed or copied all of the input
    // that was passed to us.
    while (inputHoldoverBuffer != null) {
      compact();
    }
    assert testInvariants();
    // logState("After setInput");
  }

  @Override
  public void flush() throws IOException {
    if (inputBufferLen > 0) {
      compress(inputBuffer, 0, inputBufferLen);
      inputBufferLen = 0;
      assert testInvariants();
    }
    flushIndex();
  }

  @Override
  public void close() throws IOException {
    flush();
    out.close();
    if (indexOut != null) {
      indexOut.close();
    }
  }

  /*
   * Take the data from inputHoldoverBuffer that was just given to us
   * and either compress it or add it to the inputBuffer. If the inputBuffer
   * is full, we compress it and return. 
   */
  private void compact() throws IOException {
    int remaining = inputBuffer.length - inputBufferLen;

    // if there isn't any saved input
    if (inputBufferLen == 0) {

      // if we have enough to fill a buffer, do so directly without a copy
      if (inputHoldoverBufferLen >= remaining) {
        compress(inputHoldoverBuffer, inputHoldoverBufferPos, remaining);
        inputHoldoverBufferPos += remaining;
        inputHoldoverBufferLen -= remaining;
        // are we done?
        if (inputHoldoverBufferLen == 0) {
          inputHoldoverBuffer = null;
          inputHoldoverBufferPos = -1;
          inputHoldoverBufferLen = -1;
        }
      } else {
        System.arraycopy(inputHoldoverBuffer, inputHoldoverBufferPos
            , 
            inputBuffer, 0, inputHoldoverBufferLen);
        inputBufferLen += inputHoldoverBufferLen;
        inputHoldoverBuffer = null;
        inputHoldoverBufferPos = -1;
        inputHoldoverBufferLen = -1;
      }
    } else {
      int copiedBytes = Math.min(remaining, inputHoldoverBufferLen);

      System.arraycopy(inputHoldoverBuffer, inputHoldoverBufferPos, 
                       inputBuffer, inputBufferLen, copiedBytes);
      inputBufferLen += copiedBytes;
      inputHoldoverBufferPos += copiedBytes;
      inputHoldoverBufferLen -= copiedBytes;
      if (inputBufferLen == inputBuffer.length) {
        compress(inputBuffer, 0, inputBufferLen);
        inputBufferLen = 0;
      } else if (inputHoldoverBufferLen == 0) {
        inputHoldoverBuffer = null;
        inputHoldoverBufferPos = -1;
        inputHoldoverBufferLen = -1;
      }
    }
  }

  private void compress(byte[] buffer, int off, int len) throws IOException {

    outputBufferLen.value = outputBuffer.length;
    try {
      int code = compressor.compress(buffer, off, len, outputBuffer, 0,
                                     outputBufferLen);

      if (code != LzoTransformer.LZO_E_OK) {
        logState("LZO error: " + code);
        throw new IllegalArgumentException(compressor.toErrorString(code));
      }
    } catch (IndexOutOfBoundsException e) {
      logState("IndexOutOfBoundsException: " + e);
      throw new IOException(e);
    }
    writeOffset(offset);
    writeBlock(buffer, off, len, outputBuffer, 0, outputBufferLen.value);
  }

  protected void writeBlock(byte[] inputData, int inputPos, int inputLen, 
                            byte[] outputData, int outputPos, int outputLen
                            ) throws IOException {
    writeInt(inputLen);
    writeInt(outputLen);
    out.write(outputData, outputPos, outputLen);
    offset += outputLen;
  }

  protected void writeInt(int v) throws IOException {
    out.write((v >>> 24) & 0xFF);
    out.write((v >>> 16) & 0xFF);
    out.write((v >>> 8) & 0xFF);
    out.write(v & 0xFF);
    offset += 4;
  }

  private ByteBuffer indexBuffer = ByteBuffer.allocate(8*1024);
  {
    indexBuffer.order(ByteOrder.BIG_ENDIAN);
  }

  protected void writeOffset(long offset) throws IOException {
    if (indexOut != null) {
      if (indexBuffer.remaining() < 8) {
	flushIndex();
      }
      indexBuffer.putLong(offset);
    }
  }

  protected void flushIndex() throws IOException {
    if (indexOut != null) {
      indexOut.write(indexBuffer.array(), 0, indexBuffer.position());
      indexBuffer.rewind();
      indexOut.flush();
    }
  }
}
