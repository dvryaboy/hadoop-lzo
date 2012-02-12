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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.util.ReflectionUtils;

import com.hadoop.compression.lzo.LzopCodec;


/**
 * Unit Test for LZO with random data.
 */
public class TestLzoRandData extends TestCase {

  Configuration conf;
  CompressionCodec codec;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    conf = new Configuration();
    conf.set("io.compression.codecs", LzopCodec.class.getName());
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testLzoRandData() throws Exception {
    runTest(100, 100000);
  }

  public void testLzoRandDataLargeChunks() throws Exception {
    runTest(20, 500000);
  }

  public void testLzoRandDataHugeChunks() throws Exception {
    runTest(10, 1000000);
  }

  private void runTest(int numChunks, int chunkSize) throws Exception {
    CompressionCodec codec = 
      ReflectionUtils.newInstance(LzopCodec.class, conf);

    final Random writerRand = new Random(12345);
    final Random readerRand = new Random(12345);

    File outDir = new File(System.getProperty("test.scratch"));

    outDir.mkdirs();
    File testFile = new File(outDir, "randdata");
    String fileName = testFile.getAbsolutePath();

    // Create the file
    OutputStream fos = new FileOutputStream(fileName);

    fos = codec.createOutputStream(fos);

    // Write file
    byte[] data = new byte[chunkSize];

    for (int i = 0; i < numChunks; i++) {
      writerRand.nextBytes(data);
      fos.write(data);
    }
    fos.close();

    // Open file
    InputStream tis = new FileInputStream(fileName);

    tis = codec.createInputStream(tis);

    // Read file
    byte[] dataExpected = new byte[chunkSize];
    byte[] dataRead = new byte[chunkSize];

    for (int i = 0; i < numChunks; i++) {
      readerRand.nextBytes(dataExpected);
      readFully(tis, dataRead);
      assertArrayEquals(dataExpected, dataRead);
    }

    assertEquals(-1, tis.read());
    tis.close();
  }

  private void readFully(InputStream in, byte[] b) throws IOException {
    int pos = 0;

    do {
      int len = in.read(b, pos, b.length - pos);

      if (len < 0) {
        fail("Unexpected end of file.");
      }
      pos += len;
    } while (pos < b.length);
  }

  /**
   * Assert that two arrays are equal.
   */
  private void assertArrayEquals(byte[] expected, byte[] actual) {
    assertEquals("Array lengths are different", 
                 expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Array elements " + i + " are different", expected[i]
          ,
          actual[i]);
    }
  }

}

