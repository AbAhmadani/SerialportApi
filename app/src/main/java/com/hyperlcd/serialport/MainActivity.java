package com.hyperlcd.serialport;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import android_serialport_api.hyperlcd.BaseReader;
import android_serialport_api.hyperlcd.LogInterceptorSerialPort;
import android_serialport_api.hyperlcd.SerialPortManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioGroup codeRG;
    private RadioGroup serialRG;
    private EditText serialET;
    private EditText sendET;
    private Spinner spinner_baudrate;
    private TextView readTV;
    private TextView logTV;
    private TextView serialTitle;
    private TextView codeTitle;

    private String currentPort;
    private BaseReader baseReader;
    private SerialPortManager spManager;
    ArrayList<String> baudratelist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        spManager = SerialPortManager.getInstances().setLogInterceptor(new LogInterceptorSerialPort() {
            @Override
            public void log(@SerialPortManager.Type final String type, final String port, final boolean isAscii, final String log) {
                Log.d("SerialPortLog", new StringBuffer()
                        .append("Serial port number ").append(port)
                        .append("\nData Format ").append(isAscii ? "ascii" : "hexString")
                        .append("\nOperation type ").append(type)
                        .append("Operation message ").append(log).toString());
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
        baseReader = new BaseReader() {
            @Override
            protected void onParse(final String port, final boolean isAscii, final String read) {
                System.out.println("here");
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
        spinner_baudrate = (Spinner) findViewById(R.id.spinner_baudrate);
        baudratelist.add("110");
        baudratelist.add("300");
        baudratelist.add("600");
        baudratelist.add("1200");
        baudratelist.add("2400");
        baudratelist.add("4800");
        baudratelist.add("9600");
        baudratelist.add("14400");
        baudratelist.add("19200");
        baudratelist.add("38400");
        baudratelist.add("57600");
        baudratelist.add("115200");
        baudratelist.add("128000");
        baudratelist.add("256000");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, baudratelist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_baudrate.setAdapter(dataAdapter);
        spinner_baudrate.setSelection(6);
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
        spManager.destroy();
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
        if (TextUtils.isEmpty(currentPort)) {
            // The serial port is not open
            T("The serial port is not open");
            return;
        }

        String send = sendET.getText().toString().trim();
        if (TextUtils.isEmpty(send)) {
            // Send data is empty
            T("Send data is empty");
            return;
        }
        // send data
        spManager.send(currentPort, send);
    }

    /**
     * Open the serial port
     */
    private void open() {
        String checkPort = getCurrentPort();
        if (TextUtils.isEmpty(checkPort)) {
            return;
        } else if (TextUtils.equals(checkPort, "other")) {
            checkPort = serialET.getText().toString().trim();
            if (TextUtils.isEmpty(checkPort)) {
                T("Please enter the serial port number");
                return;
            }
        }

        if (TextUtils.equals(currentPort, checkPort)) {
            return;
        }

        if (!TextUtils.isEmpty(currentPort)) {
            // Close currentPort serial port
            spManager.stopSerialPort(currentPort);
        }
        boolean isAscii = codeRG.getCheckedRadioButtonId() == R.id.rb_ascii;
        currentPort = checkPort;
        // Close currentPort serial port
        spManager.startSerialPort(checkPort, Integer.parseInt(spinner_baudrate.getSelectedItem().toString()), isAscii, baseReader);
        spManager.setReader(checkPort, baseReader);
        serialTitle.setText("Serial port ");
        serialTitle.append(currentPort);
        codeTitle.setText("Data Format ");
        codeTitle.append(isAscii ? "ASCII" : "HexString");
    }

    /**
     * Close the serial port
     */
    private void close() {
        if (!TextUtils.isEmpty(currentPort)) {
            // Close currentPort serial port
            spManager.stopSerialPort(currentPort);
            currentPort = "";
            serialTitle.setText("Serial port");
            codeTitle.setText("Data Format");
        }
    }

    /**
     * Change the data format
     *
     * @param isAscii true:ascii false:HexString
     */
    private void changeCode(boolean isAscii) {
        if (TextUtils.isEmpty(currentPort)) {
            return;
        }
        spManager.setReadCode(currentPort, isAscii);
        codeTitle.setText("Data Format ");
        codeTitle.append(isAscii ? "ASCII" : "HexString");
    }

    /**
     * Get the selected serial port number
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
            case R.id.rb_gs0:
                checkPort = SerialPortManager.ttyGS0;
                break;
            case R.id.rb_gs1:
                checkPort = SerialPortManager.ttyGS1;
                break;
            case R.id.rb_gs2:
                checkPort = SerialPortManager.ttyGS2;
                break;
            case R.id.rb_gs3:
                checkPort = SerialPortManager.ttyGS3;
                break;
            case R.id.rb_other:
                checkPort = "other";
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
