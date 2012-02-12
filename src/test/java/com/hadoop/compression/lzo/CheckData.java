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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;

public class CheckData {

  private static final int BUF_SIZE = 3000;

  private static void readFully(InputStream in, byte[] buffer, int offset,
                                int len, File name, long base
                                ) throws IOException {
    while (len > 0) {
      int read = in.read(buffer, offset, len);
      if (read == -1) {
        throw new IOException("eof failure in " + name + " at " +
                              (base + offset));
      }
      offset += read;
      len -= read;
    }
  }

  private static void compareFiles(InputStream[] files,
                                   File[] names) throws IOException {
    long base = 0;
    byte[] expectedBuf = new byte[BUF_SIZE];
    int expectedLen = 0;
    byte[] actualBuf = new byte[BUF_SIZE];
    // read through all of the expected data
    while (true) {
      expectedLen = files[0].read(expectedBuf, 0, BUF_SIZE);
      if (expectedLen == -1) {
        break;
      }
      // for each other file, read the same amount and compare it
      for(int i=1; i < files.length; ++i) {
        readFully(files[i], actualBuf, 0, expectedLen, names[i], base);
        for(int b=0; b < expectedLen; ++b) {
          if (expectedBuf[b] != actualBuf[b]) {
            throw new IOException("Difference at " + (base + b) + " in " +
                                  names[i] + " expected " + expectedBuf[b] +
                                  " actual " + actualBuf[b]);
          }
        }
      }
      base += expectedLen;
    }
    files[0].close();
    // make sure the other streams don't have additional stuff
    for(int i=1; i < files.length; ++i) {
      int len = files[i].read(actualBuf, 0, BUF_SIZE);
      if (len != -1) {
        throw new IOException("Found " + len + " extra bytes in " +
                              names[i]);
      }
      files[i].close();
    }
  }

  private static void readCases(File inputDir, int cases, 
                                CompressionCodec... codecs
                                ) throws IOException {
    for(int i=0; i < cases; ++i) {
      String base = String.format("case-%07d", i);
      InputStream[] in = new InputStream[codecs.length+1];
      File[] names = new File[codecs.length+1];
      names[0] = new File(inputDir, base + ".txt");
      in[0] = new FileInputStream(names[0]);
      for(int c=0; c < codecs.length; ++c) {
        names[1+c] = new File(inputDir, base + ".txt" +
                              codecs[c].getDefaultExtension());
        InputStream f = new FileInputStream(names[1+c]);
        in[c+1] = codecs[c].createInputStream(f);
      }
      compareFiles(in, names);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    LzoCodec lzo = new LzoCodec();
    lzo.setConf(conf);
    LzopCodec lzop = new LzopCodec();
    lzop.setConf(conf);
    File dir = new File(args[0]);
    String[] files = dir.list();
    boolean haveLzo = false;
    boolean haveLzop = false;
    int cases = 0;
    for(String f: files) {
      if (f.endsWith(".txt")) {
        cases += 1;
      }
      if (!haveLzo) {
        haveLzo = f.endsWith(lzo.getDefaultExtension());
      }
      if (!haveLzop) {
        haveLzop = f.endsWith(lzop.getDefaultExtension());
      }
    }
    System.out.println("Checking " + cases + " cases lzo: " + haveLzo +
                       " lzop: " + haveLzop);
    if (haveLzo) {
      if (haveLzop) {
        readCases(dir, cases, lzo, lzop);
      } else {
        readCases(dir, cases, lzo);
      }
    } else {
      if (haveLzop) {
        readCases(dir, cases, lzop);
      } else {
        System.out.println("Nothing to do.");
        return;
      }
    }
    System.out.println("Done.");
  }
}