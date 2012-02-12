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


import java.io.File;
import org.junit.Ignore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import static org.junit.Assert.*;


public class LzopStreamTest {

  private static final Log LOG = LogFactory.getLog(LzopStreamTest.class);
  private static final String TEST_SCRATCH = "target/test-scratch";
  private static final String OUT_PATH = TEST_SCRATCH + "/temp.lzo";
  static {
    new File(TEST_SCRATCH).mkdirs();
  }
  private static long[] FLAGS = new long[] {
    0L, // Adler32
    LzopConstants.F_ADLER32_C, LzopConstants.F_ADLER32_D
        ,
    LzopConstants.F_ADLER32_C | LzopConstants.F_ADLER32_D, // CRC32
    LzopConstants.F_CRC32_C, LzopConstants.F_CRC32_D
        ,
    LzopConstants.F_CRC32_C | LzopConstants.F_CRC32_D, // Both
    LzopConstants.F_ADLER32_C | LzopConstants.F_CRC32_C
        ,
    LzopConstants.F_ADLER32_D | LzopConstants.F_CRC32_D
        ,
    LzopConstants.F_ADLER32_C | LzopConstants.F_ADLER32_D |
        LzopConstants.F_CRC32_C | LzopConstants.F_CRC32_D
  };

  public void testAlgorithm(LzoAlgorithm algorithm, 
                            byte[] orig) throws IOException {
    for (long flags : FLAGS) {
      try {
        LzoCompressor compressor = LzoLibrary.getInstance().newCompressor(
            algorithm, null);

        LOG.info("Compressing " + orig.length + " bytes using " + algorithm);

        // LOG.info("Original:   " + Arrays.toString(orig));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LzopOutputStream cs = new LzopOutputStream(os, compressor, 256, flags,
						   null);

        cs.write(orig);
        cs.close();

        FileUtils.writeByteArrayToFile(new File(OUT_PATH), os.toByteArray());

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        LzopInputStream us = new LzopInputStream(is);
        DataInputStream ds = new DataInputStream(us);
        byte[] uncompressed = new byte[orig.length];

        ds.readFully(uncompressed);

        assertArrayEquals(orig, uncompressed);
      } finally {
        System.out.flush();
        System.err.flush();
      }
    }
  }

  // Totally RLE.
  @Test
  public void testBlank() throws Exception {
    byte[] orig = new byte[512 * 1024];

    Arrays.fill(orig, (byte) 0);
    testAlgorithm(LzoAlgorithm.LZO1X, orig);
  }

  // Highly cyclic.
  @Test
  public void testSequence() throws Exception {
    byte[] orig = new byte[512 * 1024];

    for (int i = 0; i < orig.length; i++) {
      orig[i] = (byte) (i & 0xf);
    }
    testAlgorithm(LzoAlgorithm.LZO1X, orig);
  }

  // Essentially uncompressible.
  @Test
  public void testRandom() throws Exception {
    Random r = new Random();

    for (int i = 0; i < 10; i++) {
      byte[] orig = new byte[256 * 1024];

      r.nextBytes(orig);
      testAlgorithm(LzoAlgorithm.LZO1X, orig);
    }
  }

  public void testClass(Class<?> type) throws Exception {
    String name = type.getName();

    name = name.replace('.', '/') + ".class";
    LOG.info("Class is " + name);
    InputStream in = getClass().getClassLoader().getResourceAsStream(name);
    byte[] orig = IOUtils.toByteArray(in);

    testAlgorithm(LzoAlgorithm.LZO1X, orig);
  }

  @Test
  public void testClass() throws Exception {
    testClass(getClass());
    testClass(Integer.class);
    testClass(Formatter.class);
  }
}
