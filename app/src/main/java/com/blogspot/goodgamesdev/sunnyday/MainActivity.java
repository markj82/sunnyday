package com.blogspot.goodgamesdev.sunnyday;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ConstraintLayout constraintLayout;

    // inner class for web connection, to download json from openweathermap website
    public class DownloadTask extends AsyncTask<String, Void, String> {

        // this code will never touch our UI
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        // this code can touch UI, but just a bit :)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.i("JSON: returned val", s);

            try {

                listView = findViewById(R.id.listViewId);
                ArrayList<String> chosenData = new ArrayList<>();

                // HERE I CHOOSE ONLY THE MOST INTERESTING DATA

                JSONObject jsonObject = new JSONObject(s);
                Log.i("jsonObject:", jsonObject.toString());

                // add country to chosenData
                JSONObject countryObj = jsonObject.getJSONObject("sys");
                String country = "Country: " + countryObj.getString("country");
                chosenData.add(country);
                Log.i("sys", countryObj.toString());

                // add accurate location
                String location = "Your location: " + jsonObject.getString("name");
                chosenData.add(location);

                // add coordinates
                JSONObject coord  = jsonObject.getJSONObject("coord");
                Log.i("coord", coord.toString());
                String lon = "Longitude: " + coord.getString("lon");
                String lat = "Latitude: " + coord.getString("lat");
                chosenData.add(lon);
                chosenData.add(lat);

                // add weather info
                String weatherInfo = jsonObject.getString("weather");
                JSONArray arr = new JSONArray(weatherInfo);
                JSONObject weatherOb = arr.getJSONObject(0);
                String weather = "Weather: " + weatherOb.getString("main");
                chosenData.add(weather);

                // add temperature
                JSONObject tempInfo = jsonObject.getJSONObject("main");
                String temp = "Temperature: " + tempInfo.getString("temp") + " Celsius";
                chosenData.add(temp);

                // add wind speed
                JSONObject windInfo = jsonObject.getJSONObject("wind");
                String wind = "Wind: " + windInfo.getString("speed") + " meter/sec";
                chosenData.add(wind);

                // add clouds info
                JSONObject cloudInfo = jsonObject.getJSONObject("clouds");
                String clouds = "Clouds: " + cloudInfo.getString("all") + " percent";
                chosenData.add(clouds);

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (MainActivity.this, android.R.layout.simple_list_item_1, chosenData);
                listView.setAdapter(arrayAdapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // API key from openweathermap
    // api.openweathermap.org/data/2.5/weather?lat=57&lon=21&appid=e146665a4b33f325f8356036d11f0baf
    // add &units=metric on the end of the link to get metric data (celsius temp)
    private static final String API_KEY = "e146665a4b33f325f8356036d11f0baf";

    //Button button;
    ImageButton buttonImg;
    String webAddress;

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        constraintLayout = findViewById(R.id.constraintLayoutId);
//        constraintLayout.setBackground(getResources().getDrawable(R.drawable.clouds));

        requestPermission();

        client = LocationServices.getFusedLocationProviderClient(this);

        //button = findViewById(R.id.getLocationButtonId);
        buttonImg = findViewById(R.id.imageButtonId);

        buttonImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.d("Test Message:", "The latitude is " + String.valueOf(latitude));
                            Log.d("Test Message:", "The longitude is " + String.valueOf(longitude));

                            // build string with latitude and longitude and save it to webAddress variable
                            webAddress = "http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";
                            Log.d("webAddress: ", webAddress);

                            // instantiate inner class and run it's method
                            DownloadTask task = new DownloadTask();
                            task.execute(webAddress);

                        }
                    }
                });
            }
        });
    }

    // permission from user to use device location
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }


}
