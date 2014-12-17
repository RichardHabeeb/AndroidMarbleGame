package com.example.richardhabeeb.marblegame;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.view.*;
import android.graphics.*;
import java.util.Date;

/**
 * TODO: document your custom view class.
 */
public class Game extends View implements SensorEventListener  {
    private PointF sensorValues;
    private WindowManager windowManager;
    private Bitmap ballBitmap;
    private Date sensorTimeStamp;
    private ParticleSystem particles;
    public PointF ballOffsetOrigin;
    public PointF origin;
    public static float pixels_per_meter_x;
    public static float pixels_per_meter_y;

    private Rect screenCover;
    private Paint screenCoverPaint;
    private Paint textPaint;
    private boolean paused;

    public Game(Context context, WindowManager windowManager, SensorManager sensorManager ) {
        super(context);

        SensorManager sensorManager1 = sensorManager;
        this.windowManager = windowManager;

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager1.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        ballOffsetOrigin = new PointF();
        origin = new PointF();
        sensorValues = new PointF();
        sensorTimeStamp = new Date();

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        pixels_per_meter_x = metrics.xdpi / 0.0254f;
        pixels_per_meter_y = metrics.ydpi / 0.0254f;

        ballBitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.ball),
                (int)(Particle.BALL_DIAMETER_PX),
                (int)(Particle.BALL_DIAMETER_PX), false);

        Particle.BALL_DIAMETER = ballBitmap.getHeight() / pixels_per_meter_y;



        screenCover = new Rect(0,0,0,0);
        screenCoverPaint = new Paint();
        screenCoverPaint.setAlpha(99);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);

        paused = true;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Compute the new position of our object, based on accelerometer
        // data and present time.
        particles.Update(sensorValues.x, sensorValues.y, sensorTimeStamp.getTime()*100000); //ms to ticks?

        Particle ball = particles.ball;

        // We transform the canvas so that the coordinate system matches
        // the sensors coordinate system with the origin in the center
        // of the screen and the unit is the meter.
        float x = ballOffsetOrigin.x + ball.getLocation().x * pixels_per_meter_x;
        float y = ballOffsetOrigin.y - ball.getLocation().y * pixels_per_meter_y;

        canvas.drawBitmap(ballBitmap, x, y, null);


        for(int i = 0; i < particles.Walls.size(); i++) {
            Wall wall = particles.Walls.get(i);

            canvas.drawRect(wall.rect, wall.paint);
        }

        canvas.drawRect(particles.exit.rect, particles.exit.paint);

        if(paused) {
            canvas.drawRect(screenCover, screenCoverPaint);
            canvas.drawText("Tap the screen to start!", canvas.getWidth()/2, canvas.getHeight()/2, textPaint);
        }

        // Make sure to redraw asap
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {

            paused = !paused;
        }
        return true;
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        // Compute the origin of the screen relative
        // to the origin of the bitmap

        screenCover.bottom = h;
        screenCover.right = w;

        ballOffsetOrigin.set((w - ballBitmap.getWidth()) * 0.5f, (h - ballBitmap.getHeight()) * 0.5f);
        origin.set(w * 0.5f, h * 0.5f);

        if(particles == null)
        {
            particles = new ParticleSystem(this);
        }

        particles.Bounds.x = (((float) w / pixels_per_meter_x - Particle.BALL_DIAMETER) * 0.5f);
        particles.Bounds.y = (((float) h / pixels_per_meter_y - Particle.BALL_DIAMETER) * 0.5f);


    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        sensorTimeStamp = new Date();

        switch (windowManager.getDefaultDisplay().getRotation()) {
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
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean getPaused() {
        return paused;
    }

    public void setPaused(boolean p) {
        paused = p;
    }
}
