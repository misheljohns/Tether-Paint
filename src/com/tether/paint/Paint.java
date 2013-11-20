package com.tether.paint;

import java.util.UUID;

import tether.Tether;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Paint extends Activity implements OnClickListener {
	
	// Log tag
	private static String TAG = "tether.paint";
	
	// Address of the device
	private String TETHER_ADDRESS = "00:06:66:4E:44:F5";
	
	// Tether object
	private Tether tether;
	
	// Drawing canvas view
	public DrawingView drawView;
	
	// Toolbar buttnos
	private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;
	
	// Brush sizes
	private float smallBrush, mediumBrush, largeBrush;

	// Handler for this Tether
	private static final class TetherHandler extends Handler {
		
		private final Paint activity;
		private TetherHandler(Paint a) { activity = a; }
		boolean pressed1, pressed2;
		@Override
		public void handleMessage(Message msg) {
			
			Bundle b = msg.getData();
			
			switch (msg.what) {
				case Tether.CONNECTED:
					activity.tetherConnected();
					break;
				case Tether.DISCONNECTED:
					activity.tetherDisconnected();
					break;
				case Tether.POSITION_UPDATE:
					double X = b.getDouble("X");
					double Y = b.getDouble("Y");
					double Z = b.getDouble("Z");
					activity.tetherPositionUpdated(X, Y, Z);
					break;
				case Tether.AOK:
					//activity.showToast(b.getString("INFO"));
					break;
				case Tether.ERROR:
					activity.showToast(b.getString("INFO"));
					break;
				case Tether.BUTTON_1:
					pressed1 = b.getBoolean("PRESSED");
					/*
					if(pressed1 && pressed2)
						activity.tether.sendCommand("TRACKING 0");
					else
						activity.tether.sendCommand("TRACKING 1");
						*/
					if(pressed1 && !pressed2) {
						activity.drawView.setTetherDraw(pressed1);
					}
					break;
				case Tether.BUTTON_2:
					pressed2 = b.getBoolean("PRESSED");
					if(pressed2 && !pressed1) {
						activity.drawView.setTetherPanZoom(pressed2);
					}
					break;
				default:
					Log.w(TAG, "Received unprocessed message id from libtether: " + msg.what);
					break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paint);
		
		// Don't turn off the screen
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Create a Tether object and set the Handler
		tether = new Tether(TETHER_ADDRESS);
		tether.setHandler(new TetherHandler(this));
		tether.begin();
		
		Log.v(TAG, "my tether: " + tether);
		
		drawView = (DrawingView)findViewById(R.id.drawing);
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
		
		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);
		
		drawBtn = (ImageButton)findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);
		
		eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);
		
		newBtn = (ImageButton)findViewById(R.id.new_btn);
		newBtn.setOnClickListener(this);
		
		saveBtn = (ImageButton)findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
		
		//tetherDraw = (Button)findViewById(R.id.tethDrawButton);
		
		drawView.setBrushSize(mediumBrush);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.paint, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item1)
    {
    	switch(item1.getItemId())
    	{
    	case R.id.action_settings:
    		return true;
    	case R.id.connect:
    		Log.v(TAG, "Starting tether.");
    		tether.begin();
    		drawView.setMode(true);
    		return true;
    	case R.id.disconnect:
    		Log.v(TAG, "Stopping tether.");
    		tether.end(); 
    		drawView.setMode(false);
    		return true;
    	case R.id.dispCoords:
    		double X = tether.X();
    		double Y = tether.Y();
    		double Z = tether.Z();
    		Toast savedToast = Toast.makeText(getApplicationContext(),"X: " + X + ", Y: " + Y + ", Z: " + Z, Toast.LENGTH_SHORT);
	    	savedToast.show();
	    	return true;
    	}
    	return true;
    }
	
	public void paintClicked(View view){
		drawView.setErase(false);
		drawView.setBrushSize(drawView.getLastBrushSize());
	    //use chosen color
		if(view!=currPaint){
			//update color
			ImageButton imgView = (ImageButton)view;
			String color = view.getTag().toString();
			drawView.setColor(color);
			
			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint=(ImageButton)view;
			}
	}
	
	public void showToast(String str) {
	    Toast toast = Toast.makeText(getApplicationContext(),
    	        str, Toast.LENGTH_SHORT);
    	toast.show();
	}
	
	public void onClick(View view){
		Log.v(TAG, "on click called on : " + view);
		if(view.getId()==R.id.draw_btn){
		    //draw button clicked
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Brush size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
			    @Override
			    public void onClick(View v) {
			    	drawView.setErase(false);
			        drawView.setBrushSize(smallBrush);
			        drawView.setLastBrushSize(smallBrush);
			        brushDialog.dismiss();
			    }
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
			    @Override
			    public void onClick(View v) {
			    	drawView.setErase(false);
			        drawView.setBrushSize(mediumBrush);
			        drawView.setLastBrushSize(mediumBrush);
			        brushDialog.dismiss();
			    }
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
			    @Override
			    public void onClick(View v) {
			    	drawView.setErase(false);
			        drawView.setBrushSize(largeBrush);
			        drawView.setLastBrushSize(largeBrush);
			        brushDialog.dismiss();
			    }
			});
			
			brushDialog.show();
		}
		else if(view.getId()==R.id.erase_btn){
		    //switch to erase - choose size
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Eraser size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
			    @Override
			    public void onClick(View v) {
			        drawView.setErase(true);
			        drawView.setBrushSize(smallBrush);
			        brushDialog.dismiss();
			    }
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
			    @Override
			    public void onClick(View v) {
			        drawView.setErase(true);
			        drawView.setBrushSize(mediumBrush);
			        brushDialog.dismiss();
			    }
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
			    @Override
			    public void onClick(View v) {
			        drawView.setErase(true);
			        drawView.setBrushSize(largeBrush);
			        brushDialog.dismiss();
			    }
			});
			
			brushDialog.show();
		}
		else if(view.getId()==R.id.new_btn){
		    //new button
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("New drawing");
			newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
			newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			    public void onClick(DialogInterface dialog, int which){
			        drawView.startNew();
			        dialog.dismiss();
			    }
			});
			newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			    public void onClick(DialogInterface dialog, int which){
			        dialog.cancel();
			    }
			});
			newDialog.show();
		}
		else if(view.getId()==R.id.save_btn){
            //save drawing
			AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
			saveDialog.setTitle("Save drawing");
			saveDialog.setMessage("Save drawing to device Gallery?");
			saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			    public void onClick(DialogInterface dialog, int which){
			        //save drawing
			    	drawView.setDrawingCacheEnabled(true);
			    	String imgSaved = MediaStore.Images.Media.insertImage(
			    		    getContentResolver(), drawView.getDrawingCache(),
			    		    UUID.randomUUID().toString()+".png", "drawing");
			    	if(imgSaved!=null){
			    	    Toast savedToast = Toast.makeText(getApplicationContext(),
			    	        "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
			    	    savedToast.show();
			    	}
			    	else{
			    	    Toast unsavedToast = Toast.makeText(getApplicationContext(),
			    	        "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
			    	    unsavedToast.show();
			    	}
			    	drawView.destroyDrawingCache();
			    }
			});
			saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			    public void onClick(DialogInterface dialog, int which){
			        dialog.cancel();
			    }
			});
			saveDialog.show();
		}
	}

	public void tetherConnected() {
    	Toast connectedT = Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT);
 		connectedT.show();
		
	}

	public void tetherDisconnected() {
    	 Toast disconnectedT = Toast.makeText(getApplicationContext(), "Disconnected!", Toast.LENGTH_SHORT);
 		 disconnectedT.show();
	}

	public void tetherPositionUpdated(double X, double Y, double Z) {
	   	drawView.setCoords(X, Y, Z);
   	    drawView.invalidate();
	}
	
	@Override
	protected void onStop() {
		tether.end();
		super.onStop();		
	}

}
