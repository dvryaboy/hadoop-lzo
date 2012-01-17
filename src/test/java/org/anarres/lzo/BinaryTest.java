/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.lzo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class BinaryTest {

    private static int U(byte b) {
        return b & 0xff;
    }

    // Reproduced here so that we can make the method in LZO private and static, for inlining.
    private static int UA_GET32(byte[] in, int in_ptr) {
        return (U(in[in_ptr]) << 24) | (U(in[in_ptr + 1]) << 16) | (U(in[in_ptr + 2]) << 8) | U(in[in_ptr + 3]);
    }

    @Test
    public void testShifts() {
        byte[] data = new byte[]{(byte) 0x81, 1, (byte) 0x82, 3};
        int value = UA_GET32(data, 0);
        assertEquals(0x81018203, value);
    }
}
