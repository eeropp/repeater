package com.mozeeza.repeater;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

interface DashBoardState {
	DashBoardState onTouchEvent(MotionEvent event);
	void draw(Canvas canvas);
}

interface DashBoardDrawable {
	public void drawTitle(Canvas canvas);
	public void drawTimePlayed(Canvas canvas);
	public void drawTimeTotal(Canvas canvas);
	public void drawProgress(Canvas canvas);
	public void drawProgressPassed(Canvas canvas);
	public void drawProgressThumb(Canvas canvas);
	public void drawProgressThumbPressed(Canvas canvas);
	public void drawProgressThumbA(Canvas canvas);
	public void drawProgressThumbAPressed(Canvas canvas);
	public void drawProgressThumbB(Canvas canvas);
	public void drawProgressThumbBPressed(Canvas canvas);
	public void drawCenter(Canvas canvas);
	public void drawCenterTracked(Canvas canvas);
	public void drawCenterThumbPressed(Canvas canvas);
	public void drawCenterTime(Canvas canvas);
	public void drawButtonPlay(Canvas canvas);
	public void drawButtonPlayPressed(Canvas canvas);
	public void drawButtonPause(Canvas canvas);
	public void drawButtonPausePressed(Canvas canvas);
	public void drawButtonA(Canvas canvas);
	public void drawButtonAPressed(Canvas canvas);
	public void drawButtonB(Canvas canvas);
	public void drawButtonBPressed(Canvas canvas);
	public void drawVolume(Canvas canvas);
	public void drawVolumePassed(Canvas canvas);
	public void drawVolumeThumb(Canvas canvas);
	public void drawVolumeThumbPressed(Canvas canvas);
	public void drawMouseTrack(Canvas canvas);
}


class DashBoard extends View implements DashBoardDrawable {
	public final double m_minorAngle = (1 - 0.618) * 90;
	public final double m_largerAngle = 0.618 * 90;
	public final double m_volStartAngle = 90;
	public final double m_volSweepAngle = -m_largerAngle;
	public final double m_prgStartAngle = m_minorAngle + 180;
	public final double m_prgSweepAngle = 2 * m_largerAngle;
	private double m_diameterOutter = 0;
	private double m_radiusOutter = 0;
	private double m_xCenter = 0;
	private double m_yCenter = 0;
	private double m_radiusInner = 0;
	private RectF m_rcOutterCircle = new RectF();
	
	private Path m_pathProgress = new Path();
	private Path m_pathProgressPassed = new Path();
	private double m_xProgressThumb = 0;
	private double m_yProgressThumb = 0;
	
	private float m_radiusThumbPressed = 25;
	private float m_radiusThumb = 8;
	
	private double m_xProgressThumbA = 0;
	private double m_yProgressThumbA = 0;
	
	private double m_xProgressThumbB = 0;
	private double m_yProgressThumbB = 0;
	
	private Path m_pathVolume = new Path();
	private Path m_pathVolumePassed = new Path();
	private double m_xVolumeThumb = 0;
	private double m_yVolumeThumb = 0;
	
	private Path m_pathInnerCircle = new Path();
	
	private double m_xButtonA = 0;
	private double m_yButtonA = 0;
	private double m_txtSizeA = 0;
	private double m_xButtonB = 0;
	private double m_yButtonB = 0;
	private double m_txtSizeB = 0;
	
	private double m_progress = 0;
	
	private float m_xPlayBtn[] = new float[3];
	private float m_yPlayBtn[] = new float[3];
	private float m_xPauseBtn[] = new float[4];
	private float m_yPauseBtn[] = new float[4];
	private Path m_pathPlayBtn = new Path();
	private Path m_pathPauseBtn = new Path();
	
	public DashBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setPadding(4, 4, 4, 4);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		double width = w - getPaddingLeft() - getPaddingRight();
		double height = h - getPaddingTop() - getPaddingBottom();
		m_diameterOutter = width / Math.cos(m_minorAngle * Math.PI / 180);
		m_radiusOutter = m_diameterOutter / 2;
		m_xCenter = this.getPaddingLeft() + width/2;
		m_yCenter = this.getPaddingTop() + height/2;

		m_rcOutterCircle.left = (float) (m_xCenter - m_radiusOutter);
		m_rcOutterCircle.top = (float) (m_yCenter - m_radiusOutter);
		m_rcOutterCircle.right = (float) (m_xCenter + m_radiusOutter);
		m_rcOutterCircle.bottom = (float) (m_yCenter + m_radiusOutter);
		
		m_pathProgress.reset();
		m_pathProgress.addArc(m_rcOutterCircle, (float)m_prgStartAngle, (float)m_prgSweepAngle);
		
		m_pathVolume.reset();
		m_pathVolume.addArc(m_rcOutterCircle, (float)m_volStartAngle, (float)m_volSweepAngle);
		
		m_pathInnerCircle.reset();
		m_radiusInner = m_radiusOutter * 0.618;
		m_pathInnerCircle.addCircle((float)m_xCenter, (float)m_yCenter, (float) m_radiusInner, Path.Direction.CCW);
		
		double radianA = (m_volStartAngle + m_largerAngle * 0.85) * Math.PI / 180;
		m_xButtonA = m_xCenter + m_radiusOutter * Math.cos(radianA);
		m_yButtonA = m_yCenter + m_radiusOutter * Math.sin(radianA);
		
		double radiaB = (m_volStartAngle + m_largerAngle * 0.35) * Math.PI / 180;
		m_xButtonB = m_xCenter + m_radiusOutter * Math.cos(radiaB);
		m_yButtonB = m_yCenter + m_radiusOutter * Math.sin(radiaB);
		
		m_txtSizeA = (m_xButtonB - m_xButtonA) * 0.618;
		m_txtSizeB = m_txtSizeA;
		m_yButtonA += m_txtSizeA / 2;
		m_yButtonB += m_txtSizeB / 2;
		
		double radiusBtn = m_radiusOutter * 0.618 * (1 - 0.618);
		m_xPlayBtn[0] = (float) (m_xCenter + radiusBtn);
		m_yPlayBtn[0] = (float) m_yCenter;
		m_xPlayBtn[1] = (float) (m_xCenter + radiusBtn * Math.cos(120.0 * Math.PI / 180));
		m_yPlayBtn[1] = (float) (m_yCenter + radiusBtn * Math.sin(120.0 * Math.PI / 180));
		m_xPlayBtn[2] = (float) (m_xCenter + radiusBtn * Math.cos(120.0 * Math.PI / 180));
		m_yPlayBtn[2] = (float) (m_yCenter + radiusBtn * Math.sin(-120.0 * Math.PI / 180));

		m_xPauseBtn[0] = m_xPlayBtn[1];
		m_yPauseBtn[0] = m_yPlayBtn[1];
		m_xPauseBtn[1] = m_xPlayBtn[2];
		m_yPauseBtn[1] = m_yPlayBtn[2];
		m_xPauseBtn[2] = (float) (2 * m_xCenter - m_xPauseBtn[0]);
		m_yPauseBtn[2] = m_yPauseBtn[0];
		m_xPauseBtn[3] = (float) (2 * m_xCenter - m_xPauseBtn[1]);
		m_yPauseBtn[3] = m_yPauseBtn[1];
		
		m_pathPlayBtn.moveTo(m_xPlayBtn[0], m_yPlayBtn[0]);
		m_pathPlayBtn.lineTo(m_xPlayBtn[1], m_yPlayBtn[1]);
		m_pathPlayBtn.lineTo(m_xPlayBtn[2], m_yPlayBtn[2]);
		m_pathPlayBtn.lineTo(m_xPlayBtn[0], m_yPlayBtn[0]);
		
		m_pathPauseBtn.moveTo(m_xPauseBtn[0], m_yPauseBtn[0]);
		m_pathPauseBtn.lineTo(m_xPauseBtn[1], m_yPauseBtn[1]);
		m_pathPauseBtn.moveTo(m_xPauseBtn[2], m_yPauseBtn[2]);
		m_pathPauseBtn.lineTo(m_xPauseBtn[3], m_yPauseBtn[3]);
		
		onProgressSizeChange();
		onVolumeSizeChange();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		drawTitle(canvas);
		drawTimePlayed(canvas);
		drawTimeTotal(canvas);
		drawProgress(canvas);
		drawProgressPassed(canvas);
		drawProgressThumb(canvas);
		drawProgressThumbPressed(canvas);
		drawProgressThumbA(canvas);
		drawProgressThumbAPressed(canvas);
		drawProgressThumbB(canvas);
		drawProgressThumbBPressed(canvas);
		drawCenter(canvas);
		drawCenterTracked(canvas);
		drawCenterThumbPressed(canvas);
		drawCenterTime(canvas);
		drawButtonPlay(canvas);
		drawButtonPlayPressed(canvas);
		drawButtonPause(canvas);
		drawButtonPausePressed(canvas);
		drawButtonA(canvas);
		drawButtonAPressed(canvas);
		drawButtonB(canvas);
		drawButtonBPressed(canvas);
		drawVolume(canvas);
		drawVolumeThumb(canvas);
		drawVolumeThumbPressed(canvas);
		drawMouseTrack(canvas);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			double progress = getProgress();
			progress += 0.01;
			if (progress > 1.0)
				progress = 0;
			setProgress(progress);
			setPlayState(getPlayState() == 1? 0:1);
		}
		*/
		return true;
	}
	
	protected void onProgressSizeChange() {
		double angPrg = m_prgStartAngle + m_prgSweepAngle * getProgress();
		m_xProgressThumb = m_xCenter + m_radiusOutter * Math.cos(angPrg * Math.PI / 180);
		m_yProgressThumb = m_yCenter + m_radiusOutter * Math.sin(angPrg * Math.PI / 180);
		m_pathProgressPassed.reset();
		m_pathProgressPassed.addArc(m_rcOutterCircle, (float)m_prgStartAngle, (float)(m_prgSweepAngle * getProgress()));
	
		double angPrgA = m_prgStartAngle + m_prgSweepAngle * 0.4;
		m_xProgressThumbA = m_xCenter + m_radiusOutter * Math.cos(angPrgA * Math.PI / 180);
		m_yProgressThumbA = m_yCenter + m_radiusOutter * Math.sin(angPrgA * Math.PI / 180);
		
		double angPrgB = m_prgStartAngle + m_prgSweepAngle * 0.7;
		m_xProgressThumbB = m_xCenter + m_radiusOutter * Math.cos(angPrgB * Math.PI / 180);
		m_yProgressThumbB = m_yCenter + m_radiusOutter * Math.sin(angPrgB * Math.PI / 180);
	}
	
	protected void onVolumeSizeChange() {
		double angVol = m_volStartAngle + m_volSweepAngle * getVolumn();
		m_xVolumeThumb = m_xCenter + m_radiusOutter * Math.cos(angVol * Math.PI / 180);
		m_yVolumeThumb =m_yCenter + m_radiusOutter * Math.sin(angVol * Math.PI / 180);
		m_pathVolumePassed.reset();
		m_pathVolumePassed.addArc(m_rcOutterCircle, (float)m_volStartAngle, (float)(m_volSweepAngle * getVolumn()));
	}
	
	protected void setProgress(double progress) {
		m_progress = progress;
		onProgressSizeChange();
		invalidate();
	}
	
	protected double getProgress() {
		return 0.2;
	}
	
	protected double getVolumn() {
		return 0.6;
	}
	
	protected int getPlayState() {
		return 0;
	}
	
	protected void setPlayState(int state) {
		
	}

	@Override
	public void drawTitle(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawTimePlayed(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawTimeTotal(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawProgress(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(255, 100, 100, 100);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(4);
		canvas.drawPath(m_pathProgress, paint);
	}

	@Override
	public void drawProgressPassed(Canvas canvas) {
		Paint paint =  new Paint();
		paint.setARGB(255, 255, 255, 255);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8);
		canvas.drawPath(m_pathProgressPassed, paint);
	}

	@Override
	public void drawProgressThumb(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(50, 255, 255, 255);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.FILL);
		
		canvas.drawCircle(
				(float)m_xProgressThumb, 
				(float)m_yProgressThumb, 
				m_radiusThumbPressed, 
				paint);
		
		paint.setARGB(255, 255, 255, 255);
		canvas.drawCircle(
				(float)m_xProgressThumb, 
				(float)m_yProgressThumb,
				8,
				paint);
	}

	@Override
	public void drawProgressThumbPressed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(50, 255, 255, 255);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		canvas.drawCircle(
				(float)m_xProgressThumb, 
				(float)m_yProgressThumb, 
				m_radiusThumbPressed, 
				paint);
		
		paint.setARGB(255, 255, 255, 255);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(
				(float)m_xProgressThumb, 
				(float)m_yProgressThumb,
				8,
				paint);
	}

	@Override
	public void drawProgressThumbA(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(200, 255, 0, 0);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		
		canvas.drawCircle(
				(float)m_xProgressThumbA, 
				(float)m_yProgressThumbA,
				m_radiusThumb,
				paint);
	}

	@Override
	public void drawProgressThumbAPressed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(50, 255, 255, 255);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		canvas.drawCircle(
				(float)m_xProgressThumbA, 
				(float)m_yProgressThumbA, 
				m_radiusThumbPressed, 
				paint);
		
		paint.setARGB(200, 255, 0, 0);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(
				(float)m_xProgressThumbA, 
				(float)m_yProgressThumbA,
				m_radiusThumb,
				paint);
	}

	@Override
	public void drawProgressThumbB(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(50, 0, 255, 0);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		
		canvas.drawCircle(
				(float)m_xProgressThumbB, 
				(float)m_yProgressThumbB,
				m_radiusThumb,
				paint);
	}

	@Override
	public void drawProgressThumbBPressed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(50, 255, 255, 255);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		canvas.drawCircle(
				(float)m_xProgressThumbB, 
				(float)m_yProgressThumbB, 
				m_radiusThumbPressed, 
				paint);
		
		paint.setARGB(200, 0, 255, 0);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(
				(float)m_xProgressThumbB, 
				(float)m_yProgressThumbB,
				m_radiusThumb,
				paint);
	}

	@Override
	public void drawCenter(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(255, 100, 100, 100);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8);
		paint.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.SOLID));
		canvas.drawPath(m_pathInnerCircle, paint);
	}

	@Override
	public void drawCenterTracked(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(255, 100, 100, 100);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8);
		paint.setMaskFilter(new BlurMaskFilter(20.0f, BlurMaskFilter.Blur.OUTER));
		canvas.drawPath(m_pathInnerCircle, paint);
	}

	@Override
	public void drawCenterThumbPressed(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawCenterTime(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawButtonPlay(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(128, 255, 255, 255);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8);
		canvas.drawPath(m_pathPlayBtn, paint);
	}

	@Override
	public void drawButtonPlayPressed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(128, 255, 255, 255);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8);
		canvas.drawPath(m_pathPlayBtn, paint);
	}

	@Override
	public void drawButtonPause(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(128, 255, 255, 255);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8);
		canvas.drawPath(m_pathPauseBtn, paint);
	}

	@Override
	public void drawButtonPausePressed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(128, 255, 255, 255);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8);
		canvas.drawPath(m_pathPauseBtn, paint);
	}

	@Override
	public void drawButtonA(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xFFFFFFFF);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize((float) m_txtSizeA);
		paint.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.OUTER));
		canvas.drawText("A", (float)m_xButtonA, (float)m_yButtonA, paint);
	}

	@Override
	public void drawButtonAPressed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xFFFFFFFF);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize((float) m_txtSizeA);
		paint.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.SOLID));
		canvas.drawText("A", (float)m_xButtonA, (float)m_yButtonA, paint);
	}

	@Override
	public void drawButtonB(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xFFFFFFFF);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize((float) m_txtSizeB);
		paint.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.OUTER));
		canvas.drawText("B", (float)m_xButtonB, (float)m_yButtonB, paint);
	}

	@Override
	public void drawButtonBPressed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xFFFFFFFF);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize((float) m_txtSizeB);
		paint.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.SOLID));
		canvas.drawText("B", (float)m_xButtonB, (float)m_yButtonB, paint);
	}

	@Override
	public void drawVolume(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(255, 100, 100, 100);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(4);
		paint.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.SOLID));
		canvas.drawPath(m_pathVolume, paint);
	}

	@Override
	public void drawVolumePassed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(8);
		paint.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.SOLID));
		paint.setARGB(255, 255, 255, 255);
		canvas.drawPath(m_pathVolumePassed, paint);
	}
	
	@Override
	public void drawVolumeThumb(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(200, 255, 255, 255);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		
		canvas.drawCircle(
				(float)m_xVolumeThumb, 
				(float)m_yVolumeThumb,
				m_radiusThumb,
				paint);
	}

	@Override
	public void drawVolumeThumbPressed(Canvas canvas) {
		Paint paint = new Paint();
		paint.setARGB(50, 255, 255, 255);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		canvas.drawCircle(
				(float)m_xVolumeThumb, 
				(float)m_yVolumeThumb, 
				m_radiusThumbPressed, 
				paint);
		
		paint.setARGB(200, 255, 255, 255);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(
				(float)m_xVolumeThumb, 
				(float)m_yVolumeThumb,
				m_radiusThumb,
				paint);
	}

	@Override
	public void drawMouseTrack(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}
}

