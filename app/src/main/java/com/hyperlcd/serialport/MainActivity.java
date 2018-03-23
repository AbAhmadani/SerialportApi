package com.hyperlcd.serialport;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import android_serialport_api.hyperlcd.LogInterceptorSerialPort;
import android_serialport_api.hyperlcd.ReadListener;
import android_serialport_api.hyperlcd.SerialPort;
import android_serialport_api.hyperlcd.SerialPortManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioGroup codeRG;
    private RadioGroup serialRG;
    private EditText serialET;
    private EditText sendET;
    private TextView readTV;
    private TextView logTV;
    private TextView serialTitle;
    private TextView codeTitle;

    private String currentPort;
    private SerialPort serialPort;
    private ReadListener readListener;
    private boolean isAscii;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initData();
    }

    private void initData() {
        SerialPortManager.getInstances().initSerialPort();
        SerialPortManager.getInstances().setLogInterceptor(new LogInterceptorSerialPort() {
            @Override
            public void log(@SerialPortManager.Type final String type, final String port, final boolean isAscii, final String log) {
                Log.d("SerialPortLog", new StringBuffer()
                        .append("串口号：").append(port)
                        .append("\n数据格式：").append(isAscii ? "ascii" : "hexString")
                        .append("\n操作类型：").append(type)
                        .append("操作消息：").append(log).toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logTV.append(new StringBuffer()
                                .append(" ").append(port)
                                .append(" ").append(isAscii ? "ascii" : "hexString")
                                .append(" ").append(type)
                                .append("：").append(log)
                                .append("\n").toString());
                    }
                });
            }
        });

        serialPort = SerialPortManager.getInstances().getSerialPort();
        readListener = new ReadListener() {
            @Override
            public void onRead(final String port, final boolean isAscii, final String read) {
                Log.d("SerialPortRead", new StringBuffer()
                        .append(port).append("/").append(isAscii ? "ascii" : "hex")
                        .append(" read：").append(read).append("\n").toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readTV.append(new StringBuffer()
                                .append(port).append("/").append(isAscii ? "ascii" : "hex")
                                .append(" read：").append(read).append("\n").toString());
                    }
                });
            }
        };
    }


    private void initView() {
        codeRG = (RadioGroup) findViewById(R.id.rg_code);
        serialRG = (RadioGroup) findViewById(R.id.rg_serial);

        serialET = (EditText) findViewById(R.id.et_serial);

        sendET = (EditText) findViewById(R.id.et_send);
        serialTitle = (TextView) findViewById(R.id.title_serial);
        codeTitle = (TextView) findViewById(R.id.title_code);
        readTV = (TextView) findViewById(R.id.tv_read);
        logTV = (TextView) findViewById(R.id.tv_log);

        codeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                changeCode(checkedId == R.id.rb_ascii);
            }
        });

        serialRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.rb_other) {
                    serialET.requestFocus();
                } else {
                    sendET.requestFocus();
                }
            }
        });

        serialET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    serialRG.check(R.id.rb_other);
                }
            }
        });

        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.clear_send).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.clear_read).setOnClickListener(this);
        findViewById(R.id.clear_log).setOnClickListener(this);

        codeRG.check(R.id.rb_ascii);
        serialRG.check(R.id.rb_com0);
        sendET.requestFocus();
    }

    @Override
    protected void onDestroy() {
        SerialPortManager.getInstances().destroySerialPort();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open:
                open();
                break;
            case R.id.btn_close:
                close();
                break;
            case R.id.btn_send:
                send();
                break;
            case R.id.clear_send:
                sendET.setText("");
                break;
            case R.id.clear_read:
                readTV.setText("");
                break;
            case R.id.clear_log:
                logTV.setText("");
                break;
            default:
        }
    }

    private void send() {
        if (serialPort == null) {
            // 串口未初始化
            T("串口未初始化");
            return;
        }

        if (TextUtils.isEmpty(currentPort)) {
            // 串口未打开
            T("串口未打开");
            return;
        }

        String send = sendET.getText().toString().trim();
        if (TextUtils.isEmpty(send)) {
            // 发送数据为空
            T("发送数据为空");
            return;
        }
        // 发送数据
        try {
            serialPort.writeSerialService(currentPort, isAscii, send);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开串口
     */
    private void open() {
        if (serialPort == null) {
            return;
        }
        String checkPort = getCurrentPort();
        if (TextUtils.isEmpty(checkPort)) {
            return;
        } else if (TextUtils.equals(checkPort, SerialPortManager.other)) {
            checkPort = serialET.getText().toString().trim();
            if (TextUtils.isEmpty(checkPort)) {
                T("请输入串口号");
                return;
            }
        }

        if (TextUtils.equals(currentPort, checkPort)) {
            return;
        }

        if (!TextUtils.isEmpty(currentPort)) {
            // 关闭currentPort串口
            serialPort.stopSerialPort(currentPort);
        }

        isAscii = codeRG.getCheckedRadioButtonId() == R.id.rb_ascii;
        currentPort = checkPort;
        // 打开checkPort串口
        serialPort.startSerialPort(checkPort, isAscii, readListener);

        serialTitle.setText("串口：");
        serialTitle.append(currentPort);
        codeTitle.setText("数据格式：");
        codeTitle.append(isAscii ? "ASCII" : "HexString");
    }

    /**
     * 关闭串口
     */
    private void close() {
        if (!TextUtils.isEmpty(currentPort)) {
            // 关闭currentPort串口
            serialPort.stopSerialPort(currentPort);
            currentPort = "";
            serialTitle.setText("串口");
            codeTitle.setText("数据格式");
        }
    }

    /**
     * 更改数据格式
     *
     * @param isAscii true:ascii false:HexString
     */
    private void changeCode(boolean isAscii) {
        if (TextUtils.isEmpty(currentPort)) {
            return;
        }
        serialPort.setReadCode(currentPort, isAscii);
        codeTitle.setText("数据格式：");
        codeTitle.append(isAscii ? "ASCII" : "HexString");
    }

    /**
     * 获取选中的串口号
     *
     * @return
     */
    private String getCurrentPort() {
        String checkPort;
        switch (serialRG.getCheckedRadioButtonId()) {
            case R.id.rb_com0:
                checkPort = SerialPortManager.ttyCOM0;
                break;
            case R.id.rb_com1:
                checkPort = SerialPortManager.ttyCOM1;
                break;
            case R.id.rb_com2:
                checkPort = SerialPortManager.ttyCOM2;
                break;
            case R.id.rb_com3:
                checkPort = SerialPortManager.ttyCOM3;
                break;
            case R.id.rb_s0:
                checkPort = SerialPortManager.ttyS0;
                break;
            case R.id.rb_s1:
                checkPort = SerialPortManager.ttyS1;
                break;
            case R.id.rb_s2:
                checkPort = SerialPortManager.ttyS2;
                break;
            case R.id.rb_s3:
                checkPort = SerialPortManager.ttyS3;
                break;
            case R.id.rb_other:
                checkPort = SerialPortManager.other;
                break;
            default:
                checkPort = "";
        }
        return checkPort;
    }

    private Toast toast;
    private TextView textView;

    private void T(String message) {
        if (toast == null) {
            toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
            textView = new TextView(this);
            textView.setTextColor(0xffffffff);
            textView.setTextSize(30);
            textView.setPadding(10, 5, 10, 5);
            textView.setBackgroundResource(R.drawable.shape_toast_bg);
            toast.setView(textView);
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        textView.setText(message);
        toast.show();
    }
}
