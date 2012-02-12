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


import org.junit.Test;
import static org.junit.Assert.*;


public class BinaryTest {

  private static int U(byte b) {
    return b & 0xff;
  }

  // Reproduced here so that we can make the method in LZO private and
  // static, for inlining.
  private static int UA_GET32(byte[] in, int in_ptr) {
    return (U(in[in_ptr]) << 24) | (U(in[in_ptr + 1]) << 16) |
        (U(in[in_ptr + 2]) << 8) | U(in[in_ptr + 3]);
  }

  @Test
  public void testShifts() {
    byte[] data = new byte[] { (byte) 0x81, 1, (byte) 0x82, 3};
    int value = UA_GET32(data, 0);

    assertEquals(0x81018203, value);
  }
}
