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
import java.util.Calendar;
import java.util.Locale;

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
     * Sets Content view and checks member mSharedPref for previously loaded city weather data.
     * @param savedInstanceState: Unused.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] pValues = new String[Forecast.NUM_VALUES];
        mSharedPref = getSharedPreferences("prevCity", Context.MODE_PRIVATE);

        //Sets member mForecast to values stored in sharedPreferences, default values otherwise
        pValues[0] = mSharedPref.getString("pCity","No City Selected");
        pValues[1] = mSharedPref.getString("pDate","N/A");
        pValues[2] = mSharedPref.getString("pCurrentTemp","N/A");
        pValues[3] = mSharedPref.getString("pFeelsLike","N/A");
        pValues[4] = mSharedPref.getString("pConditions","N/A");
        pValues[5] = mSharedPref.getString("pShortCity", "N/A");
        pValues[6] = mSharedPref.getString("pState", "N/A");
        pValues[7] = mSharedPref.getString("pHumidity", "N/A");
        pValues[8] = mSharedPref.getString("pVisibility", "N/A");
        pValues[9] = mSharedPref.getString("pWind", "N/A");

        mForecast = new Forecast(mSharedPref.getInt("pZip",0),pValues);


        //toStringArray used with ArrayAdapter to populate listView for MainActivity
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
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
     * Inflates menu with menu layout from menu_main.xml.
     * @param menu: Menu object that will be inflated using menu_main.xml layout.
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
        editor.putString("pDate",mForecast.getmDate());
        editor.putString("pCurrentTemp",mForecast.getmCurrentTemp());
        editor.putString("pFeelsLike",mForecast.getmFeelsLike());
        editor.putString("pConditions",mForecast.getmConditions());
        editor.putString("pShortCity", mForecast.getmShortCity());
        editor.putString("pState", mForecast.getmState());
        editor.putString("pHumidity", mForecast.getmHumidity());
        editor.putString("pVisibility", mForecast.getmVisibility());
        editor.putString("pWind", mForecast.getmWind());
        editor.putInt("pZip",mForecast.getmZip());
        editor.apply();

    }

    /**
     * Two options can be selected: action_add, represented by the "+" icon, opens up
     * AddCity activity where a new zip code can be input.
     * action_help displays an informative message
     * @param item: The menu item that was selected.
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
     * @param requestCode: The type of request that was made.
     * @param resultCode: Whether or not the requested activity was successful.
     * @param data: Holds the data returned from the activity, the ZIP code.
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
         * @param s: The JSON formatted response that will be parsed.
         */
        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                Log.d(TAG, s);
                String values[] = new String[Forecast.NUM_VALUES];
                //Parsing the string s using JSONObjects
                try {
                    JSONObject jsonobject = new JSONObject(s);
                    JSONObject current_observation =
                            jsonobject.getJSONObject("current_observation");
                    JSONObject display_location =
                            current_observation.getJSONObject("display_location");

                    values[0] = display_location.getString("full");
                    values[1] = current_observation.getString("local_time_rfc822");
                    values[2] = current_observation.getString("temperature_string");
                    values[3] = current_observation.getString("feelslike_string");
                    values[4] = current_observation.getString("weather");
                    values[5] = display_location.getString("city");
                    values[6] = display_location.getString("state_name");
                    values[7] = current_observation.getString("relative_humidity");
                    values[8] = current_observation.getString("visibility_mi");
                    values[9] = current_observation.getString("wind_string");



                } catch (JSONException e) {
                    for(int i =0; i < Forecast.NUM_VALUES; i++){
                        values[i] = "N/A";
                    }
                    Toast.makeText(MainActivity.this, "INVALID LOCATION", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                //The Forecast object is updated with this constructor call
                mForecast = new Forecast(mForecast.getmZip(), values);

                //Adapter is created using mForecast.toStringArray as input
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
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
         * @param myurl: The URL that the request will be made to.
         */
        private String downloadUrl(String myurl){
            BufferedReader reader;
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
