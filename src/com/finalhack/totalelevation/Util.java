package com.finalhack.totalelevation;

import static com.finalhack.totalelevation.ElevationActivity.TAG_ELEVATION;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.util.Log;

public class Util {

	private static final double FEET_PER_METER = 3.28084;
	private static final String FEET_ID = "ft";
	private static final String METERS_ID = "m";
	private static final String US_LOCALE = "en_US";
	public static final int NMEA_SUB_PRN_NUMBER = 0;
	public static final int NMEA_SUB_ELEVATION_DEGREES = 1;
	public static final int NMEA_SUB_AZIMUTH = 2;
	public static final int NMEA_SUB_SIGNAL = 3;
	private static final int NUMBER_OF_SATELLITE_DATA_PARTS = 4;

	//Localize the heights
	public static String localizeString(double meters, String locale)
	{
		String ELEVATION_FORMAT = "#,###";
		
		//If we're in the US, use Feet
		if (US_LOCALE.equals(locale))
		{
			DecimalFormat formatter = (DecimalFormat)NumberFormat.getNumberInstance(Locale.getDefault());
			formatter.applyPattern(ELEVATION_FORMAT);
			return formatter.format(localizeInt(meters, locale)) + FEET_ID;
		} 
		//Use meters
		else {
			DecimalFormat formatter = (DecimalFormat)NumberFormat.getNumberInstance(Locale.getDefault());
			formatter.applyPattern(ELEVATION_FORMAT);
			return formatter.format(localizeInt(meters, locale)) + METERS_ID;	
		}
	}

	//A helper method for the actual numeric conversion of meters<->feet
	public static int localizeInt(double meters, String locale)
	{
		if (US_LOCALE.equals(locale)) return (int)(meters * FEET_PER_METER);
		return (int)meters;
	}
	
    //Pull out data specific to each satellite
    public static List<String> extractIndividualSatelliteData(String[] nmeaSentence)
    {
    	//Create a place holder for satellites
    	List<String> individualNmeaSatellites = new ArrayList<String>();
    	try
    	{
    		//Sat data starts here
    		int i=NUMBER_OF_SATELLITE_DATA_PARTS;
    		
    		//If we are not pointing to the checksum block, there is still another sat to parse
    		while (!nmeaSentence[i].contains("*"))
    		{
    			String singleSatelliteData = String.format("%s,%s,%s,%s", 
    					nmeaSentence[i + NMEA_SUB_PRN_NUMBER], 
    					nmeaSentence[i + NMEA_SUB_ELEVATION_DEGREES],
    					nmeaSentence[i + NMEA_SUB_AZIMUTH],
    					nmeaSentence[i + NMEA_SUB_SIGNAL]);
    			
    			//Grab the data for the satellite we're on
    			individualNmeaSatellites.add(singleSatelliteData);
    			if (BuildConfig.DEBUG) Log.d(TAG_ELEVATION, singleSatelliteData);
    			
    			// Go to the next set of sat data
    			i+=NUMBER_OF_SATELLITE_DATA_PARTS;
    		}
    	}
    	catch(Exception e)
    	{
    		if (BuildConfig.DEBUG) Log.d(TAG_ELEVATION, e.getMessage(), e);
    	}

    	return individualNmeaSatellites;
    }
	
}