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


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.hadoop.compression.lzo.LzoCodec;
import com.hadoop.compression.lzo.LzoCompressor.CompressionStrategy;
import com.hadoop.compression.lzo.LzoIndex;
import com.hadoop.compression.lzo.LzopCodec;
import com.hadoop.compression.lzo.LzopOutputStream;


/**
 * An {@link OutputFormat} for lzop compressed text files. The files are
 * automatically compressed using LzopOutputStream and the index files are
 * generated.
 */
public class LzoTextOutputFormat<K,V> extends TextOutputFormat<K,V> {

  public RecordWriter<K, V> 
    getRecordWriter(TaskAttemptContext job
		    ) throws IOException, InterruptedException {
    Configuration conf = job.getConfiguration();
    String keyValueSeparator= conf.get("mapred.textoutputformat.separator",
				       "\t");
    int bufferSize = LzoCodec.getBufferSize(conf);
    CompressionStrategy strategy = LzoCodec.getCompressionStrategy(conf);
    Path file = getDefaultWorkFile(job, LzopCodec.DEFAULT_LZO_EXTENSION);
    FileSystem fs = file.getFileSystem(conf);
    OutputStream fileOut = fs.create(file, false);
    OutputStream indexOut = fs.create(LzoIndex.makeIndexName(file), false);
    OutputStream compressOut = new LzopOutputStream(fileOut, strategy,
						    bufferSize, indexOut);
    return new LineRecordWriter<K, V>(new DataOutputStream(compressOut),
				      keyValueSeparator);
  }
}