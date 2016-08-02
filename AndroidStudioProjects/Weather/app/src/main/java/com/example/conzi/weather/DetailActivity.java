package com.example.conzi.weather;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * When an item in the MainActivity listView is clicked, the DetailActivity is called and
 * provides more details to the user. If CITY was clicked then more details about the city will
 * be shown. If any of the weather items are clicked, then more weather details are shown.
 *
 * @author Conor Stephens
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Attempting to get city details from the intent
        String[] detail_string = getIntent().getStringArrayExtra("CITY_EXTRA");
        //If the city details weren't passed, then detail_string will still be null
        if(detail_string == null){
            detail_string = getIntent().getStringArrayExtra("WEATHER_EXTRA");
        }if(detail_string != null) {
            //Once one of the string arrays is present, it is used to build listView
            // using ArrayAdapter.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    detail_string);
            ListView lv = (ListView) findViewById(R.id.detail_listView);
            lv.setAdapter(adapter);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /** Menu is populated*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    /**
     * Two menu items can be selected. The help button displays a Toast message. The back/home
     * button takes the user back to the parent activity MainActivity.
     * @param item: The menu item that was selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_help:
                Toast.makeText(DetailActivity.this, "Press back button to return",
                        Toast.LENGTH_LONG).show();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
