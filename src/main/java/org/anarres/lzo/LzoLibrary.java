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


public class LzoLibrary {

  private static class Inner {

    private static final LzoLibrary INSTANCE = new LzoLibrary();
  }

  public static LzoLibrary getInstance() {
    return Inner.INSTANCE;
  }

  /**
   * Returns a new compressor for the given algorithm.
   */
  public LzoCompressor newCompressor(LzoAlgorithm algorithm, 
                                     LzoConstraint constraint) {
    if (algorithm == null) {
      return new LzoCompressor1x_1();
    }
    switch (algorithm) {
    case LZO1X:
      return new LzoCompressor1x_1();

    case LZO1Y:
      return new LzoCompressor1y_1();

    default:
      throw new UnsupportedOperationException(
          "Unsupported algorithm " + algorithm);
    }
  }

  /**
   * Returns a new decompressor for the given algorithm.
   * The only constraint which makes sense is {@link LzoConstraint#SAFETY}.
   */
  public LzoDecompressor newDecompressor(LzoAlgorithm algorithm, 
                                         LzoConstraint constraint) {
    if (algorithm == null) {
      throw new NullPointerException("No algorithm specified.");
    }
    switch (algorithm) {

    case LZO1X:
      if (constraint == LzoConstraint.SAFETY) {
        return new LzoDecompressor1x_safe();
      } else {
        return new LzoDecompressor1x();
      }

    case LZO1Y:
      if (constraint == LzoConstraint.SAFETY) {
        return new LzoDecompressor1y_safe();
      } else {
        return new LzoDecompressor1y();
      }

    case LZO1Z:
      if (constraint == LzoConstraint.SAFETY) {
        return new LzoDecompressor1z_safe();
      } else {
        return new LzoDecompressor1z();
      }

    default:
      throw new UnsupportedOperationException(
          "Unsupported algorithm " + algorithm);

    }
  }
}
