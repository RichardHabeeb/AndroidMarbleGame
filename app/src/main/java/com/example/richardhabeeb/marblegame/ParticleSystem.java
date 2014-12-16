package com.example.richardhabeeb.marblegame;

import com.example.richardhabeeb.marblegame.Game;
import com.example.richardhabeeb.marblegame.Particle;

import java.util.List;
import java.util.ArrayList;
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
            Balls.add(new Particle(this, new PointF(-100,-100)));

        //Walls.add(new Wall(560, 296));
        Walls.add(new Wall(180, 180));
        Walls.add(new Wall(180, 360));
        Walls.add(new Wall(180, 540));
        Walls.add(new Wall(180, 720));
        Walls.add(new Wall(360, 180));

        Walls.add(new Wall(720, 0));
        Walls.add(new Wall(720, 180));
        Walls.add(new Wall(720, 360));
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

                if(!sim_view.getPaused()) {
                    for (int i = 0; i < Balls.size(); i++) {

                        Balls.get(i).ComputePhysics(sx, sy, dT, dTC);
                    }
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
        //if(!sim_view.getPaused()) {
            // update the system's positions
            UpdatePositions(sx, sy, now);


            for (int i = 0; i < Balls.size(); i++) {
                Particle curr = Balls.get(i);
                PointF ballCenter = curr.getLocation();

                for (int j = 0; j < Walls.size(); j++) {
                    Wall wall = Walls.get(j);
                    PointF wallCenter = wall.getCenter(sim_view.origin);
                    PointF[] wallCorners = wall.getCorners(sim_view.origin);

                    double theta = 180 * Math.atan2(wallCenter.y - ballCenter.y, wallCenter.x - ballCenter.x) / (Math.PI);

                    PointF edgePointClosestToWall = new PointF(
                            ballCenter.x + (Particle.BALL_DIAMETER / 2.0f) * (float) Math.cos(Math.atan2(wallCenter.y - ballCenter.y, wallCenter.x - ballCenter.x)),
                            ballCenter.y + (Particle.BALL_DIAMETER / 2.0f) * (float) Math.sin(Math.atan2(wallCenter.y - ballCenter.y, wallCenter.x - ballCenter.x)));

                    if (edgePointClosestToWall.x > wallCorners[0].x &&
                            edgePointClosestToWall.x < wallCorners[3].x &&
                            edgePointClosestToWall.y < wallCorners[0].y &&
                            edgePointClosestToWall.y > wallCorners[3].y) {

                        float distanceToTop = Math.abs(edgePointClosestToWall.y - wallCorners[0].y);
                        float distanceToLeft = Math.abs(edgePointClosestToWall.x - wallCorners[0].x);
                        float distanceToBottom = Math.abs(edgePointClosestToWall.y - wallCorners[3].y);
                        float distanceToRight = Math.abs(edgePointClosestToWall.x - wallCorners[3].x);

                        if (distanceToTop < distanceToLeft && distanceToTop < distanceToBottom && distanceToTop < distanceToRight) {
                            curr.getLocation().y += distanceToTop;
                        } else if (distanceToLeft < distanceToTop && distanceToLeft < distanceToBottom && distanceToLeft < distanceToRight) {
                            curr.getLocation().x -= distanceToLeft;
                        } else if (distanceToBottom < distanceToTop && distanceToBottom < distanceToLeft && distanceToBottom < distanceToRight) {
                            curr.getLocation().y -= distanceToBottom;
                        } else {
                            curr.getLocation().x += distanceToRight;
                        }
                    }
                }

                // Finally make sure the particle doesn't intersects
                // with the walls.
                curr.ResolveCollisionWithBounds();
            }
        //}
    }
}
