package com.example.richardhabeeb.marblegame; /**
 * Created by richardhabeeb on 12/11/14.
 */
import android.graphics.*;
import java.util.*;

public class Particle {
    public static float BALL_DIAMETER;
    public static float BALL_DIAMETER_PX = 50.0f;


    private ParticleSystem system;
    private PointF prev_location = new PointF ();
    private PointF accel = new PointF ();

    private PointF Location = new PointF();

    public Particle (ParticleSystem system, PointF startingLocation)
    {
        this.system = system;
        resetLocation(startingLocation);
    }

    public PointF getLocation()
    {
        return Location;
    }

    public void resetLocation(PointF startLocation) {
        Location.set(startLocation.x, startLocation.y);
        prev_location.set(startLocation.x, startLocation.y);
        accel = new PointF();
    }

    public void ComputePhysics (float sx, float sy, float dT, float dTC)
    {
        // Force of gravity applied to our virtual object
        float m = 1000.0f; // mass of our virtual object
        float gx = -sx * m;
        float gy = -sy * m;

        // ·F = mA <=> A = ·F / m We could simplify the code by
        // completely eliminating "m" (the mass) from all the equations,
        // but it would hide the concepts from this sample code.
        float invm = 1.0f / m;
        float ax = gx * invm;
        float ay = gy * invm;

        // Time-corrected Verlet integration The position Verlet
        // integrator is defined as x(t+Æt) = x(t) + x(t) - x(t-Æt) +
        // a(t)Ætö2 However, the above equation doesn't handle variable
        // Æt very well, a time-corrected version is needed: x(t+Æt) =
        // x(t) + (x(t) - x(t-Æt)) * (Æt/Æt_prev) + a(t)Ætö2 We also add
        // a simple friction term (f) to the equation: x(t+Æt) = x(t) +
        // (1-f) * (x(t) - x(t-Æt)) * (Æt/Æt_prev) + a(t)Ætö2

        float dTdT = dT * dT;
        float x = Location.x + dTC * (Location.x - prev_location.x) + accel.x * dTdT;
        float y = Location.y + dTC * (Location.y - prev_location.y) + accel.y * dTdT;


        prev_location.set(Location);
        Location.set(x, y);
        accel.set(ax, ay);
    }

    // Resolving constraints and collisions with the Verlet integrator
    // can be very simple, we simply need to move a colliding or
    // constrained particle in such way that the constraint is
    // satisfied.
    public void ResolveCollisionWithBounds ()
    {
        float xmax = system.Bounds.x;
        float ymax = system.Bounds.y;
        float x = Location.x;
        float y = Location.y;

        if (x > xmax)
            Location.x = xmax;
        else if (x < -xmax)
            Location.x = -xmax;

        if (y > ymax)
            Location.y = ymax;
        else if (y < -ymax)
            Location.y = -ymax;
    }

}
