package com.example.richardhabeeb.marblegame;

import java.util.List;
import java.util.ArrayList;
import android.graphics.PointF;

class ParticleSystem
{
    public long last_t;
    public float last_delta_t;

    public PointF Bounds = new PointF();
    public PointF startPoint;

    public Game sim_view;

    public Particle ball;
    public List<Wall> Walls;
    public Exit exit;

    public ParticleSystem (Game view)
    {
        sim_view = view;

        resetMaze();
    }

    public void resetMaze()
    {
        sim_view.setPaused(true);
        Walls = new ArrayList<>();

        int h =  sim_view.getHeight();
        int w = sim_view.getWidth();
        int gridRowCount = h / Wall.WALL_WIDTH_PX;
        int gridColCount = w / Wall.WALL_WIDTH_PX;

        String[] stringMaze = Prim.run(gridRowCount, gridColCount).split("\\r?\\n");

        for(int r = 0; r < gridRowCount; r++) {
            for(int c = 0; c < gridColCount; c++) {
                if (stringMaze[r].charAt(c) == '*') {
                    Walls.add(new Wall(c * Wall.WALL_WIDTH_PX, r * Wall.WALL_WIDTH_PX));
                }

                if (stringMaze[r].charAt(c) == 'S') {
                    startPoint = new PointF(
                            (sim_view.origin.x - c * Wall.WALL_WIDTH_PX + Particle.BALL_DIAMETER_PX / 2) / Game.pixels_per_meter_x,
                            (sim_view.origin.y - r * Wall.WALL_WIDTH_PX - Particle.BALL_DIAMETER_PX / 2) / Game.pixels_per_meter_y);
                    ball = new Particle(this, startPoint);
                }

                if (stringMaze[r].charAt(c) == 'E') {
                    exit = new Exit(c * Wall.WALL_WIDTH_PX, r * Wall.WALL_WIDTH_PX);
                }
            }
        }

        for(int c = 0; c < gridColCount; c++) {
            Walls.add(new Wall(c * Wall.WALL_WIDTH_PX, gridRowCount * Wall.WALL_WIDTH_PX));
        }

        for(int r = 0; r < gridRowCount + 1; r++) {
            Walls.add(new Wall(gridColCount * Wall.WALL_WIDTH_PX, r * Wall.WALL_WIDTH_PX));
        }
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

                    if(Math.sqrt((ballCenter.y - wallCenter.y)*(ballCenter.y - wallCenter.y) + (ballCenter.x - wallCenter.x)*(ballCenter.x - wallCenter.x)) < Particle.BALL_DIAMETER / 2.0f) {
                        ball.resetLocation(startPoint);
                    }

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

            PointF exitCenter = exit.getCenter(sim_view.origin);

            if(Math.sqrt((ballCenter.y - exitCenter.y)*(ballCenter.y - exitCenter.y) + (ballCenter.x - exitCenter.x)*(ballCenter.x - exitCenter.x)) < Particle.BALL_DIAMETER / 2.0f) {
                resetMaze();
            }


            // Finally make sure the particle doesn't intersects
            // with the walls.
            ball.ResolveCollisionWithBounds();
        }

    }
}
