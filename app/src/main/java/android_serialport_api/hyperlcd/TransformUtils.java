package android_serialport_api.hyperlcd;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Created by ADan on 2017/10/14.
 */

public class TransformUtils {

    /**
     * 16进制数字字符集
     */
    private static String hexString = "0123456789ABCDEF";

    /**
     * ASCII字符串 转 16进制数字,适用于所有字符（包括中文）
     *
     * @param str
     * @return String
     */
    public static String asciiString2HexString(String str) throws UnsupportedEncodingException {
        // 根据默认编码获取字节数组
        byte[] bytes = null;
        bytes = str.getBytes("GBK");
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    /**
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     *
     * @param bytes
     * @return String
     */
    public static String hexString2AsciiString(String bytes) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2) {
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        }

        return new String(baos.toByteArray(), "GBK");

    }

    /**
     * 字节数组 转 ASCII字符串
     *
     * @param buffer 字节数组
     * @param size   数据有效位长度
     * @return ASCII字符串
     */
    public static String byte2AsciiString(byte[] buffer, int size) {
        return new String(buffer, 0, size);
    }

    /**
     * 字符串 转 字节数组
     *
     * @param str Ascii字符串
     * @return 字节数组
     */
    public static byte[] AsciiString2byte(String str) {
        return str.getBytes();
    }

    /**
     * 字节数组 转 16进制字符串
     *
     * @param buffer 字节数组
     * @param size   数据有效位长度
     * @return String
     */
    public static String bytes2HexString(byte[] buffer, int size) {
        StringBuffer sb = new StringBuffer();
        if (buffer == null || size <= 0) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(buffer[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase(Locale.getDefault());
    }

    /**
     * 16进制字符串 转 字节数组
     *
     * @param hex
     * @return
     */
    public static byte[] hexStringToBytes(String hex) {
        byte[] ret = new byte[hex.length() / 2];
        byte[] tmp = hex.getBytes();
        for (int i = 0; i < tmp.length / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /**
     * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
     *
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0}))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }


}
