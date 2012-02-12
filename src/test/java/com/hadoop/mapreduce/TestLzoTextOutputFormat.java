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
 
package com.hadoop.mapreduce;

import com.hadoop.compression.lzo.LzoCodec;
import com.hadoop.compression.lzo.LzoIndex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestLzoTextOutputFormat {
  private static final Path testBuildData = 
    new Path(System.getProperty("test.build.data"));
  private static final Path outputDir = 
    new Path(System.getProperty("test.scratch"), "TestLzoTextOutputFormat");

  @Test
  public void testRecordWriter() throws IOException, InterruptedException {
    Configuration conf = new Configuration();
    conf.set("mapred.output.dir", outputDir.toString());
    LzoCodec.setBufferSize(conf, 1000);
    FileSystem fs = FileSystem.getLocal(conf);
    TaskAttemptID taskId = new TaskAttemptID("jt", 0, true, 0, 0);
    TaskAttemptContext context = new TaskAttemptContext(conf, taskId);
    LzoTextOutputFormat<Text,Text> format =
      new LzoTextOutputFormat<Text, Text>();
    Path inFile = new Path(testBuildData, "1000.txt");

    // copy the input
    RecordWriter<Text, Text> writer = format.getRecordWriter(context);
    BufferedReader in = 
      new BufferedReader(new InputStreamReader(fs.open(inFile)));
    String line = in.readLine();
    while (line != null) {
      Text key = new Text(line);
      writer.write(key,key);
      line = in.readLine();
    }
    writer.close(context);
    OutputCommitter committer = format.getOutputCommitter(context);
    committer.commitTask(context);

    // It is 7.8k in 8 1k blocks.
    LzoIndex index = LzoIndex.readIndex(fs, new Path(outputDir,
						     "part-m-00000.lzo"));
    assertEquals(8, index.getNumberOfBlocks());
  }
}