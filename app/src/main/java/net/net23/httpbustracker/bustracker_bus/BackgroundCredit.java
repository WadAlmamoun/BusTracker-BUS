package net.net23.httpbustracker.bustracker_bus;

/**
 * Created by Hamid on 11/30/2016.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.widget.TextView;

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

public class BackgroundCredit extends AsyncTask<String,Void,String> {
    SharedPreferences Data;
    SharedPreferences.Editor editor;
    String transferC_url = "http://bustrackersudan.net16.net/bus_credit_trans.php";
//    String transferC_url = "http://bustracker.net23.net/bus_credit_trans.php";
    Activity activity;
    Context ctx;
    ProgressDialog progressDialog;
    BackgroundCredit(Context ctx)
    {
        this.ctx = ctx;
        activity = (Activity) ctx;
    }


    public BackgroundCredit asyncObject;
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
    protected String doInBackground(String... params)
    {
        Data = ctx.getSharedPreferences("MYDATA", Context.MODE_PRIVATE);
        editor = Data.edit();

        String method = params[0];
        if (method.equals("transC")) {
            try {
                URL url = new URL(transferC_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(OS, "UTF-8"));
                String commuter_id = params[1];
                String bus_id = params[2];
                String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(commuter_id, "UTF-8") + "&" +
                        URLEncoder.encode("idbus", "UTF-8") + "=" + URLEncoder.encode(bus_id, "UTF-8");
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

        return null;
    }

    @Override
    protected void onPostExecute(String json)
    {
        try {
            progressDialog.dismiss();
            JSONObject jsonObject = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
            JSONObject JO = jsonArray.getJSONObject(0);
            String code = JO.getString("code");

            if(code.equals("trans_success"))
            {   String message = JO.getString("message");
                String credit = JO.getString("credit");
                editor.putString("Credit",credit).commit();
                TextView creditTv;
                creditTv = (TextView) activity.findViewById(R.id.current_credit);
                creditTv.setText("Your Balance: " + credit + "SDG");
                String commuter_name = Data.getString("commuter_name","Error getting name");
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setPositiveButton("OK",null);
                builder.setIcon(R.drawable.qrcode_approved);
                builder.setTitle("Scan Success");
                builder.setMessage(message + " " + commuter_name);
                builder.show();
            }
            else if(code.equals("trans_failed"))
            {String message = JO.getString("message");
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setPositiveButton("OK",null);
                builder.setTitle("Transaction Failed");
                builder.setMessage(message);
                builder.show();
            }
            else if (code.equals("found_user"))
            {
                String credit = JO.getString("credit");
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setPositiveButton("OK",null);
                builder.setTitle("Your Balance");
                builder.setMessage("Your Balance is " + credit + " SDG");
                builder.show();
            }
            else if (code.equals("no_user"))
            {String message = JO.getString("message");
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setPositiveButton("OK",null);
                builder.setTitle("Error");
                builder.setMessage(message);
                builder.show();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
