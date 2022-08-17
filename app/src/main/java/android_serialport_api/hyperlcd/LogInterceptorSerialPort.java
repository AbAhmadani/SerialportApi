package android_serialport_api.hyperlcd;

/**
 * Created by ADan on 2018/3/1.
 * Serial log interceptor
 */

public interface LogInterceptorSerialPort {

    /**
     * Operation log output callback
     *
     * @param type    Log type
     * @param port    Serial port
     * @param isAscii true:ASCIIEncoding false: hexadecimal encoding
     * @param log     Log content
     */
    void log(@SerialPortManager.Type String type, String port, boolean isAscii, String log);

}
