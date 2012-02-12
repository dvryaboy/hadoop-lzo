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


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LzoInputStream extends InputStream {

  private static final Log LOG = LogFactory.getLog(
      LzoInputStream.class.getName());
  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  protected final InputStream in;
  private final LzoDecompressor decompressor;
  protected byte[] inputBuffer = EMPTY_BYTE_ARRAY;
  protected byte[] outputBuffer = EMPTY_BYTE_ARRAY;
  protected int outputBufferPos;
  // Also, end, since we base outputBuffer at 0.
  protected final lzo_uintp outputBufferLen = new lzo_uintp();

  public LzoInputStream(InputStream in, LzoDecompressor decompressor) {
    this.in = in;
    this.decompressor = decompressor;
  }

  public void setInputBufferSize(int inputBufferSize) {
    if (inputBufferSize > inputBuffer.length) {
      inputBuffer = new byte[inputBufferSize];
    }
  }

  public void setOutputBufferSize(int outputBufferSize) {
    if (outputBufferSize > outputBuffer.length) {
      outputBuffer = new byte[outputBufferSize];
    }
  }

  @Override
  public int available() throws IOException {
    return outputBufferLen.value - outputBufferPos;
  }

  @Override
  public int read() throws IOException {
    if (!fill()) {
      return -1;
    }
    return outputBuffer[outputBufferPos++];
  }

  public void resetState() throws IOException {
    outputBufferLen.value = 0;
    outputBufferPos = 0;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (!fill()) {
      return -1;
    }
    len = Math.min(len, available());
    System.arraycopy(outputBuffer, outputBufferPos, b, off, len);
    outputBufferPos += len;
    return len;
  }

  protected void logState(String when) {
    LOG.info("\n");
    LOG.info(when + " Input buffer size=" + inputBuffer.length);
    LOG.info(
        when + " Output buffer pos=" + outputBufferPos + "; length=" +
        outputBufferLen + "; size=" + outputBuffer.length);
  }

  private boolean fill() throws IOException {
    while (available() == 0) {
      if (!readBlock()) { // Always consumes 8 bytes, so guaranteed to terminate.
        return false;
      }
    }
    return true;
  }

  protected boolean readBlock() throws IOException {
    // logState("Before readBlock");
    int outputBufferLength = readInt(true);

    if (outputBufferLength == -1) {
      return false;
    }
    setOutputBufferSize(outputBufferLength);
    int inputBufferLength = readInt(false);

    setInputBufferSize(inputBufferLength);
    readBytes(inputBuffer, 0, inputBufferLength);
    decompress(outputBufferLength, inputBufferLength);
    return true;
  }

  protected void decompress(int outputBufferLength, 
                            int inputBufferLength) throws IOException {
    try {
      outputBufferPos = 0;
      outputBufferLen.value = outputBuffer.length;
      int code = decompressor.decompress(inputBuffer, 0, inputBufferLength
          ,
          outputBuffer, 0, outputBufferLen);

      if (code != LzoTransformer.LZO_E_OK) {
        logState("LZO error: " + code);
        throw new IllegalArgumentException(decompressor.toErrorString(code));
      }
      if (outputBufferLen.value != outputBufferLength) {
        logState("Output underrun: ");
        throw new IllegalStateException("Expected " + outputBufferLength + 
                                        " bytes, but got only " +
                                        outputBufferLen);
      }
    } catch (IndexOutOfBoundsException e) {
      logState("IndexOutOfBoundsException: " + e);
      throw new IOException(e);
    }
  }

  protected int readInt(boolean start_of_frame) throws IOException {
    int b1 = in.read();

    if (b1 == -1) {
      if (start_of_frame) {
        return -1;
      } else {
        throw new EOFException("EOF before reading 4-byte integer.");
      }
    }
    int b2 = in.read();
    int b3 = in.read();
    int b4 = in.read();

    if ((b1 | b2 | b3 | b4) < 0) {
      throw new EOFException("EOF while reading 4-byte integer.");
    }
    return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
  }

  protected void readBytes(byte[] buf, int off, int length
                           ) throws IOException {
    while (length > 0) {
      int count = in.read(buf, off, length);

      if (count < 0) {
        throw new EOFException();
      }
      off += count;
      length -= count;
    }
  }
}
