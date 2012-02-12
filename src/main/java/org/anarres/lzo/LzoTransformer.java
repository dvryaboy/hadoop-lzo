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


public interface LzoTransformer {

  public static final int LZO_E_OK = 0;
  public static final int LZO_E_ERROR = -1;
  public static final int LZO_E_OUT_OF_MEMORY = -2;
  public static final int LZO_E_NOT_COMPRESSIBLE = -3;
  public static final int LZO_E_INPUT_OVERRUN = -4;
  public static final int LZO_E_OUTPUT_OVERRUN = -5;
  public static final int LZO_E_LOOKBEHIND_OVERRUN = -6;
  public static final int LZO_E_EOF_NOT_FOUND = -7;
  public static final int LZO_E_INPUT_NOT_CONSUMED = -8;

  public LzoAlgorithm getAlgorithm();

  public LzoConstraint[] getConstraints();

  public String toErrorString(int code);
}
