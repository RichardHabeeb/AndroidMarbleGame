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

    public List<Wall> Walls;

    public ParticleSystem (Game view)
    {
        sim_view = view;
        Balls = new ArrayList<Particle>();
        Walls = new ArrayList<Wall>();

        // Initially our particles have no speed or acceleration
        for (int i = 0; i < NUM_PARTICLES; i++)
            Balls.add(new Particle(this));

        Walls.add(new Wall(900, 0));
    }

    // Update the position of each particle in the system using the
    // Verlet integrator.
    private void UpdatePositions (float sx, float sy, long timestamp)
    {
        long t = timestamp;

        if (last_t != 0 && last_t != t) {
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

    private float PointDistance(PointF p1, PointF p2) {
        return (float)Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
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
                PointF ballCenter =  curr.getLocation();

                for (int j = 0; j < Walls.size(); j++) {
                    Wall wall = Walls.get(j);
                    PointF wallCenter = wall.getCenter(sim_view.origin);
                    PointF[] wallCorners = wall.getCorners(sim_view.origin);

                    double theta = 180*Math.atan2(wallCenter.y - ballCenter.y, wallCenter.x - ballCenter.x) / (Math.PI);

                    PointF edgePointClosestToWall = new PointF(
                            ballCenter.x + (Particle.BALL_DIAMETER / 2.0f) * (float)Math.cos(Math.atan2(wallCenter.y - ballCenter.y, wallCenter.x - ballCenter.x)),
                            ballCenter.y + (Particle.BALL_DIAMETER / 2.0f) * (float)Math.sin(Math.atan2(wallCenter.y - ballCenter.y, wallCenter.x - ballCenter.x)));

                    for(int p = 0; p < wallCorners.length; p++) {
                        if (PointDistance(wallCorners[p], ballCenter) < (Particle.BALL_DIAMETER / 2.0f)) {
                            more = true;
                        }
                    }
                    if (PointDistance(wallCenter, ballCenter) < (Particle.BALL_DIAMETER / 2.0f)) {
                        more = true;
                    }
                    if(edgePointClosestToWall.x > wallCorners[0].x &&
                       edgePointClosestToWall.x < wallCorners[2].x &&
                       edgePointClosestToWall.y < wallCorners[0].y &&
                       edgePointClosestToWall.y > wallCorners[2].y) {
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
