package com.finalhack.totalelevation;

import android.content.Context;
import android.widget.TextView;

// Convenience class for allowing controller to update view
// See flyweight pattern
public class ViewHolder {
	
	public ViewHolder(Context context, TextView numSatellites, TextView signalStrength, TextView fixQuality, TextView prnList, Graph graph) {
		this.context = context;
		this.numSatellites = numSatellites;
		this.signalStrength = signalStrength;
		this.fixQuality = fixQuality;
		this.prnList = prnList;
		this.graph = graph;
	}
	
	public Context context;
	public TextView numSatellites;
	public TextView signalStrength;
	public TextView fixQuality;
	public TextView prnList;
	public Graph graph;
}
