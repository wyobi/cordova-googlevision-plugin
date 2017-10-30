package com.erikeuserr;

import android.graphics.Point;
import android.graphics.Rect;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.internal.client.BoundingBoxParcel;

final class zza {
    static Rect zza(Text var0) {
        int var1 = 2147483647;
        int var2 = -2147483648;
        int var3 = 2147483647;
        int var4 = -2147483648;
        Point[] var5 = var0.getCornerPoints();
        Point[] var6 = var5;
        int var7 = var5.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            Point var9 = var6[var8];
            var1 = Math.min(var1, var9.x);
            var2 = Math.max(var2, var9.x);
            var3 = Math.min(var3, var9.y);
            var4 = Math.max(var4, var9.y);
        }

        return new Rect(var1, var3, var2, var4);
    }

    static Point[] zza(BoundingBoxParcel var0) {
        Point[] var1 = new Point[4];
        double var2 = Math.sin(Math.toRadians((double)var0.aOP));
        double var4 = Math.cos(Math.toRadians((double)var0.aOP));
        var1[0] = new Point(var0.left, var0.top);
        var1[1] = new Point((int)((double)var0.left + (double)var0.width * var4), (int)((double)var0.top + (double)var0.width * var2));
        var1[2] = new Point((int)((double)var1[1].x - (double)var0.height * var2), (int)((double)var1[1].y + (double)var0.height * var4));
        var1[3] = new Point(var1[0].x + (var1[2].x - var1[1].x), var1[0].y + (var1[2].y - var1[1].y));
        return var1;
    }
}