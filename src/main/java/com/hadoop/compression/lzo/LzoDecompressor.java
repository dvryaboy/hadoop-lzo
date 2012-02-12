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
package com.hadoop.compression.lzo;

import java.io.IOException;
import org.anarres.lzo.LzoAlgorithm;
import org.anarres.lzo.LzoConstraint;
import org.anarres.lzo.LzoLibrary;
import org.anarres.lzo.LzoTransformer;
import org.anarres.lzo.lzo_uintp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.compress.Decompressor;
import org.apache.hadoop.io.compress.DoNotPool;

/**
 * A {@link Decompressor} based on the lzo algorithm.
 * http://www.oberhumer.com/opensource/lzo/
 * 
 */
@DoNotPool
public class LzoDecompressor implements Decompressor {

  private static final Log LOG = LogFactory.getLog(LzoDecompressor.class);

  public static enum CompressionStrategy {

    /**
     * lzo1 algorithms.
     */
    LZO1(LzoAlgorithm.LZO1),
      /**
       * lzo1a algorithms.
       */
      LZO1A(LzoAlgorithm.LZO1A),
      /**
       * lzo1b algorithms.
       */
      LZO1B(LzoAlgorithm.LZO1B),
      LZO1B_SAFE(LzoAlgorithm.LZO1B, LzoConstraint.SAFETY),
      /**
       * lzo1c algorithms.
       */
      LZO1C(LzoAlgorithm.LZO1C),
      LZO1C_SAFE(LzoAlgorithm.LZO1C, LzoConstraint.SAFETY),
      LZO1C_ASM(LzoAlgorithm.LZO1C),
      LZO1C_ASM_SAFE(LzoAlgorithm.LZO1C, LzoConstraint.SAFETY),
      /**
       * lzo1f algorithms.
       */
      LZO1F(LzoAlgorithm.LZO1F),
      LZO1F_SAFE(LzoAlgorithm.LZO1F, LzoConstraint.SAFETY),
      LZO1F_ASM_FAST(LzoAlgorithm.LZO1F),
      LZO1F_ASM_FAST_SAFE(LzoAlgorithm.LZO1F, LzoConstraint.SAFETY),
      /**
       * lzo1x algorithms.
       */
      LZO1X(LzoAlgorithm.LZO1X),
      LZO1X_SAFE(LzoAlgorithm.LZO1X, LzoConstraint.SAFETY),
      LZO1X_ASM(LzoAlgorithm.LZO1X),
      LZO1X_ASM_SAFE(LzoAlgorithm.LZO1X, LzoConstraint.SAFETY),
      LZO1X_ASM_FAST(LzoAlgorithm.LZO1X, LzoConstraint.SPEED),
      LZO1X_ASM_FAST_SAFE(LzoAlgorithm.LZO1X, LzoConstraint.SAFETY),
      /**
       * lzo1y algorithms.
       */
      LZO1Y(LzoAlgorithm.LZO1Y),
      LZO1Y_SAFE(LzoAlgorithm.LZO1Y, LzoConstraint.SAFETY),
      LZO1Y_ASM(LzoAlgorithm.LZO1Y),
      LZO1Y_ASM_SAFE(LzoAlgorithm.LZO1Y, LzoConstraint.SAFETY),
      LZO1Y_ASM_FAST(LzoAlgorithm.LZO1Y, LzoConstraint.SPEED),
      LZO1Y_ASM_FAST_SAFE(LzoAlgorithm.LZO1Y, LzoConstraint.SAFETY),
      /**
       * lzo1z algorithms.
       */
      LZO1Z(LzoAlgorithm.LZO1Z),
      LZO1Z_SAFE(LzoAlgorithm.LZO1Z, LzoConstraint.SAFETY),
      /**
       * lzo2a algorithms.
       */
      LZO2A(LzoAlgorithm.LZO2A),
      LZO2A_SAFE(LzoAlgorithm.LZO2A, LzoConstraint.SAFETY);
    private final LzoAlgorithm algorithm;
    private final LzoConstraint constraint;

    private CompressionStrategy(LzoAlgorithm algorithm, 
                                LzoConstraint constraint) {
      this.algorithm = algorithm;
      this.constraint = constraint;
    }

    private CompressionStrategy(LzoAlgorithm algorithm) {
      this(algorithm, null);
    }

    public org.anarres.lzo.LzoDecompressor newDecompressor() {
      return LzoLibrary.getInstance().newDecompressor(algorithm, constraint);
    }
  }; // CompressionStrategy

  private final org.anarres.lzo.LzoDecompressor decompressor;
  private byte[] outputBuffer;
  private int outputBufferPos;
  // Also, end, since we base outputBuffer at 0.
  private final lzo_uintp outputBufferLen = new lzo_uintp();
  private boolean done = false;

  /**
   * The minimum version of LZO that we can read.
   * Set to 1.0 since there were a couple header
   * size changes prior to that.
   * See read_header() in lzop.c
   */
  public static int MINIMUM_LZO_VERSION = 0x0100;

  /**
   * Creates a new lzo decompressor.
   *
   * @param strategy lzo decompression algorithm
   * @param outputBufferSize size of the output buffer
   */
  public LzoDecompressor(CompressionStrategy strategy, int outputBufferSize) {
    this.decompressor = strategy.newDecompressor();
    setOutputBufferSize(outputBufferSize);
  }

  public void setOutputBufferSize(int outputBufferSize) {
    if (outputBuffer == null || outputBufferSize > outputBuffer.length)
      outputBuffer = new byte[outputBufferSize];
  }

  /**
   * Creates a new lzo decompressor.
   */
  public LzoDecompressor() {
    this(CompressionStrategy.LZO1X, 64 * 1024);
  }

  private void logState(String when) {
    LOG.info("\n");
    LOG.info(when + " Output buffer pos=" + outputBufferPos + "; length=" + 
	     outputBufferLen);
    // testInvariants();
  }

  @Override
  public void setInput(byte[] b, int off, int len) {
    if (b == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || off > b.length - len) {
      throw new ArrayIndexOutOfBoundsException("Illegal range in buffer: " +
                                               "Buffer length=" + b.length + 
                                               ", offset=" + off + ", length="
                                               + len);
    }
    if (!needsInput()) {
      throw new IllegalStateException("I don't need input: pos=" + 
                                      outputBufferPos + "; len=" + 
                                      outputBufferLen);
    }

    outputBufferLen.value = outputBuffer.length;
    try {
      outputBufferPos = 0;
      int code = decompressor.decompress(b, off, len, outputBuffer, 
                                         outputBufferPos, outputBufferLen);
      if (code != LzoTransformer.LZO_E_OK) {
        logState("LZO error: " + code);
        throw new IllegalArgumentException(decompressor.toErrorString(code));
      }
    } catch (IndexOutOfBoundsException e) {
      logState("IndexOutOfBoundsException: " + e);
      throw e;
    }
  }

  @Override
  public void setDictionary(byte[] b, int off, int len) {
    // nop
  }

  @Override
  public boolean needsInput() {
    // logState("Before needsInput");
    return outputBufferLen.value <= 0;
  }

  @Override
  public boolean needsDictionary() {
    return false;
  }

  @Override
  public int getRemaining() {
    return outputBufferLen.value;
  }

  @Override
  public boolean finished() {
    return outputBufferLen.value == 0 && done;
  }

  @Override
  public int decompress(byte[] b, int off, int len) throws IOException {
    if (b == null)
      throw new NullPointerException();
    if (off < 0 || len < 0 || off > b.length - len) {
      throw new ArrayIndexOutOfBoundsException("Illegal range in buffer: " +
                                               "Buffer length=" + b.length + 
                                               ", offset=" + off + 
                                               ", length=" + len);
    }
    len = Math.min(len, outputBufferLen.value);
    System.arraycopy(outputBuffer, outputBufferPos, b, off, len);
    outputBufferPos += len;
    outputBufferLen.value -= len;

    return len;
  }

  @Override
  public void reset() {
    outputBufferPos = 0;
    outputBufferLen.value = 0;
    done = false;
  }

  @Override
  public void end() {
    done = true;
  }
}
