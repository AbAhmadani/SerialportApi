package android_serialport_api.hyperlcd;

import android.util.Log;

import androidx.annotation.StringDef;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ADan on 2017/10/14.
 */

public class SerialPortManager {

    private final String TAG = "ADan_SerialPortManager";
    private static SerialPortManager instance;
    private HashMap serialPorts;
    private LogInterceptorSerialPort logInterceptor;

    public static final String ttyCOM0 = "/dev/ttyCOM0";
    public static final String ttyCOM1 = "/dev/ttyCOM1";
    public static final String ttyCOM2 = "/dev/ttyCOM2";
    public static final String ttyCOM3 = "/dev/ttyCOM3";
    public static final String ttyS0 = "/dev/ttyS0";
    public static final String ttyS1 = "/dev/ttyS1";
    public static final String ttyS2 = "/dev/ttyS2";
    public static final String ttyS3 = "/dev/ttyS3";
    public static final String ttyGS0 = "/dev/ttyGS0";
    public static final String ttyGS1 = "/dev/ttyGS1";
    public static final String ttyGS2 = "/dev/ttyGS2";
    public static final String ttyGS3 = "/dev/ttyGS3";

    public static final String port = "port";
    public static final String read = "read";
    public static final String write = "write";
    public static final String append = "append";

    @StringDef({port, read, write, append})
    public @interface Type {
    }

    private SerialPortManager() {
        serialPorts = new HashMap();
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
        Iterator iter = serialPorts.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            SerialPort serialPort = (SerialPort) entry.getValue();
            serialPort.setLogInterceptor(logInterceptor);
        }
        return this;
    }

    /**
     * 启动串口
     *
     * @param port    serial port number
     * @param isAscii encoding format true:ascii false HexString
     * @param reader  read data monitoring
     */
    public boolean startSerialPort(String port, int baudRate, boolean isAscii, BaseReader reader) {
        return startSerialPort(port, isAscii, baudRate, 0, reader);
    }

    /**
     * 启动串口
     *
     * @param port     serial port number
     * @param baudRate baud rate
     * @param flags    mark
     * @param reader   read data monitoring
     */
    public boolean startSerialPort(String port, boolean isAscii, int baudRate, int flags, BaseReader reader) {
        SerialPort serial;
        if (serialPorts.containsKey(port)) {
            serial = (SerialPort) serialPorts.get(port);
        } else {
            serial = new SerialPort(port, isAscii, baudRate, flags);
            serialPorts.put(port, serial);
        }
        serial.setLogInterceptor(logInterceptor);
        return serial.open(reader);
    }

    /**
     * @param port
     * @param reader
     */
    public void setReader(String port, BaseReader reader) {
        if (serialPorts.containsKey(port)) {
            SerialPort serial = (SerialPort) serialPorts.get(port);
            serial.setReader(reader);
        }
    }

    public void setReadCode(String port, boolean isAscii) {
        if (serialPorts.containsKey(port)) {
            SerialPort serial = (SerialPort) serialPorts.get(port);
            serial.setReadCode(isAscii);
        }
    }

    public void send(String port, String cmd) {
        if (serialPorts.containsKey(port)) {
            SerialPort serial = (SerialPort) serialPorts.get(port);
            serial.write(cmd);
        }
    }

    public void send(String port, boolean isAscii, String cmd) {
        if (serialPorts.containsKey(port)) {
            SerialPort serial = (SerialPort) serialPorts.get(port);
            serial.write(isAscii, cmd);
        }
    }

    /**
     * Close the serial port
     *
     * @param port
     */
    public void stopSerialPort(String port) {
        if (serialPorts.containsKey(port)) {
            SerialPort serial = (SerialPort) serialPorts.get(port);
            serial.close();
            serialPorts.remove(serial);
            System.gc();
        }
    }

    /**
     * Whether to open
     *
     * @param port
     * @return
     */
    public boolean isStart(String port) {
        if (serialPorts.containsKey(port)) {
            SerialPort serial = (SerialPort) serialPorts.get(port);
            return serial.isOpen();
        }
        return false;
    }

    /**
     * Destroy resources
     */
    public void destroy() {
        Log.e(TAG, "SerialPort destruction");
        try {
            Iterator iter = serialPorts.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                SerialPort serial = (SerialPort) entry.getValue();
                serial.close();
                serialPorts.remove(serial);
            }
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
