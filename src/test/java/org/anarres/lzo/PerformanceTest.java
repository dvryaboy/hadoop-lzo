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


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;


public class PerformanceTest {

  private static final Log LOG = LogFactory.getLog(
      BlockCompressorStreamTest.class);
  private static final String PATH = "src/main/jcpp/src/miniacc.h";

  @Test
  public void testBlockCompress() throws Exception {
    try {
      LOG.info("Total memory is " + Runtime.getRuntime().totalMemory());
      LOG.info("Max memory is " + Runtime.getRuntime().maxMemory());

      File file = new File(PATH);

      assumeTrue(file.isFile());
      FileInputStream fi = new FileInputStream(new File(PATH));
      DataInputStream di = new DataInputStream(fi);
      int length = Math.min((int) file.length(), 512 * 1024 * 1024);
      byte[] data = new byte[length];

      di.readFully(data);
      LOG.info("Original data is " + data.length + " bytes.");
      byte[] compressed = new byte[data.length * 106 / 100];

      LzoCompressor compressor = new LzoCompressor1x_1();
      LzoDecompressor decompressor = new LzoDecompressor1x();

      for (int i = 0; i < 8; i++) {
        lzo_uintp compressed_length = new lzo_uintp(compressed.length);

        LOG.info("Starting.");
        long start = System.currentTimeMillis();

        compressor.compress(data, 0, data.length, compressed, 0
            ,
            compressed_length);
        long end = System.currentTimeMillis();

        LOG.info(
            "Compression took " + ((end - start) / 1000d) + " ms " +
            "and output " + compressed_length + " bytes, " + "ratio=" +
            (data.length / (double) compressed_length.value));

        lzo_uintp uncompressed_length = new lzo_uintp(data.length);

        start = System.currentTimeMillis();
        decompressor.decompress(compressed, 0, compressed_length.value, data, 
                                0, uncompressed_length);
        end = System.currentTimeMillis();
        LOG.info("Uncompression took " + ((end - start) / 1000d) + 
                 " ms and output " + uncompressed_length + " bytes");
        assertEquals(data.length, uncompressed_length.value);
      }
    } finally {
      System.out.flush();
      System.err.flush();
      Thread.sleep(100);
    }
  }
}
