package com.rohit.encrypto.utils;


public class HexUtil {

    /**
     * Constructor
     */
    public HexUtil() {
    }

    /**
     * This method is used to convert string in to hex format
     *
     * @param s
     * @return byte[]
     */
    public static byte[] HexfromString(String s) {
        int i = s.length();
        byte abyte0[] = new byte[(i + 1) / 2];
        int j = 0;
        int k = 0;
        if (i % 2 == 1)
            abyte0[k++] = (byte) HexfromDigit(s.charAt(j++));
        while (j < i)
            abyte0[k++] = (byte) (HexfromDigit(s.charAt(j++)) << 4 | HexfromDigit(s.charAt(j++)));
        return abyte0;
    }

    /**
     * This method is used to return Hex form Digit
     *
     * @param c
     * @return int
     */
    public static int HexfromDigit(char c) {
        if (c >= '0' && c <= '9')
            return c - 48;
        if (c >= 'A' && c <= 'F')
            return (c - 65) + 10;
        if (c >= 'a' && c <= 'f')
            return (c - 97) + 10;
        else
            throw new IllegalArgumentException("invalid hex digit");
    }

    /**
     * This method is used to return string as Hex
     *
     * @param buf
     * @return String
     */
    public static String asHex(byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        for (int i = 0; i < buf.length; i++) {
            if ((buf[i] & 0xff) < 16)
                strbuf.append("0");
            strbuf.append(Long.toString(buf[i] & 0xff, 16));
        }

        return strbuf.toString();
    }

    /**
     * This method is used to convert Hex to string
     *
     * @param abyte0
     * @param i
     * @param j
     * @return String
     */
    public static String HextoString(byte abyte0[], int i, int j) {
        char ac[] = new char[j * 2];
        int k = 0;
        for (int l = i; l < i + j; l++) {
            byte byte0 = abyte0[l];
            ac[k++] = hexDigits[byte0 >>> 4 & 0xf];
            ac[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(ac);
    }

    /**
     * This method is used to convert Hex to string
     *
     * @param abyte0
     * @return String
     */
    public static String HextoString(byte abyte0[]) {
        return HextoString(abyte0, 0, abyte0.length);
    }

    private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

}