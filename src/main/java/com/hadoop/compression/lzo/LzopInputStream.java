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
import java.io.InputStream;
import org.apache.hadoop.io.compress.CompressionInputStream;

public class LzopInputStream extends CompressionInputStream {

    public LzopInputStream(InputStream in) throws IOException {
        super(new org.anarres.lzo.LzopInputStream(in));
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return in.read(buf, off, len);
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public void resetState() throws IOException {
      ((org.anarres.lzo.LzopInputStream) in).resetState();
    }

    public int getCompressedChecksumsCount() {
      return 
        ((org.anarres.lzo.LzopInputStream) in).getCompressedChecksumCount();
    }

    public int getDecompressedChecksumsCount() {
      return 
        ((org.anarres.lzo.LzopInputStream) in).getUncompressedChecksumCount();
    }
}
