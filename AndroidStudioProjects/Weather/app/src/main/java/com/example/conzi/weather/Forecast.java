package com.example.conzi.weather;

/**
 * class Forecast
 *
 * Stores all data that will be presented. All of the data fields are strings except for zip code,
 * which is used to build the URL for the HTTP requests.
 * @author Conor Stephens
 */
public class Forecast {
    public static final int NUM_VALUES = 10;
    /**
     * mValues[0] = City, State
     * mValues[1] = Date
     * mValues[2] = Current Temp
     * mValues[3] = Feels Like
     * mValues[4] = Conditions
     * mValues[5] = City
     * mValues[6] = State
     * mValues[7] = Humidity
     * mValues[8] = Visibility
     * mValues[9] = Wind
     */
    private String mValues[];
    private int mZip = 0;

    /** Get functions for each field*/
    public String getmCity(){return mValues[0];}
    public String getmDate(){return mValues[1];}
    public String getmCurrentTemp(){return mValues[2];}
    public String getmFeelsLike(){return mValues[3];}
    public String getmConditions(){return mValues[4];}
    public String getmShortCity(){return mValues[5];}
    public String getmState(){return mValues[6];}
    public String getmHumidity(){return mValues[7];}
    public String getmVisibility(){return mValues[8];}
    public String getmWind(){return mValues[9];}
    public int getmZip(){return mZip;}

    /** Set functions for each field*/
    public void setmCity(String city){mValues[0] = city;}
    public void setmDate(String date){mValues[1] = date;}
    public void setmCurrentTemp(String currentTemp){mValues[2] = currentTemp;}
    public void setmFeelsLike(String feelsLike){mValues[3] = feelsLike;}
    public void setmConditions(String conditions){mValues[4] = conditions;}
    public void setmShortCity(String shortCity) {mValues[5] = shortCity;}
    public void setmState(String state) {mValues[6] = state;}
    public void setmHumidity(String humidity){mValues[7] = humidity;}
    public void setmVisibility(String visibility){mValues[8] = visibility;}
    public void setmWind(String wind){mValues[9] = wind;}
    public void setmZip(int zip){mZip = zip;}

    /** Default constructor, fields are initialized*/
    public Forecast(){
        mValues = new String[]{"No City Selected","N/A","N/A","N/A","N/A","N/A","N/A","N/A",
                "N/A","N/A"};
    }
    /** Constructor with a parameter for each field*/
    public Forecast(int zip, String values[]){
        mZip = zip;
        mValues = new String[NUM_VALUES];
        mValues=values;
    }

    /**
     * toStringArray builds an array of strings that will populate the listView on the
     * MainActivity.
     */
    public String[] toStringArray(){
        String[] stringArray = new String[5];
        stringArray[0] = mValues[5].toUpperCase();
        stringArray[1] = "DATE: " + mValues[1];
        stringArray[2] = "CURRENT TEMP: " + mValues[2];
        stringArray[3] = "FEELS LIKE: " + mValues[3];
        stringArray[4] = "CONDITIONS: " + mValues[4];
        return stringArray;
    }

    /**
     * cityDetails builds an array of strings that will populate the listView on the
     * DetailsActivity when the "CITY" item is selected
     */
    public String[] cityDetails(){
        String[] stringArray = new String[3];
        stringArray[0] = "CITY: "+ mValues[5];
        stringArray[1] = "STATE: " + mValues[6];
        stringArray[2] = "ZIP: " + mZip;
        return stringArray;
    }

    /**
     * weatherDetails builds an array of strings that will populate the listView on the
     * DetailsActivity when one of the weather items is selected
     */
    public String[] weatherDetails(){
        String [] stringArray = new String[6];
        stringArray[0] = "CURRENT TEMP: " + mValues[2];
        stringArray[1] = "FEELS LIKE: " + mValues[3];
        stringArray[2] = "CONDITIONS: " + mValues[4];
        stringArray[3] = "HUMIDITY: " + mValues[7];
        stringArray[4] = "VISIBILITY: " + mValues[8] + " miles";
        stringArray[5] = "WIND: " + mValues[9];
        return stringArray;
    }
}
