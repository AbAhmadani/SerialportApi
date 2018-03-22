package com.hyperlcd.serialport;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private RadioButton asciiRB;
    private RadioButton hexRB;
    private RadioGroup serialRG;
    private RadioButton com0RB;
    private RadioButton s0RB;
    private RadioButton s1RB;
    private RadioButton s2RB;
    private RadioButton s3RB;
    private EditText sendET;
    private TextView readTV;

    private String currentPort;
    private SerialPort serialPort;
    private ReadListener readListener;
    private boolean isAsicc;
    private Button openBtn;

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
            public void log(@SerialPortManager.Type String type, @SerialPortManager.Port String port, boolean isAscii, String log) {
                Log.d("SerialPortLog", new StringBuffer()
                        .append("操作类型：").append(type)
                        .append("\n串口号：").append(port)
                        .append("\n数据格式：").append(isAscii ? "ascii" : "hexString")
                        .append("操作消息：").append(log).toString());
            }
        });
        serialPort = SerialPortManager.getInstances().getSerialPort();
        readListener = new ReadListener() {
            @Override
            public void onRead(@SerialPortManager.Port final String port, final boolean isAscii, final String read) {
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
        asciiRB = (RadioButton) findViewById(R.id.rb_ascii);
        hexRB = (RadioButton) findViewById(R.id.rb_hex);
        serialRG = (RadioGroup) findViewById(R.id.rg_serial);
        com0RB = (RadioButton) findViewById(R.id.rb_com0);
        s0RB = (RadioButton) findViewById(R.id.rb_s0);
        s1RB = (RadioButton) findViewById(R.id.rb_s1);
        s2RB = (RadioButton) findViewById(R.id.rb_s2);
        s3RB = (RadioButton) findViewById(R.id.rb_s3);

        openBtn = (Button) findViewById(R.id.btn_open);

        sendET = (EditText) findViewById(R.id.et_send);
        readTV = (TextView) findViewById(R.id.tv_read);

        serialRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                String checkPort;
                switch (checkedId) {
                    case R.id.rb_com0:
                        checkPort = SerialPortManager.ttyUSB;
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
                    default:
                        return;
                }
                if (!TextUtils.isEmpty(currentPort)
                        && TextUtils.equals(checkPort, currentPort)) {
                    openBtn.setText("关闭串口");
                } else {
                    openBtn.setText("打开串口");
                }
            }
        });

        openBtn.setOnClickListener(this);
        findViewById(R.id.clear_send).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.clear_read).setOnClickListener(this);

        asciiRB.setChecked(true);
        com0RB.setChecked(true);
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
            case R.id.btn_send:
                send();
                break;
            case R.id.clear_send:
                sendET.setText("");
                break;
            case R.id.clear_read:
                readTV.setText("");
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
            serialPort.writeSerialService(currentPort, isAsicc, send);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void open() {
        if (serialPort == null) {
            return;
        }
        String checkPort;
        switch (serialRG.getCheckedRadioButtonId()) {
            case R.id.rb_com0:
                checkPort = SerialPortManager.ttyUSB;
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
            default:
                return;
        }

        if (!TextUtils.isEmpty(currentPort)) {
            // 关闭currentPort串口
            serialPort.stopSerialPort(currentPort);
        }
        if (TextUtils.equals(currentPort, checkPort)) {
            openBtn.setText("打开串口");
            currentPort = "";
            return;
        }

        isAsicc = asciiRB.isChecked();
        currentPort = checkPort;
        // 打开checkPort串口
        serialPort.startSerialPort(checkPort, isAsicc, readListener);
        openBtn.setText("关闭串口");
    }


    private Toast toast;

    private void T(String message) {
        if (toast == null) {
            toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        }
        toast.setText(message);
        toast.show();
    }
}
