package com.finalhack.totalelevation;

import static com.finalhack.totalelevation.ElevationActivity.TAG_ELEVATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class NmeaListener implements android.location.GpsStatus.NmeaListener {
	
	private static final String NMEA_DELIMITER = ",";
	private static final int NMEA_SENTENCE_COUNT = 1;
	private static final int NMEA_SENTENCE_NUMBER = 2;
	private static final int NMEA_FIX_QUALITY = 6;
	private static final int NMEA_PDOP = 15;
	private static final String NMEA_SATELLITES_IN_VIEW = "GPGSV";
	private static final String NMEA_LOCATION_AND_ACCURACY_DATA = "GPGGA";
	private static final String NMEA_DOP = "GPGSA";
	
	private String pdop = null;
	
	private int nmeaSentenceCount = -1;
	private List<String> individualNmeaSatellites;
	private ViewHolder mViewHolder;

	private Map<String, String> fixQualityMap = new HashMap<String, String>();
	
	public NmeaListener(ViewHolder viewHolder) {
		mViewHolder = viewHolder;
		
		fixQualityMap.put("0", "Not enough GPS data");
		fixQualityMap.put("1", "Normal");
		fixQualityMap.put("2", "DGPS fix (enhanced GPS)");
		fixQualityMap.put("3", "PPS (enhanced GPS)");
		fixQualityMap.put("4", "Real Time Kinematic");
		fixQualityMap.put("5", "Float RTK");
		fixQualityMap.put("6", "Estimate");
		fixQualityMap.put("7", "Manual mode");
		fixQualityMap.put("8", "Simulation mode");
	}

	@Override
	public void onNmeaReceived(long timestamp, String nmea) {

		// Make the checksum parseable by adding a delimiter to separate the end checksum
		nmea = nmea.replace("*", ",*");
		if (BuildConfig.DEBUG) Log.d(TAG_ELEVATION, "NMEA: " + nmea);

		// As long as the line we've received is the set of stats we want...
		if (nmea.contains(NMEA_SATELLITES_IN_VIEW)) {
			// Separate out everything for parsing
			String[] nmeaParts = nmea.split(NMEA_DELIMITER);
			int newNmeaSentenceCount = Integer.parseInt(nmeaParts[NMEA_SENTENCE_COUNT]);
			int currentNmeaSentence = Integer.parseInt(nmeaParts[NMEA_SENTENCE_NUMBER]);
			if (BuildConfig.DEBUG) Log.d(TAG_ELEVATION, "\n\nnewCount: " + newNmeaSentenceCount + ", current: "
					+ currentNmeaSentence);

			// We need to make sure that we reset our stats if we get new ones in
			// Reset if the count changed
			if (nmeaSentenceCount != newNmeaSentenceCount 
					// Or we're starting over on sentence 1
					|| currentNmeaSentence == 1) {
				individualNmeaSatellites = new ArrayList<String>();
				nmeaSentenceCount = newNmeaSentenceCount;
			}

			// Extract all the individual satellite stats
			List<String> someIndividualNmeaSatellites = Util.extractIndividualSatelliteData(nmeaParts);
			for (String satellite : someIndividualNmeaSatellites)
				individualNmeaSatellites.add(satellite);

			// If we've gathered all the stats for all the satellites we see, show those stats
			if (nmeaSentenceCount == currentNmeaSentence &&
					individualNmeaSatellites != null &&
					!individualNmeaSatellites.isEmpty() &&
					!individualNmeaSatellites.get(0).equals(",,,")) {
				
				// Calculate the number of satellites and their average signal strength
				int numOfSatellites = 0;
				int totalSignalStrength = 0;
				
				String validSatPrnList = "";
				
				// Get the average signal strength
				for (String satellite : individualNmeaSatellites) {
					
					if (BuildConfig.DEBUG) Log.d("", satellite);
					
					try {
						if (BuildConfig.DEBUG) Log.d(TAG_ELEVATION, satellite);
						
						String[] satelliteParts = satellite.split(NMEA_DELIMITER);
						if (satelliteParts == null || satelliteParts.length <= Util.NMEA_SUB_SIGNAL) continue;
						
						String textStrength = satelliteParts[Util.NMEA_SUB_SIGNAL];
						String prn = satelliteParts[Util.NMEA_SUB_PRN_NUMBER];
						
						// Make sure there is a signal strength for this sat
						if (textStrength == null || textStrength.length() == 0) continue;
						int strength = Integer.parseInt(textStrength);
						
						// If there is a strength, add it for averaging
						if (strength > 0) {
							totalSignalStrength += strength;
							numOfSatellites++;
							
							// Comma delimit, but not before the first element
							if (validSatPrnList.length() != 0)
								validSatPrnList += NMEA_DELIMITER;

							validSatPrnList += prn;
						}
					} catch (Exception e) {
						if (BuildConfig.DEBUG) {
							Log.d("", e.getMessage(), e);
							Log.d("", satellite);
						}
					}
				}
				
				List<Integer> prnList = new ArrayList<Integer>();
				String[] prnStrings = validSatPrnList.split(NMEA_DELIMITER);
				for (String prn : prnStrings) {
					try {
						prnList.add(Integer.parseInt(prn));
					}
					catch(Exception e) {
						if (BuildConfig.DEBUG) e.printStackTrace();
					}
				}
				mViewHolder.graph.setAvailSats(prnList);
				
				// Calculate the average signal strength
				int averageSignalStrength = 0;
				if (numOfSatellites > 0) averageSignalStrength = totalSignalStrength / numOfSatellites;

				// Update the UI
				mViewHolder.numSatellites.setText(mViewHolder.context.getString(R.string.gps_number_of_satellites)
						+ numOfSatellites);
				mViewHolder.signalStrength.setText(mViewHolder.context.getString(R.string.gps_signal_strength)
						+ averageSignalStrength);
				mViewHolder.prnList.setText(mViewHolder.context.getString(R.string.visible_sats) + validSatPrnList);
			}
		}
		else if (nmea.contains(NMEA_LOCATION_AND_ACCURACY_DATA)) {
			String[] nmeaParts = nmea.split(NMEA_DELIMITER);
			// If we have fix quality data
			if (nmeaParts.length > NMEA_FIX_QUALITY && nmeaParts[NMEA_FIX_QUALITY].length() > 0) {
				String fixQuality = nmeaParts[NMEA_FIX_QUALITY];
				fixQuality = fixQualityMap.get(fixQuality);
				String quality = mViewHolder.context.getString(R.string.gps_fix_quality) + fixQuality;
				if (pdop != null) quality += mViewHolder.context.getString(R.string.pdop) + pdop;
				mViewHolder.fixQuality.setText(quality);
			}
		}
		else if (nmea.contains(NMEA_DOP)) {
			String[] nmeaParts = nmea.split(NMEA_DELIMITER);
			// If we have fix quality data
			if (nmeaParts.length > NMEA_PDOP && nmeaParts[NMEA_PDOP].length() > 0) {
				pdop = nmeaParts[NMEA_PDOP];
			}			
		}
	}
}
