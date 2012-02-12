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

import java.io.IOException;
import java.io.FilterOutputStream;
import java.io.OutputStream;

/**
 * This class is for debugging output streams to see what is going wrong.
 */
public class DebugOutputStream extends FilterOutputStream {

  private final String name;
  private long offset;

  public DebugOutputStream(String name, OutputStream out) throws IOException {
    super(out);
    this.name = name;
    offset = 0;
  }

  @Override
  public void write(byte[] buf, int off, int len) throws IOException {
    System.out.print("Stream " + name + " - " + offset + " write (" +
                     Integer.toString(len, 16) + ")");
    for(int i=0; i < 20 && i < len; ++i) {
      System.out.print(" " + Integer.toString(0xff & buf[off + i], 16));
    }
    System.out.println();
    offset += len;
    out.write(buf, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    System.out.println("Stream " + name + " - " + offset + " write (1) " +
                       Integer.toString(0xff & b, 16));
    offset += 1;
    out.write(b);
  }

  @Override
  public void flush() throws IOException {
    System.out.println("Stream " + name + " - " + offset + " flush");
    out.close();
  }

  @Override
  public void close() throws IOException {
    System.out.println("Stream " + name + " - " + offset + " close");
    out.close();
  }
}
