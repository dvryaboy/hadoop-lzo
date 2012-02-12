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


import java.util.Random;
import org.apache.commons.io.input.NullInputStream;

public class RandomInputStream extends NullInputStream {

  private final Random r = new Random();

  public RandomInputStream(long size) {
    super(size);
  }

  @Override
  protected int processByte() {
    return r.nextInt() & 0xff;
  }

  @Override
  protected void processBytes(byte[] bytes, int offset, int length) {
    r.nextBytes(bytes);
  }
}
