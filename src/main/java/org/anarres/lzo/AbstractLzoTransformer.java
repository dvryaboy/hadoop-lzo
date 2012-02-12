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


public class AbstractLzoTransformer implements LzoTransformer {

  private final LzoAlgorithm algorithm;
  private final LzoConstraint[] constraints;

  public AbstractLzoTransformer(LzoAlgorithm algorithm, 
      LzoConstraint... constraints) {
    this.algorithm = algorithm;
    this.constraints = constraints;
  }

  @Override
  public LzoAlgorithm getAlgorithm() {
    return algorithm;
  }

  @Override
  public LzoConstraint[] getConstraints() {
    return constraints;
  }

  @Override
  public String toErrorString(int code) {
    switch (code) {
    case LZO_E_OK:
      return "OK";

    case LZO_E_ERROR:
      return "Error";

    case LZO_E_OUT_OF_MEMORY:
      return "Out of memory";

    case LZO_E_NOT_COMPRESSIBLE:
      return "Not compressible";

    case LZO_E_INPUT_OVERRUN:
      return "Input overrun";

    case LZO_E_OUTPUT_OVERRUN:
      return "Output overrun";

    case LZO_E_LOOKBEHIND_OVERRUN:
      return "Lookbehind overrun";

    case LZO_E_EOF_NOT_FOUND:
      return "EOF not found";

    case LZO_E_INPUT_NOT_CONSUMED:
      return "Input not consumed";

    default:
      return "Unknown-" + code;
    }
  }
}
