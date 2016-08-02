package com.example.conzi.weather;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Asks the user for input, only a 5 digit zip code is accepted. The input is then sent back to
 * the parent activity MainActivity to build the URL
 *
 * @author Conor Stephens
 */
public class AddCity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /*Inflates the mmenu*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    /**
     * Menu includes two items. action_help displays an informative Toast message and the home
     * button returns the user to MainActivity
     *
     * @param item: The menu item that was selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_help:
                Toast.makeText(AddCity.this, "Enter zip code and press SEARCH",
                        Toast.LENGTH_LONG).show();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * When the submit button is clicked, the number entered in the editText field is stored in
     * zipcode and then passed back to MainActivity
     *
     * @param view: The current view.
     */
    public void buttonClickHandler(View view) {
        EditText et = (EditText) findViewById(R.id.zipField);
        String stringZip = et.getText().toString();
        //Handles empty input
        if(!stringZip.matches("")) {
            int zipCode = Integer.parseInt(stringZip);

            Intent data = new Intent();
            data.putExtra(MainActivity.RETURN_MESSAGE, zipCode);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
