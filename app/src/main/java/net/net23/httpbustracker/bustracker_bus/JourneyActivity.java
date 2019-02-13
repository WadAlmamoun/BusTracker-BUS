package net.net23.httpbustracker.bustracker_bus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

public class JourneyActivity extends AppCompatActivity {

    Spinner routeStart, routeEnd;
    ArrayAdapter<CharSequence> adapter;
    SharedPreferences Data;
    SharedPreferences.Editor editor;
    String routeS, routeE,route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("Go To Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setMessage("Application is requesting permission to turn on Location Service. \n Allow?");
            builder.show();

            Data = getSharedPreferences("MYDATA", Context.MODE_PRIVATE);
            route = Data.getString("Route", "no route");

            routeStart = (Spinner) findViewById(R.id.route_start_spinner);

            if (route.equals("ArabiMarkazi")) {
                adapter = ArrayAdapter.createFromResource(this, R.array.routes_markazi, android.R.layout.simple_spinner_item);
            } else if (route.equals("ArabiMamora")) {
                adapter = ArrayAdapter.createFromResource(this, R.array.routes_mamora, android.R.layout.simple_spinner_item);
            } else if (route.equals("ArabiJabra")) {
                adapter = ArrayAdapter.createFromResource(this, R.array.routes_jabra, android.R.layout.simple_spinner_item);
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            routeStart.setAdapter(adapter);
            routeStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        routeS = "";
                        routeE = "";
                    } else if (position == 1 && route.equals("ArabiMarkazi")) {
                        routeS = "Arabi";
                        routeE = "Markazi";
                    } else if (position == 1 && route.equals("ArabiMamora")) {
                        routeS = "Arabi";
                        routeE = "Mamora";
                    } else if (position == 1 && route.equals("ArabiJabra")) {
                        routeS = "Arabi";
                        routeE = "Jabra";
                    } else if (position == 2 && route.equals("ArabiMarkazi")) {
                        routeS = "Markazi";
                        routeE = "Arabi";
                    } else if (position == 2 && route.equals("ArabiMamora")) {
                        routeS = "Mamora";
                        routeE = "Arabi";
                    } else if (position == 2 && route.equals("ArabiJabra")) {
                        routeS = "Jabra";
                        routeE = "Arabi";
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            Button submitRoute = (Button) findViewById(R.id.submit_route);
            submitRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (routeE.isEmpty()) {

                    } else {
                        String idbus = Data.getString("BusID", "NO ID");
                        Backgroundjourney backgroundjourney = new Backgroundjourney(JourneyActivity.this);
                        backgroundjourney.execute(routeS, routeE, idbus);
                    }
                }
            });
        }else
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
                editor = Data.edit();
                editor.putBoolean("login", false);
                editor.commit();
            startActivity( new Intent(this,LoginActivity.class));
                finish();
            }
        return super.onOptionsItemSelected(item);
    }

    private class Backgroundjourney extends AsyncTask<String, Void, String> {
        SharedPreferences Data;
        SharedPreferences.Editor editor;
        //String journey_set_url = "http://bustracker.net23.net/bus_trip_activation.php";
        String journey_set_url = "http://bustrackersudan.net16.net/bus_trip_activation.php";
        Activity activity;
        Context context;
        ProgressDialog progressDialog;
        AlertDialog.Builder builder;

        Backgroundjourney(Context context) {
            this.context = context;
            activity = (Activity) context;
        }

        public Backgroundjourney asyncObject;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Please wait");
            progressDialog.setMessage("Connecting to server...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
            asyncObject = this;
            new CountDownTimer(20000, 1000) {
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
            Data = context.getSharedPreferences("MYDATA", Context.MODE_PRIVATE);
            editor = Data.edit();

                try {
                    URL url = new URL(journey_set_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String tripS = params[0];
                    String tripE = params[1];
                    String busid =  params[2];
                    String data = URLEncoder.encode("tripS", "UTF-8") + "=" + URLEncoder.encode(tripS, "UTF-8") + "&" +
                            URLEncoder.encode("tripE", "UTF-8") + "=" + URLEncoder.encode(tripE, "UTF-8") + "&" +
                            URLEncoder.encode("idbus", "UTF-8") + "=" + URLEncoder.encode(busid, "UTF-8");
                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }
                    httpURLConnection.disconnect();
                    return stringBuilder.toString().trim();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
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

                if (code.equals("t_active")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putBoolean("JourneyStart",true);
                            activity.startActivity(new Intent(activity,MainActivity.class));
                            activity.finish();
                        }
                    });
                    builder.setTitle("Journey Active");
                    builder.setMessage(message);
                    builder.show();
                } else if (code.equals("t_unactivated")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setPositiveButton("OK", null);
                    builder.setTitle("Error!");
                    builder.setMessage(message);
                    builder.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
