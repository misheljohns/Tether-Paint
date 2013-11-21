package com.tether.paint;


import android.view.ScaleGestureDetector;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;

public class DrawingView extends View {
	
	private static final float TETHER_BRUSHSIZE_SCALE = 0.50f;
	private static final float TETHER_BRUSHSIZE_ZERO = 40.0f;
	private static final float TETHER_ZOOM_SCALE = 0.005f;
	private static float TETHER_DRAW_SCALE = 60.0f;
	private static float TETHER_X_ZERO = 350.0f;
	private static float TETHER_Y_ZERO = 400.0f;
	private static final float TETHER_PAN_SCALE = 0.2f;
	
	private static String TAG = "tether.paint.drawview";
	
	private boolean tetherMode; 
	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint, cursorPaint;
	//initial color
	private int paintColor = 0xFF660000;
	//canvas
	private Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;
	
	private float brushSize, lastBrushSize;
	
	private boolean erase=false;
	
	private float X,Y,Z;
	
	private boolean pressed=false;
	private boolean ispressed=false;
	private boolean panzoom=false;
	private boolean ispanzoomed=false;
	
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
    private float mScaleFocusX;
    private float mScaleFocusY;
    private float mFocusX = 0.0f;
    private float mFocusY = 0.0f;
    private float mFocusZ = 0.0f;
    private float mPanX = 0.0f;
    private float mPanY = 0.0f;

	public DrawingView(Context context, AttributeSet attrs){
	    super(context, attrs);
	    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	    setupDrawing();
	}
	
	private void setupDrawing(){
		//get drawing area setup for interaction

		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
		
		drawPath = new Path();
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
				
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		
		cursorPaint = new Paint(drawPaint);	
		cursorPaint.setStrokeWidth(5);
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
		
		tetherMode = false;
		X = Y = Z = 0;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	//view given size
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
		TETHER_X_ZERO = w/2;
		TETHER_Y_ZERO = h/2;
		TETHER_DRAW_SCALE = Math.max(w, h)/(20);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	//draw view
		//Log.v(TAG,"height" + canvas.getHeight() + "width" + canvas.getWidth());
		canvas.save();
		canvas.scale(mScaleFactor, mScaleFactor, mScaleFocusX, mScaleFocusY);
		canvas.translate(mPanX, mPanY);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), cursorPaint);
		canvas.drawPath(drawPath, drawPaint);
		if(tetherMode) {
			//cursorPaint.setStrokeWidth(5);
			//canvas.drawCircle((float)X, (float) Y, (float)100.0, cursorPaint);
			canvas.drawCircle((float)X, (float) Y, brushSize, cursorPaint);
		}
		canvas.restore();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	//detect user touch
		/*
		if(tetherMode) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			    drawPath.moveTo((float) X, (float) Y);
			    pressed = true;
			    break;
			case MotionEvent.ACTION_UP:
			    drawCanvas.drawPath(drawPath, drawPaint);
			    drawPath.reset();
			    pressed = false;
			    break;
			default:
			}
		}*/
		if(!tetherMode) {
			mScaleDetector.onTouchEvent(event);
			if(event.getPointerCount() == 1) {
				float touchX = event.getX();
				float touchY = event.getY();
				X = touchX;
				Y = touchY;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					drawPath.moveTo(touchX, touchY);
				    break;
				case MotionEvent.ACTION_MOVE:
				    drawPath.lineTo(touchX, touchY);
				    break;
				case MotionEvent.ACTION_UP:
				    drawCanvas.drawPath(drawPath, drawPaint);
				    drawPath.reset();
				    break;
				default:
				    return false;
				}
			}
		}		
		invalidate();
		return true;
	}
	
	public void setColor(String newColor) {
		//set color
		invalidate();
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
		cursorPaint.setColor(paintColor);
	}
	
	public void setBrushSize(float newSize) {
		//update size
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
			    newSize, getResources().getDisplayMetrics());
			brushSize=pixelAmount;
			drawPaint.setStrokeWidth(brushSize);
	}
	
	public void setLastBrushSize(float lastSize){
	    lastBrushSize=lastSize;
	}
	
	public float getLastBrushSize(){
	    return lastBrushSize;
	}
	
	public void setErase(boolean isErase){
		//set erase true or false
		erase=isErase;
		if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else drawPaint.setXfermode(null);
	}
	
	public void startNew(){
	    drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    invalidate();
	}
	
	public void setMode(boolean mode) {
		tetherMode = mode;
	}
	
	public void setCoords(double newX, double newY, double newZ) {
		X = (float) newX*TETHER_DRAW_SCALE + TETHER_X_ZERO;
		Y = (float) -newY*TETHER_DRAW_SCALE + TETHER_Y_ZERO;
		Z = (float) newZ;
		
		if(tetherMode) { 
			/*
			if(mScaleFactor > 1) {
				X = X + (X - mScaleFocusX)*(mScaleFactor - 1);
				Y = Y + (Y - mScaleFocusY)*(mScaleFactor - 1);
			}
			else if(mScaleFactor < 1) {
				X = X - (X - mScaleFocusX)*(1 - mScaleFactor);
				Y = Y - (Y - mScaleFocusY)*(1 - mScaleFactor);
			}*/
			X = mScaleFocusX + (X - mScaleFocusX)/mScaleFactor;
			Y = mScaleFocusY + (Y - mScaleFocusY)/mScaleFactor;
			if(pressed) {	
				setBrushSize((float)Math.abs((TETHER_BRUSHSIZE_ZERO-Z)*TETHER_BRUSHSIZE_SCALE/mScaleFactor));
				if(ispressed) {
					drawPath.lineTo(X, Y);
				    drawCanvas.drawPath(drawPath, drawPaint);
				    drawPath.reset();
				}
			    drawPath.moveTo(X, Y);
			}
			else if(panzoom && !ispanzoomed) { //starting zoom
				mScaleFocusX = mFocusX = X;
				mScaleFocusY = mFocusY = Y;
				mFocusZ = Z;
			}
			else if(panzoom) { //continuing zoom
				float cScaleFactor = (1 + (Z-mFocusZ)*TETHER_ZOOM_SCALE);
				mScaleFactor = mScaleFactor*cScaleFactor;
				mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
				/*
				if((cScaleFactor > 1.001)||(cScaleFactor < 0.999)) {
					mScaleFocusX = (mScaleFocusX*(1-cScaleFactor) + mFocusX*(cScaleFactor - 1/cScaleFactor))/(1-1/cScaleFactor);
					mScaleFocusY = (mScaleFocusY*(1-cScaleFactor) + mFocusY*(cScaleFactor - 1/cScaleFactor))/(1-1/cScaleFactor);
				}
				else {
					mScaleFocusX = mFocusX;
					mScaleFocusY = mFocusY;
				} */
				
				mPanX = (X - mFocusX)*TETHER_PAN_SCALE;
				mPanY = (Y - mFocusY)*TETHER_PAN_SCALE;
			}
			else {
				setBrushSize((float)Math.abs((TETHER_BRUSHSIZE_ZERO-Z)*TETHER_BRUSHSIZE_SCALE/mScaleFactor));
			}
			ispressed = pressed;
			ispanzoomed = panzoom;
		}
		invalidate();
	}
	
	public void zeroView() {
		mScaleFactor = 1;
		mScaleFocusX = 0;
		mScaleFocusY = 0;
	}
	
	public void setTetherDraw(boolean mode) {
		pressed = mode;
	}
	
	public void setTetherPanZoom(boolean mode) {
		panzoom = mode;
	}

	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
		    mScaleFactor *= detector.getScaleFactor();
	        mScaleFocusX = detector.getFocusX();
	        mScaleFocusY = detector.getFocusY();
		
		    // Don't let the object get too small or too large.
		    mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
		
		    invalidate();
		    return true;
		}
	}

	
}
