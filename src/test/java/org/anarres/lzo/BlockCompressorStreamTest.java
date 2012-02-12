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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import com.hadoop.compression.lzo.LzoCompressor;
import com.hadoop.compression.lzo.LzoDecompressor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.compress.BlockCompressorStream;
import org.apache.hadoop.io.compress.BlockDecompressorStream;
import org.junit.Test;


public class BlockCompressorStreamTest {

  private static final Log LOG = LogFactory.getLog(
      BlockCompressorStreamTest.class);
  private static final String PATH = "src/main/jcpp/src/miniacc.h";
  private static final String TEST_SCRATCH = "target/test-scratch";
  private static final String OUT_PATH = TEST_SCRATCH + "/compressed.out";
  static {
    new File(TEST_SCRATCH).mkdirs();
  }

  @Test
  public void testBlockCompressorStream() throws Throwable {
    try {
      LOG.info("Total memory is " + Runtime.getRuntime().totalMemory());
      LOG.info("Max memory is " + Runtime.getRuntime().maxMemory());

      FileInputStream fi = new FileInputStream(new File(PATH));
      DataInputStream di = new DataInputStream(fi);
      int len = (int) new File(PATH).length();
      byte[] data = new byte[len];

      di.readFully(data);
      LOG.info("Original data is " + data.length + " bytes.");

      for (int i = 0; i < 1; i++) {
        ByteArrayInputStream bi = new ByteArrayInputStream(data);
        ByteArrayOutputStream bo = new ByteArrayOutputStream(data.length);
        BlockCompressorStream co = new BlockCompressorStream(bo
            ,
            new LzoCompressor(), 64 * 1024, 18);

        LOG.info("Starting.");
        long start = System.currentTimeMillis();

        IOUtils.copy(bi, co);
        co.close();
        long end = System.currentTimeMillis();

        LOG.info("Compression took " + ((end - start) / 1000d) + " ms");
        LOG.info("Compressed data is " + bo.size() + " bytes.");

        byte[] cb = bo.toByteArray();

        FileUtils.writeByteArrayToFile(new File(OUT_PATH), cb);

        bi = new ByteArrayInputStream(cb);
        BlockDecompressorStream ci = new BlockDecompressorStream(bi
            ,
            new LzoDecompressor());

        bo.reset();
        start = System.currentTimeMillis();
        IOUtils.copy(ci, bo);
        end = System.currentTimeMillis();
        LOG.info("Uncompression took " + ((end - start) / 1000d) + " ms");
        LOG.info("Uncompressed data is " + bo.size() + " bytes.");
      }
    } catch (Throwable t) {
      LOG.error(t, t);
      throw t;
    } finally {
      System.out.flush();
      System.err.flush();
      Thread.sleep(100);
    }
  }
}
