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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;

public class GenerateData {
  private static final Charset UTF8 = Charset.forName("UTF-8");
  private static final byte[] NEW_LINE = "\n".getBytes(UTF8);
  private static final byte[] SPACE = " ".getBytes(UTF8);
  private static final int LINE_WRAP = 70;
  private final String[] words;
  private final Random random;

  private static String[] readWords(File name) throws IOException {
    List<String> result = new ArrayList<String>();
    BufferedReader reader= new BufferedReader(new FileReader(name));
    String line = reader.readLine();
    while (line != null) {
      result.add(line);
      line = reader.readLine();
    }
    reader.close();
    return result.toArray(new String[result.size()]);
  }

  public GenerateData(File wordList, int seed) throws IOException {
    random = new Random(seed);
    words = readWords(wordList);
  }

  private void writeFile(OutputStream[] files, int size) throws IOException {
    int bytes = 0;
    int bytesInLine = 0;
    while (bytes < size) {
      if (bytesInLine >= LINE_WRAP) {
        for(OutputStream f: files) {
          f.write(NEW_LINE);
        }
        bytesInLine = 0;
        bytes += NEW_LINE.length;
      } else if (bytesInLine != 0) {
        for(OutputStream f: files) {
          f.write(SPACE);
        }
        bytesInLine += SPACE.length;
        bytes += SPACE.length;
      }
      byte[] word = words[random.nextInt(words.length)].getBytes(UTF8);
      for(OutputStream f: files) {
        f.write(word);
      }
      bytesInLine += word.length;
      bytes += word.length;
    }
  }

  private void writeCases(File outputDir, int cases, CompressionCodec... codecs
                          ) throws IOException {
    for(int i=0; i < cases; ++i) {
      String base = String.format("case-%07d", i);
      OutputStream[] out = new OutputStream[codecs.length+1];
      out[0] = new FileOutputStream(new File(outputDir, base + ".txt"));
      for(int c=0; c < codecs.length; ++c) {
        OutputStream f = 
          new FileOutputStream(new File(outputDir,
                                        base + ".txt" +
                                        codecs[c].getDefaultExtension()));
        out[c+1] = codecs[c].createOutputStream(f);
      }
      writeFile(out, random.nextInt(100000));
      for(OutputStream s: out) {
        s.close();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    GenerateData data = new GenerateData(new File(args[0]), 1965);
    Configuration conf = new Configuration();
    LzoCodec.setBufferSize(conf, 5*1024);
    LzoCodec lzo = new LzoCodec();
    lzo.setConf(conf);
    LzopCodec lzop = new LzopCodec();
    lzop.setConf(conf);
    data.writeCases(new File(args[1]), Integer.parseInt(args[2]), lzo, lzop);
  }
}