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
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.compress.BlockCompressorStream;
import org.apache.hadoop.io.compress.BlockDecompressorStream;
import org.apache.hadoop.io.compress.zlib.BuiltInZlibDeflater;
import org.apache.hadoop.io.compress.zlib.BuiltInZlibInflater;
import org.junit.Ignore;
import org.junit.Test;


public class ZlibCompressorTest {

  private static final Log LOG = LogFactory.getLog(ZlibCompressorTest.class);
  private static final String PATH = "src/main/jcpp/src/miniacc.h";

  @Test
  public void testEmpty() {
    LOG.info("Every test suite must have a case, or JUnit gets unhappy.");
  }

  @Ignore
  @Test
  public void testBlockCompressorStream() throws Exception {
    try {
      LOG.info("Total memory is " + Runtime.getRuntime().totalMemory());
      LOG.info("Max memory is " + Runtime.getRuntime().maxMemory());

      FileInputStream fi = new FileInputStream(new File(PATH));
      DataInputStream di = new DataInputStream(fi);
      byte[] data = new byte[512 * 1024 * 1024];

      di.readFully(data);
      LOG.info("Original data is " + data.length + " bytes.");

      ByteArrayInputStream bi = new ByteArrayInputStream(data);
      ByteArrayOutputStream bo = new ByteArrayOutputStream(data.length);
      BlockCompressorStream co = 
        new BlockCompressorStream(bo, new BuiltInZlibDeflater());

      LOG.info("Starting.");
      long start = System.currentTimeMillis();

      IOUtils.copy(bi, co);
      co.close();
      long end = System.currentTimeMillis();

      LOG.info("Compression took " + ((end - start) / 1000d) + " ms");
      LOG.info("Compressed data is " + bo.size() + " bytes.");

      if (true) {
        return;
      }

      bi = new ByteArrayInputStream(bo.toByteArray());
      BlockDecompressorStream ci = 
        new BlockDecompressorStream(bi, new BuiltInZlibInflater());

      bo.reset();
      start = System.currentTimeMillis();
      IOUtils.copy(ci, bo);
      end = System.currentTimeMillis();
      LOG.info("Uncompression took " + ((end - start) / 1000d) + " ms");
      LOG.info("Uncompressed data is " + bo.size() + " bytes.");
    } finally {
      System.out.flush();
      System.err.flush();
      Thread.sleep(100);
    }
  }
}
