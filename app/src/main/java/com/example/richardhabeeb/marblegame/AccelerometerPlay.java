package com.example.richardhabeeb.marblegame;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.SensorManager;
import android.view.Window;
import android.view.WindowManager;


public class AccelerometerPlay extends Activity {
    private Game gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        gameView = new Game(this,
                            (WindowManager) getSystemService(WINDOW_SERVICE),
                            (SensorManager) getSystemService(SENSOR_SERVICE));
        setContentView(gameView);
    }

}
