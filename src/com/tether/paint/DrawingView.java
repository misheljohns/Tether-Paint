package com.tether.paint;

import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
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
	
	private double X,Y,Z;
	
	private boolean pressed=false;

	public DrawingView(Context context, AttributeSet attrs){
	    super(context, attrs);
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
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	//draw view
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
		cursorPaint.setStrokeWidth(5);
		//canvas.drawCircle((float)X, (float) Y, (float)100.0, cursorPaint);
		canvas.drawCircle((float)X, (float) Y, brushSize, cursorPaint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	//detect user touch
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
		}
		else {
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
		X = newX*100 + 200;
		Y = newY*100 + 200;
		Z = newZ;
		
		if(tetherMode) { 
			setBrushSize((float)Math.abs((20-Z)*5));
		}
		if(pressed){
			drawPath.lineTo((float) X, (float) Y);
		}
		invalidate();
	}

	
}
