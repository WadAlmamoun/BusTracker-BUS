package net.net23.httpbustracker.bustracker_bus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static net.net23.httpbustracker.bustracker_bus.R.id.user_name;

public class LoginActivity extends AppCompatActivity {
    Button OpenRegister;
    EditText et_login_username,et_login_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

        et_login_username = (EditText) findViewById(user_name);
        et_login_password = (EditText) findViewById(R.id.user_pass);


        OpenRegister = (Button) findViewById(R.id.go_to_register);
        OpenRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });
        }
        else
        {
            AlertDialog.Builder builder  = new AlertDialog.Builder(this);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            builder.setTitle("Error!");
            builder.setMessage("This Application Cannot Function Without Internet Connection");
            builder.show();
        }

    }

    public void userLogin (View v)
    {
        String username = et_login_username.getText().toString();
        String password = et_login_password.getText().toString();

        if (username.isEmpty()|| password.isEmpty())
        {
            AlertDialog.Builder builder  = new AlertDialog.Builder(this);
            builder.setPositiveButton("OK",null);
            builder.setTitle("Error!");
            builder.setMessage("Please Fill All the Fields!");
            builder.show();
        }
        else
        {
            String method = "login";
            BackgroundLogReg backgroundLogReg = new BackgroundLogReg(LoginActivity.this);
            backgroundLogReg.execute(method, username, password);
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
