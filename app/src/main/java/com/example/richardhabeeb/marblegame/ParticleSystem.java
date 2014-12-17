package com.example.richardhabeeb.marblegame;

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

    public Particle ball;

    public List<Wall> Walls;

    public ParticleSystem (Game view)
    {
        sim_view = view;
        Walls = new ArrayList<>();


        int h =  view.getHeight();
        int w = view.getWidth();
        int gridRowCount = h / Wall.WALL_WIDTH_PX;
        int gridColCount = w / Wall.WALL_WIDTH_PX;

        String[] stringMaze = Prim.run(gridRowCount, gridColCount).split("\\r?\\n");

        for(int r = 0; r < gridRowCount; r++) {
            for(int c = 0; c < gridColCount; c++) {
                if(stringMaze[r].charAt(c) == '*') {
                    Walls.add(new Wall(c * Wall.WALL_WIDTH_PX, r * Wall.WALL_WIDTH_PX));
                }

                if(stringMaze[r].charAt(c) == 'E') {
                    ball = new Particle(this, new PointF(
                            (view.origin.x - c * Wall.WALL_WIDTH_PX + Particle.BALL_DIAMETER_PX / 2) / Game.pixels_per_meter_x,
                            (view.origin.y - r * Wall.WALL_WIDTH_PX - Particle.BALL_DIAMETER_PX / 2) / Game.pixels_per_meter_y));
                }

            }
        }

        for(int c = 0; c < gridColCount; c++) {
           Walls.add(new Wall(c * Wall.WALL_WIDTH_PX, gridRowCount * Wall.WALL_WIDTH_PX));
        }
        for(int r = 0; r < gridRowCount + 1; r++) {
            Walls.add(new Wall(gridColCount * Wall.WALL_WIDTH_PX, r * Wall.WALL_WIDTH_PX));
        }

        //Walls.add(new Wall(1280/2 - Wall.WALL_WIDTH_PX / 2, 752/2 - Wall.WALL_WIDTH_PX / 2));

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
                    ball.ComputePhysics(sx, sy, dT, dTC);
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
        UpdatePositions(sx, sy, now);

        if(!sim_view.getPaused()) {
            PointF ballCenter = ball.getLocation();

            for (int j = 0; j < Walls.size(); j++) {
                Wall wall = Walls.get(j);
                PointF wallCenter = wall.getCenter(sim_view.origin);
                PointF[] wallCorners = wall.getCorners(sim_view.origin);

                double theta = Math.atan2(wallCenter.y - ballCenter.y, wallCenter.x - ballCenter.x);

                PointF edgePointClosestToWall = new PointF(
                        ballCenter.x + (Particle.BALL_DIAMETER / 2.0f) * (float) Math.cos(theta),
                        ballCenter.y + (Particle.BALL_DIAMETER / 2.0f) * (float) Math.sin(theta));

                if (edgePointClosestToWall.x > wallCorners[0].x &&
                        edgePointClosestToWall.x < wallCorners[3].x &&
                        edgePointClosestToWall.y < wallCorners[0].y &&
                        edgePointClosestToWall.y > wallCorners[3].y) {

                    float distanceToTop = Math.abs(edgePointClosestToWall.y - wallCorners[0].y);
                    float distanceToLeft = Math.abs(edgePointClosestToWall.x - wallCorners[0].x);
                    float distanceToBottom = Math.abs(edgePointClosestToWall.y - wallCorners[3].y);
                    float distanceToRight = Math.abs(edgePointClosestToWall.x - wallCorners[3].x);

                    if (distanceToTop < distanceToLeft && distanceToTop < distanceToBottom && distanceToTop < distanceToRight) {
                        ballCenter.y += distanceToTop;
                    } else if (distanceToLeft < distanceToTop && distanceToLeft < distanceToBottom && distanceToLeft < distanceToRight) {
                        ballCenter.x -= distanceToLeft;
                    } else if (distanceToBottom < distanceToTop && distanceToBottom < distanceToLeft && distanceToBottom < distanceToRight) {
                        ballCenter.y -= distanceToBottom;
                    } else {
                        ballCenter.x += distanceToRight;
                    }
                }
            }

            // Finally make sure the particle doesn't intersects
            // with the walls.
            ball.ResolveCollisionWithBounds();
        }

    }
}
