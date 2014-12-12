package com.example.richardhabeeb.marblegame;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.BoringLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import java.util.Date;

/**
 * TODO: document your custom view class.
 */
public class Game extends View implements SensorEventListener  {
    private PointF sensorValues;
    private WindowManager windowManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Bitmap background;
    private Bitmap ballBitmap;
    private Date sensorTimeStamp;
    private ParticleSystem particles;
    private PointF origin;
    private float meters_to_pixels_x;
    private float meters_to_pixels_y;

    public Game(Context context, WindowManager windowManager, SensorManager sensorManager ) {
        super(context);

        this.sensorManager = sensorManager;
        this.windowManager = windowManager;

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        background = BitmapFactory.decodeResource(getResources(), R.drawable.icecream);
        ballBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);

        particles = new ParticleSystem(this);
        origin = new PointF();
        sensorValues = new PointF();
        sensorTimeStamp = new Date();

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        meters_to_pixels_x = metrics.xdpi / 0.0254f;
        meters_to_pixels_y = metrics.ydpi / 0.0254f;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(background, 0, 0, null);
        // Compute the new position of our object, based on accelerometer
        // data and present time.
        particles.Update(sensorValues.x, sensorValues.y, sensorTimeStamp.getTime()*10000); //ms to ticks?

        for(int i = 0; i < particles.Balls.size(); i++) {
            Particle ball = particles.Balls.get(i);

            // We transform the canvas so that the coordinate system matches
            // the sensors coordinate system with the origin in the center
            // of the screen and the unit is the meter.
            float x = origin.x + ball.getLocation().x * meters_to_pixels_x;
            float y = origin.x - ball.getLocation().y * meters_to_pixels_y;

            canvas.drawBitmap(ballBitmap, x, y, null);
        }

        // Make sure to redraw asap
        invalidate();
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        // Compute the origin of the screen relative
        // to the origin of the bitmap
        origin.set((w - ballBitmap.getWidth()) * 0.5f, (h - ballBitmap.getHeight()) * 0.5f);

        particles.Bounds.x = (((float) w / meters_to_pixels_x - Particle.BALL_DIAMETER) * 0.5f);
        particles.Bounds.y = (((float) h / meters_to_pixels_y - Particle.BALL_DIAMETER) * 0.5f);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        sensorTimeStamp = new Date();

        switch(windowManager.getDefaultDisplay().getRotation())
        {
            case Surface.ROTATION_0:
                sensorValues.set(event.values[0], event.values[1]);
                break;
            case Surface.ROTATION_90:
                sensorValues.set(-event.values[1], event.values[0]);
                break;
            case Surface.ROTATION_180:
                sensorValues.set(-event.values[0], -event.values[1]);
                break;
            case Surface.ROTATION_270:
                sensorValues.set(event.values[1], -event.values[0]);
                break;
            default:
                return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
