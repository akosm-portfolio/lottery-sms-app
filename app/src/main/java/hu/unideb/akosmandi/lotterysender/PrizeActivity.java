package hu.unideb.akosmandi.lotterysender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

public class PrizeActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private EditText nameEditText;
    private EditText accountNumberEditText;
    private TextView errorMsgTv;
    private String msgToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prize);

        nameEditText = findViewById(R.id.editTextPersonName);
        accountNumberEditText = findViewById(R.id.editTextAccountNumber);
        errorMsgTv = findViewById(R.id.errorMsgTv);

        accountNumberEditText.addTextChangedListener(onTextChangedListener());


    }

    public void SendPrizeBtnClicked(View view) {
        if (nameEditText.getText() != null && accountNumberEditText.getText() != null &&
                (accountNumberEditText.getText().length() == 17 || accountNumberEditText.getText().length() == 26)) {
            errorMsgTv.setText("");
            StringBuilder sb = new StringBuilder();
            sb.append("UTAL*");
            sb.append(nameEditText.getText());
            sb.append("*");
            sb.append(accountNumberEditText.getText());
            if(accountNumberEditText.getText().length()==17){
                sb.append("-00000000");
            }
            sb.append("*");

            msgToSend = sb.toString();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);

            } else {
                SendSms();
            }

        }else{
            errorMsgTv.setText(R.string.incorrect_data);
        }
    }


    private TextWatcher onTextChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                char space = '-';
                // Remove spacing char
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if (space == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert char where needed.
                if (s.length() > 0 && (s.length() % 9) == 0) {
                    char c = s.charAt(s.length() - 1);
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 7) {
                        s.insert(s.length() - 1, String.valueOf(space));
                    }
                }
                //accountNumberEditText.removeTextChangedListener(this);
            }
        };
    }

    public void BackBtnClicked(View view) {
        finish();
    }

    public void SendSms(){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("1756", null,
                    msgToSend, null, null);
            Log.d("SENT", "Message: \"" + msgToSend +"\"");
            Toast.makeText(this, R.string.successful_send, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, R.string.failed_to_send, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (permissions[0].equalsIgnoreCase
                        (Manifest.permission.SEND_SMS)
                        && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    SendSms();
                } else {
                Toast.makeText(this, R.string.failed_to_send, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}