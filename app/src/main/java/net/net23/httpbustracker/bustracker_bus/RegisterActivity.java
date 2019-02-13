package net.net23.httpbustracker.bustracker_bus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class RegisterActivity extends AppCompatActivity {
    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    EditText etUsername, etFname, etLname, etPassword, etConfirmPassword;
    Button btnSignup;
    String route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       //////////////////////////////////////////////////////////////////////////////////////
       //SPINNER FOR ROUTE SELECTION:
        spinner = (Spinner) findViewById(R.id.route_spinner);
        adapter = ArrayAdapter.createFromResource(this,R.array.routes,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                {
                    route = "";
                }
                else if(position == 1)
                {
                  route = "ArabiMamora";
                }
                else if (position == 2)
                {
                    route = "ArabiJabra";
                }
                else if (position == 3)
                {
                    route = "ArabiMarkazi";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //////////////////////////////////////////////////////////////////////////////////////


        btnSignup =(Button)findViewById(R.id.sign_up_button);
        etUsername = (EditText) findViewById(R.id.new_username);
        etFname = (EditText) findViewById(R.id.fname);
        etLname = (EditText)findViewById(R.id.lname);
        etPassword = (EditText) findViewById(R.id.new_password);
        etConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(etPassword.getText().length()<6)
                {
                    etPassword.setError("Password must be at least 6 characters.");
                    // btnSignup.setEnabled(false);
                }
            }
        });
    }

    public void sign_up_btn(View v) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            String username = etUsername.getText().toString();
            String fname = etFname.getText().toString();
            String lname = etLname.getText().toString();
            String name = etFname.getText().toString() + " " + etLname.getText().toString();
            String password = etPassword.getText().toString();
            String confirmpassword = etConfirmPassword.getText().toString();

            if (username.isEmpty() || fname.isEmpty() || lname.isEmpty() || password.isEmpty() || confirmpassword.isEmpty() ) {
                AlertDialog.Builder builder  = new AlertDialog.Builder(this);
                builder.setPositiveButton("OK",null);
                builder.setTitle("Error!");
                builder.setMessage("Please Fill All the Fields!");
                builder.show();
                etPassword.setText("");
                etConfirmPassword.setText("");
            }
            if (!password.equals(confirmpassword)) {
                AlertDialog.Builder builder  = new AlertDialog.Builder(this);
                builder.setPositiveButton("OK",null);
                builder.setTitle("Error!");
                builder.setMessage("Passwords Don't Match");
                builder.show();
                etPassword.setText("");
                etConfirmPassword.setText("");
            }
            else if (route.isEmpty())
            {
                AlertDialog.Builder builder  = new AlertDialog.Builder(this);
                builder.setPositiveButton("OK",null);
                builder.setTitle("Error!");
                builder.setMessage("Please Choose a Route");
                builder.show();
            }
            else
            {
                String method = "register";
                BackgroundLogReg backgroundLogReg = new BackgroundLogReg(RegisterActivity.this);
                backgroundLogReg.execute(method, name, username, password,route);
            }
        }
        else
        {
            AlertDialog.Builder builder  = new AlertDialog.Builder(this);
            builder.setPositiveButton("OK",null);
            builder.setTitle("Error!");
            builder.setMessage("You are not Connected to the internet");
            builder.show();
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
