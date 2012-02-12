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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestResetState {

  static final byte[] MARYS_LAMB =
    ("Mary had a little lamb,\n" +
     "Its fleece was white as snow,\n" +
     "And every where that Mary went\n" + 
     "The lamb was sure to go;\n" +
     "He followed her to school one day\n" +
     "That was against the rule,\n" +
     "It made the children laugh and play,\n" +
     "To see a lamb at school.\n" +
     "And so the Teacher turned him out,\n" +
     "But still he lingered near,\n" +
     "And waited patiently about,\n" +
     "Till Mary did appear;\n" +
     "And then he ran to her, and laid\n" +
     "His head upon her arm,\n" +
     "As if he said 'I'm not afraid\n" +
     "You'll keep me from all harm.'\n" +
     "'What makes the lamb love Mary so?'\n" +
     "The eager children cry\n" +
     "'O, Mary loves the lamb, you know,'\n" +
     "The Teacher did reply;\n" +
     "And you each gentle animal\n" +
     "In confidence may bind,\n" +
     "And make them follow at your call,\n" +
     "If you are always kind?\n").getBytes();

  private void copyStream(InputStream inStream, 
			  OutputStream outStream,
			  int maxBytes) throws IOException {
    byte[] buf = new byte[8000];
    int len = inStream.read(buf, 0, Math.min(buf.length, maxBytes));
    while (len != -1 && maxBytes > 0) {
      outStream.write(buf, 0, len);
      maxBytes -= len;
      len = inStream.read(buf, 0, Math.min(buf.length, maxBytes));
    }
  }

  @Test
  public void testLzopCodec() throws IOException {
    LzopCodec lzop = new LzopCodec();
    lzop.setConf(new Configuration());
    ByteArrayOutputStream buf = new ByteArrayOutputStream(8000);
    CompressionOutputStream outStream = lzop.createOutputStream(buf);
    outStream.write(MARYS_LAMB);
    outStream.finish();
    int midPoint = buf.size();
    outStream.resetState();
    outStream.write(MARYS_LAMB);
    outStream.close();
    // should have exactly twice as many bytes after doing it twice.
    assertEquals(midPoint*2, buf.size());
    CompressionInputStream inStream =
      lzop.createInputStream(new ByteArrayInputStream(buf.toByteArray()));
    buf.reset();
    copyStream(inStream, buf, Integer.MAX_VALUE);
    assertArrayEquals(MARYS_LAMB, buf.toByteArray());
    inStream.resetState();
    buf.reset();
    copyStream(inStream, buf, Integer.MAX_VALUE);
    assertArrayEquals(MARYS_LAMB, buf.toByteArray());
  }

  @Test
  public void testLzoCodec() throws IOException {
    LzoCodec lzo = new LzoCodec();
    lzo.setConf(new Configuration());
    ByteArrayOutputStream buf = new ByteArrayOutputStream(8000);
    CompressionOutputStream outStream = lzo.createOutputStream(buf);
    outStream.write(MARYS_LAMB);
    outStream.finish();
    int midPoint = buf.size();
    outStream.resetState();
    outStream.write(MARYS_LAMB);
    outStream.close();
    // should have exactly twice as many bytes after doing it twice.
    assertEquals(midPoint*2, buf.size());
    CompressionInputStream inStream =
      lzo.createInputStream(new ByteArrayInputStream(buf.toByteArray()));
    buf.reset();
    // lzo doesn't know the end of the stream, so we stop it explicitly
    copyStream(inStream, buf, MARYS_LAMB.length);
    assertArrayEquals(MARYS_LAMB, buf.toByteArray());
    inStream.resetState();
    buf.reset();
    copyStream(inStream, buf, Integer.MAX_VALUE);
    assertArrayEquals(MARYS_LAMB, buf.toByteArray());
  }
}