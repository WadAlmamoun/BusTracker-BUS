package net.net23.httpbustracker.bustracker_bus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Hamid on 11/28/2016.
 */

public class BackgroundLogReg extends AsyncTask<String,Void,String> {
    SharedPreferences Data;
    SharedPreferences.Editor editor;
    String reg_url = "http://bustrackersudan.net16.net/bus_register.php";
    String login_url = "http://bustrackersudan.net16.net/bus_login.php";
    Activity activity;
    Context ctx;
    ProgressDialog progressDialog;
    AlertDialog.Builder builder;
    BackgroundLogReg(Context ctx)
    {
        this.ctx = ctx;
        activity = (Activity) ctx;
    }

    public BackgroundLogReg asyncObject;
    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Connecting to server...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        asyncObject = this;
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                // You can monitor the progress here as well by changing the onTick() time

            }
            public void onFinish() {
                // stop async task if not in progress
                if (asyncObject.getStatus() == AsyncTask.Status.RUNNING) {
                    progressDialog.dismiss();
                    asyncObject.cancel(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setPositiveButton("OK", null);
                    builder.setTitle("Error!");
                    builder.setMessage("Slow Internet connection, please try again");
                    builder.show();
                }
            }
        }.start();
    }


    @Override
    protected String doInBackground(String... params) {
        Data = ctx.getSharedPreferences("MYDATA", Context.MODE_PRIVATE);
        editor = Data.edit();

        String method = params[0];
        if (method.equals("register")) {
            try {
                URL url = new URL(reg_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(OS, "UTF-8"));
                String name = params[1];
                String username = params[2];
                String password = params[3];
                String route = params [4];

                String data =  URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8") + "&" +
                        URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") + "&" +
                        URLEncoder.encode("route", "UTF-8") + "=" + URLEncoder.encode(route, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                OS.close();

                InputStream IS = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(IS));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = bufferedReader.readLine())!= null)
                {
                    stringBuilder.append(line+"\n");
                }
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(method.equals("login"))
        {

            try {
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String username = params[1];
                String password = params[2];
                String data = URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"+
                        URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = bufferedReader.readLine())!= null)
                {
                    stringBuilder.append(line+"\n");
                }
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String json) {
        try {
            progressDialog.dismiss();
            JSONObject jsonObject = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
            JSONObject JO = jsonArray.getJSONObject(0);
            String code = JO.getString("code");
            String message = JO.getString("message");

            if(code.equals("reg_success"))
            {
                AlertDialog.Builder builder  = new AlertDialog.Builder(activity);
                builder.setTitle("Registered Successfully");
                builder.setMessage(message);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.finish();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            else if(code.equals("reg_failed"))
            {
                AlertDialog.Builder builder  = new AlertDialog.Builder(activity);
                builder.setTitle("Failed to Register");
                builder.setMessage(message);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = (EditText) activity.findViewById(R.id.new_username);
                        editText.setText("");
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            else if (code.equals("login_success"))
            {
                String idbus = JO.getString("idbus");
                String name = JO.getString("name");
                String credit = JO.getString("credit");
                String route = JO.getString("route");
                Intent intent = new Intent(activity,JourneyActivity.class);
                editor.putString("BusID",idbus).commit();
                editor.putString("Name",name).commit();
                editor.putString("Credit",credit).commit();
                editor.putString("Route",route).commit();
                editor.putBoolean("login",true).commit();
                activity.startActivity(intent);
                activity.finish();
            }
            else if (code.equals("login_failed"))
            {
                AlertDialog.Builder builder  = new AlertDialog.Builder(activity);
                builder.setTitle("Failed to Log In");
                builder.setMessage(message);
                builder.setPositiveButton("OK", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
