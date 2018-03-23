package android_serialport_api.hyperlcd;


/**
 * Created by ADan on 2017/12/4.
 */

public abstract class ReadListener {

    private LogInterceptorSerialPort logInterceptor;

    public void onBaseRead(String port, boolean isAscii, String read) {
        if (logInterceptor != null) {
            logInterceptor.log(SerialPortManager.read, port, isAscii, read);
        }
        onRead(port, isAscii, read);
    }

    public abstract void onRead(String port, boolean isAscii, String read);

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
