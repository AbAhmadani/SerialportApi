package android_serialport_api.hyperlcd;

/**
 * Created by ADan on 2018/3/1.
 * 串口日志拦截器
 */

public abstract class LogInterceptorSerialPort {

    /**
     * 操作日志输出回调
     *
     * @param type    日志类型
     * @param port    串口
     * @param isAscii true:ASCII编码 false:十六进制编码
     * @param log     日志内容
     */
    public abstract void log(@SerialPortManager.Type String type, String port, boolean isAscii, String log);

}
