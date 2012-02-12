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
import java.io.OutputStream;
import org.anarres.lzo.LzopConstants;
import org.apache.hadoop.io.compress.CompressionOutputStream;

public class LzopOutputStream extends CompressionOutputStream {

  public LzopOutputStream(OutputStream out, 
                          LzoCompressor.CompressionStrategy strategy, 
                          int bufferSize, OutputStream indexOut
			  ) throws IOException {
    super(new org.anarres.lzo.LzopOutputStream(out, strategy.newCompressor(),
                                               bufferSize, 0, indexOut));
  }

  @Override
  public void write(byte[] buf, int off, int len) throws IOException {
    out.write(buf, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
  }

  @Override
  public void finish() throws IOException {
    ((org.anarres.lzo.LzopOutputStream) out).finish();
  }

  @Override
  public void resetState() throws IOException {
    ((org.anarres.lzo.LzopOutputStream) out).resetState();
  }
}
