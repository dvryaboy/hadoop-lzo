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


import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;


public class LzoIndexOutputFormat extends OutputFormat<Path, LongWritable> {
  @Override
  public RecordWriter<Path, LongWritable> 
     getRecordWriter(TaskAttemptContext taskAttemptContext
                     ) throws IOException, InterruptedException {
    return new LzoIndexRecordWriter(taskAttemptContext);
  }

  @Override
  public void checkOutputSpecs(JobContext job
                               ) throws FileAlreadyExistsException, 
                                        IOException {
  }

  // A totally no-op output committer, because the
  // LzoIndexRecordWriter opens a file on the side and writes to that
  // instead.
  @Override
  public OutputCommitter 
      getOutputCommitter(TaskAttemptContext taskAttemptContext
                         ) throws IOException, InterruptedException {
    return new OutputCommitter() {
      @Override public void setupJob(JobContext jobContext) {}

      @Override public void cleanupJob(JobContext jobContext) {}

      @Override public void setupTask(TaskAttemptContext taskAttemptContext) {}

      @Override 
      public void commitTask(TaskAttemptContext taskAttemptContext) {}

      @Override public void abortTask(TaskAttemptContext taskAttemptContext) {}

      @Override 
      public boolean needsTaskCommit(TaskAttemptContext taskAttemptContext) {
        return false;
      }
    };
  }
}
