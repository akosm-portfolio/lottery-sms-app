package hu.unideb.akosmandi.lotterysender;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class NumberPickActivity extends AppCompatActivity
        implements SensorEventListener {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final String SENT_SMS_FLAG = BuildConfig.APPLICATION_ID + ".SENT_SMS_FLAG";


    private TextView lotteryNameTv;
    private TextView errorMsgTv;
    private EditText numbersEditText;
    private Button modifyButton;
    private String LotteryButtonName;
    private String msgToSend;

    private ArrayList<Integer> numbersIntList = new ArrayList<>();
    private int min, max;
    private int lotteryTypeInt = 0;

    private static final float SHAKE_THRESHOLD = 15f;
    private static final int MIN_TIME_BETWEEN_SHAKES = 2000;
    private long mLastShakeTime;
    private SensorManager mSensorMgr;
    private Vibrator vibratorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.numberpick_show);

        LotteryButtonName = getIntent().getStringExtra("LOTTERY_NAME");

        lotteryNameTv = findViewById(R.id.lottery_name_tv);
        errorMsgTv = findViewById(R.id.errorMsgTv);
        //numbersHeadTv.setVisibility(View.INVISIBLE);
        numbersEditText = findViewById(R.id.editText_numbers);
        modifyButton = (Button) findViewById(R.id.modify_btn);
        modifyButton.setEnabled(false);

        lotteryTypeInt = getFieldNumber(LotteryButtonName);
        lotteryNameTv.setText(LotteryButtonName);
        min = getMinMax(lotteryTypeInt)[0];
        max = getMinMax(lotteryTypeInt)[1];
        //numbersEditText.setVisibility(View.INVISIBLE);

        mSensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibratorService = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Sensor accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            //Log.e("sensor", "onSensorChanged:" + curTime);
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    Toast.makeText(this, R.string.shake_detected, Toast.LENGTH_SHORT).show();

                    numbersIntList = new ArrayList<Integer>();
                    StringJoiner sj = new StringJoiner(",");

                    for (int i = 0; i < lotteryTypeInt; i++) {
                        int random = (int) (Math.random() * (max - min + 1) + min);
                        if (numbersIntList.isEmpty()) {
                            random = (int) (Math.random() * (max - min + 1) + min);
                            Log.d("RANDOM", "Empty");
                        } else {
                            while (numbersIntList.contains(random)) {
                                random = (int) (Math.random() * (max - min + 1) + min);
                                Log.d("RANDOM", "Generating new random number as it was the same; SIZE:" + numbersIntList.size());
                            }
                        }
                        numbersIntList.add(random);
                        sj.add(String.valueOf(random));
                    }
                    numbersIntList.clear();

                    //numbersEditText.setVisibility(View.VISIBLE);
                    //numbersHeadTv.setVisibility(View.VISIBLE);
                    //numbersEditText.setFocusable(false);
                    modifyButton.setEnabled(true);

                    numbersEditText.setText(sj.toString());
                    vibratorService.vibrate(300);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int getFieldNumber(String buttonText) {
        switch (buttonText) {
            case "Ötöslottó":
                return 5;
            case "Hatoslottó":
                return 6;
            case "Skandináv lottó":
                return 7;
        }
        return 0;
    }

    private int[] getMinMax(int num) {
        switch (num) {
            case 5:
                return new int[]{1, 90};
            case 6:
                return new int[]{1, 45};
            case 7:
                return new int[]{1, 35};
        }
        return new int[]{0, 0};
    }

    public void sendButtonPushed(View view) {

        String numbersString = numbersEditText.getText().toString();
        String[] numbersStringArray;
        Boolean accept = false;
        Boolean unknownErr = false;

        numbersStringArray = numbersString.split(",");
        if (numbersString.isEmpty()) {
            errorMsgTv.setText(R.string.notify_to_shake);
        } else if (numbersStringArray.length != lotteryTypeInt) {
            errorMsgTv.setText(String.format(getResources().getString(R.string.length_error_msg), lotteryTypeInt));
            accept = false;
        } else {
            Set<String> numbersStringSet = Arrays.stream(numbersStringArray).collect(Collectors.toSet());
            if(numbersStringArray.length == numbersStringSet.size()){
                for (String s : numbersStringArray) {
                    try {
                        int nextNum = Integer.parseInt(s);
                        if (nextNum < min || nextNum > max) {
                            errorMsgTv.setText(String.format("%s: %d-%d", getResources().getString(R.string.allowed_range), min, max));
                            accept = false;
                        } else {
                            accept = true;
                        }
                    } catch (Exception e) {
                        errorMsgTv.setText(R.string.comma_error_msg);
                        accept = false;
                    }
                }
            }else{
                accept = false;
                errorMsgTv.setText(R.string.same_number_twice);
            }

        }

        if (accept) {
            errorMsgTv.setText("");
            StringBuilder sbMessage = new StringBuilder();
            sbMessage.append("L");
            switch (lotteryTypeInt) {
                case (5):
                    sbMessage.append("5");
                    break;
                case (6):
                    sbMessage.append("6");
                    break;
                case (7):
                    sbMessage.append("S");
                    break;
                default:
                    errorMsgTv.setText(R.string.unknown_error);
                    unknownErr = true;
            }
            if (!unknownErr) {
                sbMessage.append("#");
                StringJoiner sj = new StringJoiner("#");
                for (int i = 0; i < numbersStringArray.length; i++) {
                    sj.add(numbersStringArray[i]);
                }
                sbMessage.append(sj.toString());
                msgToSend = sbMessage.toString();

                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    SendSms();
                }
            }
        }
    }


    public void modifyButtonPushed(View view) {
        numbersEditText.setFocusable(true);
        numbersEditText.setFocusableInTouchMode(true);
        numbersEditText.setSelection(numbersEditText.getText().length());
        numbersEditText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(numbersEditText, InputMethodManager.SHOW_IMPLICIT);

    }

    public void SendSms() {

        try {
            SmsManager smsManager = SmsManager.getDefault();
            Intent sentIn = new Intent(SENT_SMS_FLAG);
            PendingIntent sentPIn = PendingIntent.getBroadcast(this, 0, sentIn, 0);

            smsManager.sendTextMessage(getResources().getString(R.string.sms_destination_number), null,
                    msgToSend, sentPIn, null);
            BroadcastReceiver sentReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent in) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            //sent SMS message successfully;
                            Log.d("SENT", "Message: \"" + msgToSend +"\"");

                            StartSuccessActivity();
                            break;
                        default:
                            //sent SMS message failed
                            Toast.makeText(c, R.string.failed_to_send, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };
            registerReceiver(sentReceiver, new IntentFilter(SENT_SMS_FLAG));
        } catch (Exception e) {
            Toast.makeText(this, R.string.failed_to_send, Toast.LENGTH_SHORT).show();
        }
    }

    private void StartSuccessActivity() {
        Intent intent = new Intent(this, SuccessActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (permissions[0].equalsIgnoreCase
                        (Manifest.permission.SEND_SMS)){
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        SendSms();
                    } else {
                        Toast.makeText(this, R.string.failed_to_send, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
