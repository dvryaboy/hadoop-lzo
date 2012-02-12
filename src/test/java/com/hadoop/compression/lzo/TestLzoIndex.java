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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.hadoop.compression.lzo.LzoCompressor.CompressionStrategy;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestLzoIndex {
  private static final Path testBuildData = 
    new Path(System.getProperty("test.build.data"));
  private static final Path outputDir = 
    new Path(System.getProperty("test.scratch"), "TestLzoIndex");
  private static final String BIG_FILE = "100000.txt";
  private static final String BIG_LZO_FILE = BIG_FILE + 
    LzopCodec.DEFAULT_LZO_EXTENSION;

  @Test
  public void testCreateIndex() throws IOException {
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.getLocal(conf);
    // wipe the directory and copy the input file
    fs.delete(outputDir, true);
    fs.mkdirs(outputDir);
    FileUtil.copy(fs, new Path(testBuildData, BIG_LZO_FILE), fs, outputDir,
		  false, false, conf);

    // create the index file
    Path lzoFile = new Path(outputDir, BIG_LZO_FILE);
    LzoIndex.createIndex(fs, lzoFile);

    // make sure we read it back correctly
    LzoIndex index = LzoIndex.readIndex(fs, lzoFile);
    assertEquals(3, index.getNumberOfBlocks());

    // rename the index to the old name
    fs.rename(LzoIndex.makeIndexName(lzoFile),
	      lzoFile.suffix(".index"));

    // make sure we read it back correctly
    index = LzoIndex.readIndex(fs, lzoFile);
    assertEquals(3, index.getNumberOfBlocks());
  }

  @Test
  public void testConcurrentIndexWriting() throws IOException {
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.getLocal(conf);
    // wipe the directory and copy the input file
    fs.delete(outputDir, true);
    fs.mkdirs(outputDir);

    // compress the input file and write both the compressed and index files
    InputStream in = fs.open(new Path(testBuildData, BIG_FILE));
    Path lzoFile = new Path(outputDir, BIG_LZO_FILE);
    Path lzoIndexFile = LzoIndex.makeIndexName(lzoFile);
    OutputStream out = new LzopOutputStream(fs.create(lzoFile),
					    CompressionStrategy.LZO1X_1,
					    100000,
					    fs.create(lzoIndexFile));

    // copy the input into the compressor
    byte[] buffer = new byte[100000];
    int len = in.read(buffer, 0, buffer.length);
    while (len > 0) {
      out.write(buffer, 0, len);
      len = in.read(buffer, 0, buffer.length);
    }
    in.close();
    out.close();

    // read the index, it should have 6 entries (575k divided into 100k blocks)
    LzoIndex index = LzoIndex.readIndex(fs, lzoFile);
    assertEquals(6, index.getNumberOfBlocks());

    // for each index, make sure that its position is higher than the previous
    // one and that the resulting stream is both readable and shorter than the
    // previous one.
    int prevLeft = Integer.MAX_VALUE;
    long prevIndex = 0;
    for(int i=0; i < index.getNumberOfBlocks(); ++i) {
      FSDataInputStream inSeek = fs.open(lzoFile);
      InputStream uncomp = new LzopInputStream(inSeek);
      long offset = index.getPosition(i);
      assertTrue("prevIndex (" + prevIndex + ") < offset (" + offset + ")",
		 prevIndex < offset);
      inSeek.seek(offset);
      prevIndex = offset;
      int bytesLeft = consumeStream(uncomp);
      assertTrue("bytesLeft (" + bytesLeft + ") < prevLeft (" + prevLeft + ")",
		 bytesLeft < prevLeft);
      prevLeft = bytesLeft;
    }
  }
  
  /**
   * Consume the stream and return the length in bytes
   */
  private static int consumeStream(InputStream in) throws IOException {
    int result = 0;
    byte[] buffer = new byte[1000];
    int len = in.read(buffer, 0, 1000);
    while (len > 0) {
      result += len;
      len = in.read(buffer, 0, 1000);
    }
    return result;
  }
}