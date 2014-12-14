package com.example.richardhabeeb.marblegame;

/**
 * Created by richardhabeeb on 12/13/14.
 */

import android.graphics.*;

public class Wall {
    public static int WALL_WIDTH_PX = 180;
    public static float WALL_WIDTH = WALL_WIDTH_PX / Game.pixels_per_meter_x;
    public Paint paint;
    public Rect rect;

    public Wall(int left, int top)
    {
        rect = new Rect(left,top,left + WALL_WIDTH_PX, top + WALL_WIDTH_PX);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public PointF getCenter(PointF origin)
    {
        return new PointF((rect.centerX() - origin.x) / Game.pixels_per_meter_x, (origin.y - rect.centerY()) / Game.pixels_per_meter_y);
    }

    public PointF[] getCorners(PointF origin)
    {
        System.out.print(origin.x);
        return new PointF []{
                new PointF((rect.left - origin.x) / Game.pixels_per_meter_x, (origin.y - rect.top) / Game.pixels_per_meter_y),
                new PointF((rect.left - origin.x) / Game.pixels_per_meter_x, (origin.y - rect.bottom) / Game.pixels_per_meter_y),
                new PointF((rect.right - origin.x) / Game.pixels_per_meter_x, (origin.y - rect.top) / Game.pixels_per_meter_y),
                new PointF((rect.right - origin.x) / Game.pixels_per_meter_x, (origin.y - rect.bottom) / Game.pixels_per_meter_y),
        };
    }



}
