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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GPLNativeCodeLoader {

  /**
   * This is used as a check to make sure that lzo will work. Since this
   * implementation doesn't require the native code, it will always work 
   * without and native code.
   * @return always return true
   */
  public static boolean isNativeCodeLoaded() {
    return true;
  }

}
