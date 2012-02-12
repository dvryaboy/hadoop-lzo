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


public class LzopConstants {

  /**
   * 9 bytes at the top of every lzo file */
  public static final byte[] LZOP_MAGIC = new byte[] {
    -119, 'L', 'Z', 'O', 0, '\r', '\n', '\032', '\n'};

  /**
   * Version of lzop this emulates */
  public static final int LZOP_VERSION = 0x1010;

  /**
   * Latest version of lzop this should be compatible with */
  public static final int LZOP_COMPAT_VERSION = 0x0940;
  public static final byte M_LZO1X_1 = 1;
  public static final byte M_LZO1X_1_15 = 2;
  public static final byte M_LZO1X_999 = 3;

  /*
   * header flags */
  public static final long F_ADLER32_D = 0x00000001L;
  public static final long F_ADLER32_C = 0x00000002L;
  public static final long F_STDIN = 0x00000004L;
  public static final long F_STDOUT = 0x00000008L;
  public static final long F_NAME_DEFAULT = 0x00000010L;
  public static final long F_DOSISH = 0x00000020L;
  public static final long F_H_EXTRA_FIELD = 0x00000040L;
  public static final long F_H_GMTDIFF = 0x00000080L;
  public static final long F_CRC32_D = 0x00000100L;
  public static final long F_CRC32_C = 0x00000200L;
  public static final long F_MULTIPART = 0x00000400L;
  public static final long F_H_FILTER = 0x00000800L;
  public static final long F_H_CRC32 = 0x00001000L;
  public static final long F_H_PATH = 0x00002000L;
  public static final long F_MASK = 0x00003FFFL;

  /*
   * operating system & file system that created the file [mostly unused] */
  public static final long F_OS_FAT = 0x00000000L; /*
   * DOS, OS2, Win95 */

  public static final long F_OS_AMIGA = 0x01000000L;
  public static final long F_OS_VMS = 0x02000000L;
  public static final long F_OS_UNIX = 0x03000000L;
  public static final long F_OS_VM_CMS = 0x04000000L;
  public static final long F_OS_ATARI = 0x05000000L;
  public static final long F_OS_OS2 = 0x06000000L; /*
   * OS2 */

  public static final long F_OS_MAC9 = 0x07000000L;
  public static final long F_OS_Z_SYSTEM = 0x08000000L;
  public static final long F_OS_CPM = 0x09000000L;
  public static final long F_OS_TOPS20 = 0x0a000000L;
  public static final long F_OS_NTFS = 0x0b000000L; /*
   * Win NT/2000/XP */

  public static final long F_OS_QDOS = 0x0c000000L;
  public static final long F_OS_ACORN = 0x0d000000L;
  public static final long F_OS_VFAT = 0x0e000000L; /*
   * Win32 */

  public static final long F_OS_MFS = 0x0f000000L;
  public static final long F_OS_BEOS = 0x10000000L;
  public static final long F_OS_TANDEM = 0x11000000L;
  public static final int F_OS_SHIFT = 24;
  public static final long F_OS_MASK = 0xff000000L;

  /*
   * character set for file name encoding [mostly unused] */
  public static final long F_CS_NATIVE = 0x00000000L;
  public static final long F_CS_LATIN1 = 0x00100000L;
  public static final long F_CS_DOS = 0x00200000L;
  public static final long F_CS_WIN32 = 0x00300000L;
  public static final long F_CS_WIN16 = 0x00400000L;
  public static final long F_CS_UTF8 = 0x00500000L; /*
   * filename is UTF-8 encoded */

  public static final int F_CS_SHIFT = 20;
  public static final long F_CS_MASK = 0x00f00000L;

  /*
   * these bits must be zero */
  public static final long F_RESERVED = ((F_MASK | F_OS_MASK | F_CS_MASK) ^
      0xffffffffL);
}
