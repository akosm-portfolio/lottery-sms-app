package hu.unideb.akosmandi.lotterysender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void BtnLotteryClicked(View view) {
        Button btn = (Button) view;
        Intent intent = new Intent(this, NumberPickActivity.class);

        intent.putExtra("LOTTERY_NAME", btn.getText().toString());
        startActivity(intent);

    }

    public void BtnGetPrizeClicked(View view) {
        Intent intent = new Intent(this, PrizeActivity.class);
        startActivity(intent);
    }

    public void BtnTicketClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getResources().getString(R.string.url_scratch_card)));
        startActivity(intent);
    }

}