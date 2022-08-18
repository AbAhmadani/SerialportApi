package android_serialport_api.hyperlcd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ADan on 2017/9/16.
 */

class SerialPort {

    private android_serialport_api.SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ReadThread readThread;
    private String port;
    private boolean open;
    private boolean isAscii;
    private int baudRate;
    private int flags;
    private LogInterceptorSerialPort logInterceptor;

    public SerialPort(String port, boolean isAscii, int baudRate, int flags) {
        this.port = port;
        this.isAscii = isAscii;
        this.baudRate = baudRate;
        this.flags = flags;
    }

    public void setLogInterceptor(LogInterceptorSerialPort logInterceptor) {
        this.logInterceptor = logInterceptor;
    }

    private void log(@SerialPortManager.Type String type, String port, boolean isAscii, CharSequence log) {
        log(type, port, isAscii, log == null ? "null" : log.toString());
    }

    private void log(@SerialPortManager.Type String type, String port, boolean isAscii, String log) {
        if (logInterceptor != null) {
            logInterceptor.log(type, port, isAscii, log);
        }
    }

    /**
     * Start the serial port, the default baud rate is 9600
     *
     * @param reader
     */
    public boolean open(BaseReader reader) {
        log(SerialPortManager.port, port, isAscii,
                new StringBuffer().append("Baud rate ")
                        .append(baudRate).append(" Mark bit ").append(flags)
                        .append(" Start the serial port"));
        if (open) {
            log(SerialPortManager.port, port, isAscii, new StringBuffer().append("Startup failed: the serial port has been started"));
            return open;
        }
        try {
            serialPort = new android_serialport_api.SerialPort(new File(port), baudRate, 0);
            if (serialPort == null) {
                log(SerialPortManager.port, port, isAscii, new StringBuffer().append("Startup failed: SerialPort == null"));
            } else {
                inputStream = serialPort.getInputStream();
                if (inputStream == null) {
                    throw new Exception("inputStream==null");
                }
                outputStream = serialPort.getOutputStream();
                if (outputStream == null) {
                    throw new Exception("outputStream==null");
                }
                readThread = new ReadThread(isAscii, reader);
                readThread.start();
                open = true;
                log(SerialPortManager.port, port, isAscii, new StringBuffer().append("Successfully started"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log(SerialPortManager.port, port, isAscii, new StringBuffer().append("Startup failed: ").append(e));
            open = false;
        }
        return open;
    }

    public boolean isOpen() {
        return open;
    }

    public void setReadCode(boolean isAscii) {
        if (readThread != null) {
            readThread.isAscii = isAscii;
            log(SerialPortManager.port, port, readThread.isAscii, new StringBuffer().append("Modify data format: ").append(isAscii ? "ASCII" : "HexString"));
        }
    }

    public void setReader(BaseReader reader) {
        if (readThread != null) {
            readThread.setReader(reader);
        }
    }

    class ReadThread extends Thread {

        public boolean isRun;
        public boolean isAscii;
        private BaseReader reader;

        public ReadThread(boolean isAscii, BaseReader baseReader) {
            reader = baseReader;
            this.isAscii = isAscii;
            if (reader != null) {
                reader.setLogInterceptor(logInterceptor);
            }
        }

        @Override
        public void run() {
            if (inputStream == null) {
                return;
            }
            isRun = true;
            while (isRun && !isInterrupted()) {
                try {
                    // Prevent thread IO blocking
                    if (inputStream.available() > 0) {

                        int size;
                        byte[] buffer = new byte[512];
                        /**
                         * When the data cannot be read, the method will wait until the data is read
                         * The thread cannot be interrupted when it is in the io blocking state. Even if it is interrupted, when there is data coming, the program will execute to the interrupt mark, so
                         */
                        size = inputStream.read(buffer);
                        if (!isRun) {
                            break;
                        }
                        if (reader != null) {
                            if (size > 0) {
                                reader.onBaseRead(port, isAscii, buffer, size);
                            }
                        }
                    }
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log(SerialPortManager.port, port, isAscii, "Thread terminated successfully released resources");
        }

        public void stopRead() {
            isRun = false;
        }

        public void setReader(BaseReader baseReader) {
            reader = baseReader;
            if (reader != null) {
                reader.setLogInterceptor(logInterceptor);
            }
        }
    }

    public void write(String cmd) {
        write(isAscii, cmd);
    }

    public void write(boolean isAscii, String cmd) {
        log(SerialPortManager.write, port, isAscii, new StringBuffer().append("Write: ").append(cmd));
        if (outputStream != null) {
            synchronized (outputStream) {
                byte[] bytes;
                try {
                    if (isAscii) {
                        bytes = cmd.getBytes();
                    } else {
                        bytes = TransformUtils.hexStringToBytes(cmd);
                    }
                    outputStream.write(bytes);
                } catch (Exception e) {
                    log(SerialPortManager.write, port, isAscii, new StringBuffer().append("Write failure:").append(e));
                }
            }
            log(SerialPortManager.write, port, isAscii, new StringBuffer().append("Written successfully: ").append(cmd));
        } else {
            log(SerialPortManager.write, port, isAscii, new StringBuffer().append("Write failure: outputStream is null"));
        }
    }

    public void close() {
        try {
            open = false;
            if (readThread != null) {
                readThread.stopRead();
                log(SerialPortManager.port, port, readThread.isAscii, "Close the serial port successfully");
            } else {
                log(SerialPortManager.port, port, false, "Failed to close the serial port: the serial port is not open");
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (serialPort != null) {
                serialPort.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
