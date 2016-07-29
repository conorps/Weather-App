package com.example.conzi.weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * MainActivity
 *
 * Sets the layout for the main activity screen. Checks for saved preference data
 * and creates a Forecast object with said data. Receives Zip Code from AddCity activity and
 * uses nested class DownloadInfo to create background thread. HTTP requests are made in
 * background thread and new Forecast data is set in onPostExecute. onStop puts the
 * data from mForecast into sharedPreferences file.
 *
 * @author Conor Stephens
 */
public class MainActivity extends AppCompatActivity {
    private Forecast mForecast;
    private SharedPreferences mSharedPref;
    private static final String TAG = "MainActivity";
    private static final int ZIP_REQUEST = 1000;
    public static final String RETURN_MESSAGE = "RETURN_MESSAGE";

    /**
     * Sets Content view and checks member mSharedPref for previously loaded city weather data
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPref = getSharedPreferences("prevCity", Context.MODE_PRIVATE);

        //Sets member mForecast to values stored in sharedPreferences, default values otherwise
        mForecast = new Forecast(mSharedPref.getString("pCity","No City Selected"),
                mSharedPref.getString("pConditions","N/A"),
                mSharedPref.getString("pFeelsLike","N/A"),
                mSharedPref.getString("pLow", "N/A"),
                mSharedPref.getString("pCurrentTemp","N/A"),
                mSharedPref.getInt("pZip",0),
                mSharedPref.getString("pDate","N/A"),
                mSharedPref.getString("pState", "N/A"),
                mSharedPref.getString("pShortCity", "N/A"),
                mSharedPref.getString("pWind", "N/A"),
                mSharedPref.getString("pHumidity", "N/A"),
                mSharedPref.getString("pVisibility", "N/A"));

        //toStringArray used with ArrayAdapter to populate listView for MainActivity
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mForecast.toStringArray());
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);

        //Item click listener for listView in MainActivity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,DetailActivity.class);

                //Case 0 brings up additional city data, cases 2,3,4 additional weather data.
                //Item 1 does not have additional data and goes to default case.
                switch (position){
                    case 0:
                        intent.putExtra("CITY_EXTRA", mForecast.cityDetails());
                        startActivity(intent);
                        break;
                    case 2:case 3:case 4:
                        intent.putExtra("WEATHER_EXTRA", mForecast.weatherDetails());
                        startActivity(intent);
                        break;
                    default:
                        Toast.makeText(MainActivity.this,
                                "Nothing to show", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    /**
     * Inflates menu with menu layout from menu_main.xml
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * onStop stores all weather data with sharedPreferences before it is destroyed
     */
    @Override
    protected void onStop() {
        super.onStop();

        mSharedPref = getSharedPreferences("prevCity", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("pCity",mForecast.getmCity());
        editor.putString("pConditions",mForecast.getmConditions());
        editor.putString("pFeelsLike",mForecast.getmFeelsLike());
        editor.putString("pLow",mForecast.getmLow());
        editor.putString("pCurrentTemp",mForecast.getmCurrentTemp());
        editor.putString("pDate",mForecast.getmDate());
        editor.putInt("pZip",mForecast.getmZip());
        editor.putString("pState", mForecast.getmState());
        editor.putString("pShortCity", mForecast.getmShortCity());
        editor.putString("pWind", mForecast.getmWind());
        editor.putString("pHumidity", mForecast.getmHumidity());
        editor.putString("pVisibility", mForecast.getmVisibility());
        editor.apply();

    }

    /**
     * Two options can be selected: action_add, represented by the "+" icon, opens up
     * AddCity activity where a new zip code can be input.
     * action_help displays an informative message
     * @param item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddCity.class);
                startActivityForResult(intent,ZIP_REQUEST);
                return true;
            case R.id.action_help:
                Toast.makeText(MainActivity.this, "Select the \"+\" icon to choose a new city, " +
                        "or tap the forecast for more info",
                        Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Receives response code and result code along with the zip code passed from AddCity.
     * The Zip code is then set in the Forecast object and will be used to build the URL
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ZIP_REQUEST) {
            if (resultCode == RESULT_OK) {
                int input = data.getIntExtra(RETURN_MESSAGE, 0);
                mForecast.setmZip(input);
                //DownloadInfo creates background task from here
                new DownloadInfo().execute(mForecast.getmZip());
            }
        }
    }

    /**
     * Extends AsyncTask. Builds the URL using the zipcode field of the mForecast member.
     * Creates a background thread on which the network calls are made and receives response.
     * Parses JSON response and uses specific fields of data to create new Forecast object
     * and populates listView to reflect changes.
     */
    private class DownloadInfo extends AsyncTask<Integer,Integer,String> {
        @Override
        protected String doInBackground(Integer... params) {
            String myurl = "http://api.wunderground.com/api/conditions/q/" +
                    mForecast.getmZip() + ".json";
            String result;
            result = downloadUrl(myurl);
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        /**
         * Takes the string respone from the HTTP request and parses the string to produce
         * the fields which will make up the forecast object. If unsuccessful, the fields remain
         * "N/A".
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                Log.d(TAG, s);
                String city = "NO", conditions = "N/A", currenttemp = "N/A", feelsLike = "N/A",
                        date = "N/A", low = "N/A", state = "N/A", shortCity = "N/A",wind = "N/A",
                        humidity = "N/A", visibility = "N/A";
                //Parsing the string s using JSONObjects
                try {
                    JSONObject jsonobject = new JSONObject(s);
                    JSONObject current_observation =
                            jsonobject.getJSONObject("current_observation");
                    JSONObject display_location =
                            current_observation.getJSONObject("display_location");

                    city = display_location.getString("full");
                    state = display_location.getString("state_name");
                    shortCity = display_location.getString("city");
                    currenttemp = current_observation.getString("temperature_string");
                    feelsLike = current_observation.getString("feelslike_string");
                    date = current_observation.getString("local_time_rfc822");
                    conditions = current_observation.getString("weather");
                    wind = current_observation.getString("wind_string");
                    humidity = current_observation.getString("relative_humidity");
                    visibility = current_observation.getString("visibility_mi");

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "INVALID LOCATION", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                //The Forecast object is updated with this constructor call
                mForecast = new Forecast
                        (city,conditions,feelsLike,low, currenttemp,mForecast.getmZip(),
                                date,state,shortCity,wind,humidity,visibility);
                //Adapter is created using mForecast.toStringArray as input
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        mForecast.toStringArray());
                ListView lv = (ListView) findViewById(R.id.listView);
                lv.setAdapter(adapter);
            } else if (s == null) {
                Toast.makeText(MainActivity.this, "FAILED!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Processes HTTP request and receives response. Builds a string that will eventuall be
         * sent on to onPostExecute where it will be parsed.
         * @param myurl
         */
        private String downloadUrl(String myurl){
            BufferedReader reader = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection connection = (HttpURLConnection)
                        url.openConnection();  //HTTP connection is open
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while((line = reader.readLine())!= null){
                    sb.append(line);
                }
                return sb.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
