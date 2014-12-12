package com.example.richardhabeeb.marblegame;

import com.example.richardhabeeb.marblegame.Game;
import com.example.richardhabeeb.marblegame.Particle;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import android.graphics.PointF;

class ParticleSystem
{
    public long last_t;
    public float last_delta_t;

    static int NUM_PARTICLES = 1;
    public PointF Bounds = new PointF();

    public Game sim_view;

    public List<Particle> Balls;

    public ParticleSystem (Game view)
    {
        sim_view = view;
        Balls = new ArrayList<Particle> ();

        // Initially our particles have no speed or acceleration
        for (int i = 0; i < NUM_PARTICLES; i++)
            Balls.add(new Particle(this));
    }

    // Update the position of each particle in the system using the
    // Verlet integrator.
    private void UpdatePositions (float sx, float sy, long timestamp)
    {
        long t = timestamp;

        if (last_t != 0) {
            float dT = (float)(t - last_t) * (1.0f / 1000000000.0f);

            if (last_delta_t != 0) {
                float dTC = dT / last_delta_t;

                for(int i = 0; i < Balls.size(); i++) {
                    Balls.get(i).ComputePhysics(sx, sy, dT, dTC);
                }
            }

            last_delta_t = dT;
        }

        last_t = t;
    }

    // Performs one iteration of the simulation. First updating the
    // position of all the particles and resolving the constraints and
    // collisions.
    public void Update (float sx, float sy, long now)
    {
        // update the system's positions
        UpdatePositions (sx, sy, now);

        // We do no more than a limited number of iterations
        int NUM_MAX_ITERATIONS = 10;

        // Resolve collisions, each particle is tested against every
        // other particle for collision. If a collision is detected the
        // particle is moved away using a virtual spring of infinite
        // stiffness.
        Random random = new Random ();

        boolean more = true;

        for (int k = 0; k < NUM_MAX_ITERATIONS && more; k++) {
            more = false;

            for (int i = 0; i < Balls.size(); i++) {
                Particle curr = Balls.get(i);

                for (int j = i + 1; j < Balls.size(); j++) {
                    Particle ball = Balls.get(j);

                    float dx = ball.getLocation().x - curr.getLocation().x;
                    float dy = ball.getLocation().y - curr.getLocation().y;
                    float dd = dx * dx + dy * dy;

                    // Check for collisions
                    if (dd <= Particle.BALL_DIAMETER_2) {

                        // add a little bit of entropy, after nothing is
                        // perfect in the universe.
                        dx += (random.nextFloat() - 0.5f) * 0.0001f;
                        dy += (random.nextFloat() - 0.5f) * 0.0001f;
                        dd = dx * dx + dy * dy;

                        // simulate the spring
                        float d = (float)Math.sqrt(dd);
                        float c = (0.5f * (Particle.BALL_DIAMETER - d)) / d;

                        curr.getLocation().x -= dx * c;
                        curr.getLocation().y -= dy * c;
                        ball.getLocation().x += dx * c;
                        ball.getLocation().y += dy * c;

                        more = true;
                    }
                }

                // Finally make sure the particle doesn't intersects
                // with the walls.
                curr.ResolveCollisionWithBounds ();
            }
        }
    }
}
