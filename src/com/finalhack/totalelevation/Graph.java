package com.finalhack.totalelevation;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;

public class Graph extends View {

	private static final String TAG_GRAPH = "graph";
	
	//Style information
	private int outlineColor = 0xffdddddd;
	private int lineColor = 0xff0000ff;
	private int textColor = 0xff000000; 
	private int height = 100;
	private int textSize = 18;
	private int lastY = Integer.MIN_VALUE;
	private int minElevation = Integer.MAX_VALUE;
	private int maxElevation = Integer.MIN_VALUE;
	private int borderSize = 2;
	public List<Integer> elevations = new ArrayList<Integer>();
	 
	//Some stats
	private float density;
	private int screenWidth;
	
	//Paint for onDraw
	private Paint outlinePaint = new Paint();
	private Paint linePaint = new Paint();
	private Paint textPaint = new Paint();
	private Rect rect = new Rect(0,0,0,0);
	
	//Standard constructor
	@SuppressWarnings("deprecation")
	public Graph(Context context, AttributeSet attrSet)
	{
		super(context, attrSet);
		
		//Added for preview view in IDE
		if (isInEditMode()) return;
		
		//Save off some screen data	
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		screenWidth = (int)display.getWidth();
		
		//Setup all the paint colors
		outlinePaint.setColor(outlineColor);
		linePaint.setColor(lineColor);
		linePaint.setAntiAlias(true);
		textPaint.setColor(textColor);
		textPaint.setTextSize(textSize);
		
		//Pre-calculate some sizes
		density = getResources().getDisplayMetrics().density;
		height *= density;
		textSize *= density;

		//Setup the view's background
		rect = new Rect(0,0,screenWidth,height);
	}
	
	//Allow elevations to be added from the external environment
	public void updateElevation(int elevation)
	{
		elevations.add(elevation);
		if (elevation < minElevation) minElevation = elevation;
		if (elevation > maxElevation) maxElevation = elevation;
		if (BuildConfig.DEBUG) Log.d("", "added: "+ elevation);
		this.invalidate();
	}
	
	//The real drawing happens here
	@Override 
	protected void onDraw(Canvas canvas)   
	{ 
		super.onDraw(canvas); 

		//Draw the background
		canvas.drawRect(rect, outlinePaint);
		
		if (BuildConfig.DEBUG) Log.d("", elevations.toString());

		int lineLength = 0;
		
		//Start plotting on the left
		int startX = 0;

		//As long as we have some elevations to plot...
		if (elevations.size() > 0)
		{
			//Give each elevation equal space on the screen
			lineLength = screenWidth / elevations.size();
		
			
			//Plot each elevation line
			for (int elevation : elevations)
			{
				//Normalize all elevations to be zero-based
				int y = elevation - minElevation;
				//Make the total height range proportional to the height of the view
				int addOneToAvoidDivByZero = 1;
				y = y * (height - borderSize) / (maxElevation - minElevation + addOneToAvoidDivByZero);
				//Turn it upside down becuase 0,0 is at the top instead of at the bottom
				y = height - y;
				
				//If this is the first line, just make it horizontal
				if (lastY == Integer.MIN_VALUE) lastY = y;
				
				//Draw the elevation line
				canvas.drawLine(startX, lastY, startX + lineLength, y, linePaint);				
				if (BuildConfig.DEBUG) Log.d("",""+startX+", "+lastY+", " + (startX+lineLength)+", " + y);
				
				//Update the next line's starting position
				lastY = y;
				startX += lineLength;
				
				if (BuildConfig.DEBUG) Log.d(TAG_GRAPH, "y: " + y);
			}
			
			//Reset the initial y coordinate for the first elevation plot
			//This will make it ready to plot when this method is called again
			lastY = Integer.MIN_VALUE;
			
			if (BuildConfig.DEBUG) Log.d(TAG_GRAPH, "Min: " + minElevation + ", Max: " + maxElevation);
		}		
	}
	
	//Standard view override for requesting the needed space on a screen
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(screenWidth, height + borderSize);
	}
}
