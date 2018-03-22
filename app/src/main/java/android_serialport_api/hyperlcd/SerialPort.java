package android_serialport_api.hyperlcd;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by ADan on 2017/9/16.
 */

public class SerialPort {

    private static SerialPort instance;
    private HashMap serialPorts;
    private HashMap inputStreams;
    private HashMap outputStreams;
    private HashMap readThreads;
    private LogInterceptorSerialPort logInterceptor;

    private SerialPort() {
        serialPorts = new HashMap<String, android_serialport_api.SerialPort>();
        inputStreams = new HashMap<String, InputStream>();
        outputStreams = new HashMap<String, OutputStream>();
        readThreads = new HashMap<String, ReadThread>();
        log(SerialPortManager.demo, SerialPortManager.other, true, "启动串口模块：SerialPort.onCreate");
    }

    public static SerialPort getInstance() {
        if (instance == null) {
            synchronized (SerialPort.class) {
                if (instance == null) {
                    instance = new SerialPort();
                }
            }
        }
        return instance;
    }

    public void destroy() {
        serialPorts = null;
        inputStreams = null;
        outputStreams = null;
        readThreads = null;
        logInterceptor = null;
        instance = null;
        log(SerialPortManager.demo, SerialPortManager.other, true, "关闭串口模块：SerialPort.onDestroy");
        System.gc();
    }

    public void setLogInterceptor(LogInterceptorSerialPort logInterceptor) {
        this.logInterceptor = logInterceptor;
    }

    private void log(@SerialPortManager.Type String type, @SerialPortManager.Port String port, boolean isAscii, CharSequence log) {
        log(type, port, isAscii, log == null ? "null" : log.toString());
    }

    private void log(@SerialPortManager.Type String type, @SerialPortManager.Port String port, boolean isAscii, String log) {
        if (logInterceptor != null) {
            logInterceptor.log(type, port, isAscii, log);
        }
    }

    /**
     * 启动串口 默认波特率9600
     *
     * @param port         串口号
     * @param isAscii      是否是Ascii编码，否的话用16进制编码
     * @param readListener 拼接器
     */
    public boolean startSerialPort(String port, boolean isAscii, ReadListener readListener) {
        return startSerialPort(port, 9600, 0, isAscii, readListener);
    }

    /**
     * 启动串口
     *
     * @param port         串口号
     * @param baudRate     波特率
     * @param flags        标记
     * @param isAscii      是否是Ascii编码，否的话用16进制编码
     * @param readListener 拼接器
     */
    public boolean startSerialPort(String port, int baudRate, int flags, boolean isAscii, ReadListener readListener) {

        boolean success = false;
        log(SerialPortManager.port, port, isAscii,
                new StringBuffer().append("波特率：")
                        .append(baudRate).append(" 标记位：").append(flags)
                        .append(" 启动串口"));
        try {
            InputStream inputStream;
            OutputStream outputStream;
            ReadThread readThread;
            android_serialport_api.SerialPort serialPort;
            if (serialPorts.containsKey(port)) {
                serialPort = (android_serialport_api.SerialPort)(serialPorts.get(port));
                inputStream = (InputStream) (inputStreams.get(port));
                readThread = new ReadThread(port, inputStream, isAscii, readListener);
                readThreads.put(port, readThread);
                readThread.start();
                success = true;
                log(SerialPortManager.port, port, isAscii, new StringBuffer().append("启动成功"));
            } else {
                serialPort = new android_serialport_api.SerialPort(new File(port), 9600, 0);
                if (serialPort == null) {
                    log(SerialPortManager.port, port, isAscii, new StringBuffer().append("启动失败：SerialPort == null"));
                } else {
                    serialPorts.put(port, serialPort);
                    inputStream = serialPort.getInputStream();
                    if (inputStream == null) {
                        throw new Exception("inputStream==null");
                    }
                    inputStreams.put(port, inputStream);
                    outputStream = serialPort.getOutputStream();
                    if (outputStream == null) {
                        throw new Exception("outputStream==null");
                    }
                    outputStreams.put(port, outputStream);
                    readThread = new ReadThread(port, inputStream, isAscii, readListener);
                    readThreads.put(port, readThread);
                    readThread.start();
                    success = true;
                    log(SerialPortManager.port, port, isAscii, new StringBuffer().append("启动成功"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log(SerialPortManager.port, port, isAscii, new StringBuffer().append("启动失败：").append(e));
            success = false;
        }
        return success;
    }

    public void setReadListener(@SerialPortManager.Port String port, ReadListener readListener) {
        if (readThreads.containsKey(port)) {
            ReadThread readThread = (ReadThread) (readThreads.get(port));
            readThread.setReadListener(readListener);
        }
    }

    class ReadThread extends Thread {

        public boolean isRun;
        public String port;
        private InputStream inputStream;
        public boolean isAscii;
        private ReadListener readListener;

        public ReadThread(String port, InputStream inputStream, boolean isAscii, ReadListener readListener) {
            this.port = port;
            this.inputStream = inputStream;
            this.isAscii = isAscii;
            this.readListener = readListener;
            if (readListener != null) {
                readListener.setLogInterceptor(logInterceptor);
            }
        }

        @Override
        public void run() {

            isRun = true;
            if (inputStream == null) {
                isRun = false;
                return;
            }
            while (isRun && !isInterrupted()) {
                try {
                    int size;
                    byte[] buffer = new byte[512];
                    size = inputStream.read(buffer);
                    if (readListener != null) {
                        if (size > 0) {
                            if (isAscii) {
                                readListener.onBaseRead(port, isAscii, new String(buffer, 0, size));
                            } else {
                                readListener.onBaseRead(port, isAscii, TransformUtils.bytes2HexString(buffer, size));
                            }
                        }
                    }
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopRead() {
            isRun = false;
        }

        public void setReadListener(ReadListener readListener) {
            this.readListener = readListener;
        }
    }

    public void stopSerialPort(String port) {

        if (readThreads.containsKey(port)) {
            ReadThread readThread = (ReadThread) (readThreads.get(port));
            log(SerialPortManager.port, port, true, "关闭串口");
            if (readThread != null) {
                readThread.stopRead();
                readThread.interrupt();
                readThreads.remove(port);
                log(SerialPortManager.port, port, readThread.isAscii, "关闭串口成功");
            }
        } else {
            log(SerialPortManager.port, port, false, "关闭串口失败：串口未开启");
        }
    }

    public void writeSerialService(String port, boolean isAscii, String cmd) throws Exception {
        log(SerialPortManager.write, port, isAscii, new StringBuffer().append("串口写：").append(cmd));
        if (outputStreams.containsKey(port)) {
            OutputStream outputStream = (OutputStream) (outputStreams.get(port));
            if (isAscii) {
                outputStream.write(cmd.getBytes());
            } else {
                outputStream.write(TransformUtils.hexStringToBytes(cmd));
            }
            log(SerialPortManager.write, port, isAscii, new StringBuffer().append("写成功：").append(cmd));
        } else {
            if (serialPorts.containsKey(port)) {
                log(SerialPortManager.write, port, isAscii, new StringBuffer().append("写失败：outputStream is null"));
                throw new Exception("outputStream is null");
            } else {
                log(SerialPortManager.write, port, isAscii, new StringBuffer().append("写失败：serialPorts is null"));
                throw new Exception("serialPorts is null");
            }

        }
    }

}
