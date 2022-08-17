package android_serialport_api.hyperlcd;


/**
 * Created by ADan on 2017/12/4.
 */

public abstract class BaseReader {

    private LogInterceptorSerialPort logInterceptor;
    public String port;
    public boolean isAscii;

    void onBaseRead(String port, boolean isAscii, byte[] buffer, int size) {
        this.port = port;
        this.isAscii = isAscii;
        String read;
        if (isAscii) {
            read = new String(buffer, 0, size);
        } else {
            read = TransformUtils.bytes2HexString(buffer, size);
        }
        log(SerialPortManager.read, port, isAscii, new StringBuffer("read ").append(read));
        onParse(port, isAscii, read);
    }

    protected abstract void onParse(String port, boolean isAscii, String read);

    public void setLogInterceptor(LogInterceptorSerialPort logInterceptor) {
        this.logInterceptor = logInterceptor;
    }

    protected void log(@SerialPortManager.Type String type, String port, boolean isAscii, CharSequence log) {
        log(type, port, isAscii, log == null ? "null" : log.toString());
    }

    protected void log(@SerialPortManager.Type String type, String port, boolean isAscii, String log) {
        if (logInterceptor != null) {
            logInterceptor.log(type, port, isAscii, log);
        }
    }
}
