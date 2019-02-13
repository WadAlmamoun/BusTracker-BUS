package net.net23.httpbustracker.bustracker_bus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartScreen extends AppCompatActivity {
    SharedPreferences Data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(1500);
                    Data = getSharedPreferences("MYDATA", Context.MODE_PRIVATE);
                    Boolean userStatus = Data.getBoolean("login",false);
                    Boolean journeyStatus = Data.getBoolean("JourneyStart",false);
                    if (userStatus == true)
                    {if (journeyStatus == true){
                        startActivity(new Intent(StartScreen.this,MainActivity.class));
                        finish();
                    }
                    else
                    {
                        startActivity(new Intent(StartScreen.this,JourneyActivity.class));
                        finish();
                    }
                    }
                    else{
                        startActivity(new Intent(StartScreen.this,LoginActivity.class));
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
