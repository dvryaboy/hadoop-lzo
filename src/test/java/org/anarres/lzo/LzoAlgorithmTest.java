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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Formatter;
import java.io.InputStream;
import java.util.Random;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;


public class LzoAlgorithmTest {

  private static final Log LOG = LogFactory.getLog(LzoAlgorithmTest.class);

  public void testAlgorithm(LzoAlgorithm algorithm, byte[] orig, String desc) {
    LzoCompressor compressor = 
      LzoLibrary.getInstance().newCompressor(algorithm, null);

    LOG.info("\nCompressing " + orig.length + " " + desc + " bytes using " +
             algorithm);

    byte[] compressed = new byte[orig.length * 2];
    lzo_uintp compressed_length = new lzo_uintp(compressed.length);
    int compressed_code = compressor.compress(orig, 0, orig.length, compressed,
                                              0, compressed_length);

    LOG.info("Compressed: " + compressor.toErrorString(compressed_code) + 
             "; length=" + compressed_length);
    assertEquals(LzoTransformer.LZO_E_OK, compressed_code);

    LzoDecompressor decompressor = LzoLibrary.getInstance().newDecompressor(
        algorithm, null);
    byte[] uncompressed = new byte[orig.length];
    lzo_uintp uncompressed_length = new lzo_uintp(uncompressed.length);
    int uncompressed_code = 
      decompressor.decompress(compressed, 0, compressed_length.value, 
                              uncompressed, 0, uncompressed_length);

    LOG.info("Output:     " + decompressor.toErrorString(uncompressed_code));

    assertEquals(LzoTransformer.LZO_E_OK, uncompressed_code);
    assertArrayEquals(orig, uncompressed);
  }

  // Totally RLE.
  @Test
  public void testBlank() throws Exception {
    byte[] orig = new byte[512 * 1024];

    Arrays.fill(orig, (byte) 0);
    for (LzoAlgorithm algorithm : LzoAlgorithm.values()) {
      try {
        testAlgorithm(algorithm, orig, "blank");
      } catch (UnsupportedOperationException e) {
        // LOG.info("Unsupported algorithm " + algorithm);
      }
    }
  }

  // Highly cyclic.
  @Test
  public void testSequence() throws Exception {
    byte[] orig = new byte[512 * 1024];

    for (int i = 0; i < orig.length; i++) {
      orig[i] = (byte) (i & 0xf);
    }
    for (LzoAlgorithm algorithm : LzoAlgorithm.values()) {
      try {
        testAlgorithm(algorithm, orig, "sequential");
      } catch (UnsupportedOperationException e) {
        // LOG.info("Unsupported algorithm " + algorithm);
      }
    }
  }

  // Essentially uncompressible.
  @Test
  public void testRandom() throws Exception {
    Random r = new Random();

    for (int i = 0; i < 10; i++) {
      byte[] orig = new byte[256 * 1024];

      r.nextBytes(orig);
      for (LzoAlgorithm algorithm : LzoAlgorithm.values()) {
        try {
          testAlgorithm(algorithm, orig, "random");
        } catch (UnsupportedOperationException e) {
          // LOG.info("Unsupported algorithm " + algorithm);
        }
      }
    }
  }

  public void testClass(Class<?> type) throws Exception {
    String name = type.getName();

    name = name.replace('.', '/') + ".class";
    LOG.info("Class is " + name);
    InputStream in = getClass().getClassLoader().getResourceAsStream(name);
    byte[] orig = IOUtils.toByteArray(in);

    for (LzoAlgorithm algorithm : LzoAlgorithm.values()) {
      try {
        testAlgorithm(algorithm, orig, "class-file");
      } catch (UnsupportedOperationException e) {
        // LOG.info("Unsupported algorithm " + algorithm);
      }
    }
  }

  @Test
  public void testClass() throws Exception {
    testClass(getClass());
    testClass(Integer.class);
    testClass(Formatter.class);
  }
}
