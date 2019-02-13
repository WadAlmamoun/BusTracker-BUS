package net.net23.httpbustracker.bustracker_bus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.encoder.QRCode;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.data;
import static android.R.attr.name;
import static android.R.attr.theme;
import static android.R.id.input;
import static net.net23.httpbustracker.bustracker_bus.R.id.latitude;

public class MainActivity extends AppCompatActivity {


    TextView driverName,busId,busRoute;
    //Scanner//////////////////////////////////////////////
    SharedPreferences Data;
    SharedPreferences.Editor editor;
    int TokenMin, TokenHour;
    String hashedDMY,strTokenDMY;
    String commuter_name, commuter_id;
    private Button scan_btn;
    TextView currnetCredit;
    //Tracker/////////////////////////////////////////////
    private final static int MY_PERMISSION_FINE_LOCATION = 101;
    TextView Long, Lat;
    Button sendMyLocationStart, sendMyLocationStop;
    private LocationManager locationManager;
    private LocationListener locationListener;
    String latit = "0";
    String longit ="0";
    String from,to;
    StringRequest stringRequest;
    int LOCATION_TIMEOUT = 0;
    ProgressDialog busProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Data = getSharedPreferences("MYDATA", Context.MODE_PRIVATE);
        editor = Data.edit();

        busProgressDialog= new ProgressDialog(this);
        busProgressDialog.setMessage("Locating... ");
        busProgressDialog.setIndeterminate(true);
        busProgressDialog.setCancelable(false);


        driverName = (TextView) findViewById(R.id.driver_name);
        busId = (TextView) findViewById(R.id.bus_id);
        busRoute = (TextView) findViewById(R.id.bus_route);

        final String name = Data.getString("Name","no name");
        String id = Data.getString("BusID","no id");
        String route = Data.getString("Route","no route");

        driverName.setText("Driver: " + name);
        busId.setText("Bus ID: " + id);
        if(route.equals("ArabiMarkazi")) {
            busRoute.setText("Bus Route: السوق العربي - السوق المركزي" );
            from = "Al-Sooq Al-Arabi";
            to = "Al-Sooq Al-Markazi";
        }
        else if(route.equals("ArabiMamora"))
        {
            busRoute.setText("Bus Route: السوق العربي - المعمورة" );
            from = "Al-Sooq Al-Arabi";
            to = "Al-Maamora";
        }
        else if(route.equals("ArabiJabra"))
        {
            busRoute.setText("Bus Route: السوق العربي - جبرة" );
            from = "Al-Sooq Al-Arabi";
            to = "Jabra";
        }
        // Ticketing ****************************************************************
        scan_btn = (Button) findViewById(R.id.scan_btn);
        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("QR Code Bus Scanner");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

        currnetCredit = (TextView) findViewById(R.id.current_credit);
        String credit = Data.getString("Credit","ERROR!");
        currnetCredit.setText("Your Balance: " + credit + "SDG");
        //********************************************************************************************************************

        // Tracking###########################################################################################################
        sendMyLocationStart = (Button) findViewById(R.id.send_my_location_start);
        sendMyLocationStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        busProgressDialog.show();


                        Long = (TextView) findViewById(R.id.longitude);
                        Lat = (TextView) findViewById(latitude);
                        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {

                                    busProgressDialog.dismiss();

                                    latit = String.valueOf(location.getLatitude());
                                    longit = String.valueOf(location.getLongitude());
                                    Lat.setText(latit);
                                    Long.setText(longit);
                                    String bus_id = Data.getString("BusID", "ERROR getting id");
                                    // SEND TO SERVER
                                    volley(longit, latit, bus_id);
                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {}
                                @Override
                                public void onProviderEnabled(String provider) {}
                                @Override
                                public void onProviderDisabled(String provider) {
                                    stringRequest.cancel();
                                }
                            };
                            locationManager.requestLocationUpdates("gps", 2000, 0, locationListener);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                                }
                            });
                            builder.setTitle("Warning");
                            builder.setMessage("You must enable the location services!");
                            builder.show();

                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
                        }
                    }
                } else{
                    Toast.makeText(MainActivity.this,"NO INTERNET CONNECTION",Toast.LENGTH_LONG).show();
                }
                    }
        });

        sendMyLocationStop = (Button) findViewById(R.id.send_my_location_stop);
        sendMyLocationStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        editor.putBoolean("JourneyStart",false);
                        locationManager.removeUpdates(locationListener);
                        locationManager = null;
                        longit = "0";
                        latit = "0";
                        Lat.setText(latit);
                        Long.setText(longit);
                        String bus_id = Data.getString("BusID", "ERROR getting id");
                        volley(longit, latit, bus_id);
                        //SEND TO SERVER
                    }
                    catch (java.lang.NullPointerException e)
                    {
                        Toast.makeText(MainActivity.this,"No Journey Has Started", Toast.LENGTH_LONG).show();
                    }
                } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_FINE_LOCATION);
                        }
                    }
                    }
                else{
                    Toast.makeText(MainActivity.this,"NO INTERNET CONNECTION",Toast.LENGTH_LONG).show();
                }
            }
        });

        //###################################################################################################################################
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
            if (latit != "0" || longit != "0") {
                Toast.makeText(MainActivity.this, "You Must Press on END JOURNEY Button Before You Logout", Toast.LENGTH_LONG).show();
            } else {
                editor = Data.edit();
                editor.putBoolean("login", false);
                editor.commit();
                startActivity( new Intent(this,LoginActivity.class));
                finish();
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

        @Override
    public void onBackPressed() {
        if (latit != "0" || longit != "0")
        {
            Toast.makeText(this,"You Must Press on END JOURNEY Button Before You Exit",Toast.LENGTH_LONG).show();
        }
        else {
            System.exit(0);
        }
    }


    // Sub-activities for scanning ******************************************************************
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            } else {

                String userdata = result.getContents();
                String[] separated = userdata.split(":");
                try {
                    commuter_name = separated[0]; //Name
                    Data = getSharedPreferences("MYDATA", Context.MODE_PRIVATE);
                    editor = Data.edit();
                    editor.putString("commuter_name", commuter_name).commit();
                    commuter_id = separated[1]; //id
                    String strTokenMin = separated[2]; //minutes
                    String strTokenHour = separated[3]; //hour of day
                    strTokenDMY = separated[4]; //year

                    //Change String values to integers for comparison:
                    TokenMin = Integer.valueOf(strTokenMin);
                    TokenHour = Integer.valueOf(strTokenHour);
                    ////////////////////////////////////////////////////////////////
                    //Access Token Creator
                    Calendar calendar = Calendar.getInstance();
                    int min = calendar.get(Calendar.MINUTE);
                    int hour = calendar.get(Calendar.HOUR);
                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                    int month = calendar.get(Calendar.MONTH);
                    int year = calendar.get(Calendar.YEAR);
                    ////////////////////////////////////////////////////////////////
                    // Day-Month-Year ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                    String DAY = String.valueOf(day);
                    String MONTH = String.valueOf(month);
                    String Year = String.valueOf(year);
                    String DMY = DAY + MONTH + Year;
                        MessageDigest digest;
                        try {
                            digest = MessageDigest.getInstance("MD5");
                            digest.reset();
                            digest.update(DMY.getBytes());
                            byte[] a = digest.digest();
                            int len = a.length;
                            StringBuilder sb = new StringBuilder(len << 1);
                            for (int i = 0; i < len; i++) {
                                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                                sb.append(Character.forDigit(a[i] & 0x0f, 16));
                            }
                            hashedDMY = sb.toString();
                        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }


                    /////////////////////////////////////////////////////////////////
                    //QR CODE AUTHENTICATOR
                    if (hashedDMY.equals(strTokenDMY)) {
                        if (min <= TokenMin && TokenHour == hour || min < TokenMin + 1 && hour == TokenHour + 1) {
                            //ALL GOOD...CONNECT TO SERVER
                            String method = "transC";
                            String bus_id = Data.getString("BusID", "Error");
                            BackgroundCredit backgroundCredit = new BackgroundCredit(this);
                            backgroundCredit.execute(method, commuter_id, bus_id);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setPositiveButton("OK", null);
                            builder.setTitle("Error!");
                            builder.setIcon(R.drawable.qrcode_denied);
                            builder.setMessage("Outdated QR Code!!");
                            builder.show();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setPositiveButton("OK", null);
                        builder.setTitle("Error!");
                        builder.setIcon(R.drawable.qrcode_denied);
                        builder.setMessage("Outdated QR Code!!");
                        builder.show();
                    }

                } catch (java.lang.IndexOutOfBoundsException e) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton("OK", null);
                    builder.setTitle("Error!");
                    builder.setIcon(R.drawable.qrcode_denied);
                    builder.setMessage("Unidentified QR Code!!");
                    builder.show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //***********************************************************************************************
    // Sub-activities for tracking ##################################################################

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(getApplicationContext(),"You Must Enable The Location Service",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String bus_id = Data.getString("BusID","ERROR getting id");
        volley(longit,latit,bus_id);
    }

    public void volley (final String longitude, final String latitude, final String busid)
    {
        //Volley server contact
//        String locationUrl = "http://bustracker.net23.net/gps_bus-server.php";
        String locationUrl = "http://bustrackersudan.net16.net/gps_bus-server.php";
        stringRequest = new StringRequest(Request.Method.POST, locationUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        LOCATION_TIMEOUT = 0;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               if (LOCATION_TIMEOUT < 10) {
                   Toast.makeText(MainActivity.this, "Error Sending Coordinates!", Toast.LENGTH_SHORT).show();
                   LOCATION_TIMEOUT = LOCATION_TIMEOUT + 1;
                   if (LOCATION_TIMEOUT == 10) {
                       LOCATION_TIMEOUT = 0;
                       Lat.setText("0");
                       Long.setText("0");
                       String bus_id = Data.getString("BusID", "ERROR getting id");
                       volley(longit, latit, bus_id);
                       AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                       builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               System.exit(0);
                           }
                       });
                       builder.setTitle("Error Sending Coordinates!");
                       builder.setMessage("Press OK to close the app.");
                       builder.show();
                   }
               }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("longitude",longitude);
                params.put("latitude",latitude);
                params.put("idbus",busid);

                return params;
            }
        };
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
    }
    //####################################################################################################3
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
