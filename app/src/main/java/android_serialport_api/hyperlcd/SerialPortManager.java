package android_serialport_api.hyperlcd;

import android.support.annotation.StringDef;
import android.util.Log;

/**
 * Created by ADan on 2017/10/14.
 */

public class SerialPortManager {


    private final String TAG = "ADan_SerialPortManager";
    private static SerialPortManager instance;

    private LogInterceptorSerialPort logInterceptor;

    public static final String other = "other";

    public static final String ttyCOM0 = "/dev/ttyCOM0";
    public static final String ttyCOM1 = "/dev/ttyCOM1";
    public static final String ttyCOM2 = "/dev/ttyCOM2";
    public static final String ttyCOM3 = "/dev/ttyCOM3";
    public static final String ttyS0 = "/dev/ttyS0";
    public static final String ttyS1 = "/dev/ttyS1";
    public static final String ttyS2 = "/dev/ttyS2";
    public static final String ttyS3 = "/dev/ttyS3";
    private SerialPort serialPort;

    public static final String demo = "demo";
    public static final String port = "port";
    public static final String read = "read";
    public static final String write = "write";
    public static final String joint = "joint";

    @StringDef({demo, port, read, write, joint, other})
    public @interface Type {
    }

    private SerialPortManager() {
    }

    public static SerialPortManager getInstances() {
        if (instance == null) {
            synchronized (SerialPortManager.class) {
                if (instance == null) {
                    instance = new SerialPortManager();
                }
            }
        }
        return instance;
    }

    public SerialPortManager setLogInterceptor(LogInterceptorSerialPort logInterceptor) {
        this.logInterceptor = logInterceptor;
        if (serialPort != null) {
            serialPort.setLogInterceptor(logInterceptor);
        }
        return this;
    }

    /**
     * 绑定串口服务
     */
    public SerialPortManager initSerialPort() {
        Log.e(TAG, "SerialPort初始化");
        serialPort = SerialPort.getInstance();
        if (logInterceptor != null) {
            serialPort.setLogInterceptor(logInterceptor);
        }
        return this;
    }

    /**
     * 解除串口服务绑定
     */
    public void destroySerialPort() {
        Log.e(TAG, "SerialPort销毁");
        try {
            serialPort.destroy();
            serialPort = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

}
