package com.erikeuserr;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.SparseArray;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.internal.client.BoundingBoxParcel;
import com.google.android.gms.vision.text.internal.client.LineBoxParcel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TextBlock implements Text {
    private LineBoxParcel[] aOI;
    private Point[] cornerPoints;
    private List<Line> aOJ;
    private String aOK;
    private Rect aOL;

    TextBlock(SparseArray<LineBoxParcel> var1) {
        this.aOI = new LineBoxParcel[var1.size()];

        for(int var2 = 0; var2 < this.aOI.length; ++var2) {
            this.aOI[var2] = (LineBoxParcel)var1.valueAt(var2);
        }

    }

    public String getLanguage() {
        if(this.aOK != null) {
            return this.aOK;
        } else {
            HashMap var1 = new HashMap();
            LineBoxParcel[] var2 = this.aOI;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                LineBoxParcel var5 = var2[var4];
                int var6 = 0;
                if(var1.containsKey(var5.aOK)) {
                    var6 = ((Integer)var1.get(var5.aOK)).intValue();
                }

                var1.put(var5.aOK, Integer.valueOf(var6 + 1));
            }

            this.aOK = (String)((Entry)Collections.max(var1.entrySet(), new Comparator() {
                @Override
                public int compare(Object o, Object t1) {
                    return 0;
                }

                public int compare(Entry<String, Integer> var1, Entry<String, Integer> var2) {
                    return ((Integer)var1.getValue()).compareTo((Integer)var2.getValue());
                }
            })).getKey();
            if(this.aOK == null || this.aOK.isEmpty()) {
                this.aOK = "und";
            }

            return this.aOK;
        }
    }

    public String getValue() {
        if(this.aOI.length == 0) {
            return "";
        } else {
            StringBuilder var1 = new StringBuilder(this.aOI[0].aOU);

            for(int var2 = 1; var2 < this.aOI.length; ++var2) {
                var1.append("\n");
                var1.append(this.aOI[var2].aOU);
            }

            return var1.toString();
        }
    }

    void zzclu() {
        if(this.aOI.length == 0) {
            this.cornerPoints = new Point[0];
        } else {
            int var1 = 2147483647;
            int var2 = -2147483648;
            int var3 = 2147483647;
            int var4 = -2147483648;

            for(int var5 = 0; var5 < this.aOI.length; ++var5) {
                Point[] var6 = zza(this.aOI[var5].aOR, this.aOI[0].aOR);

                for(int var7 = 0; var7 < 4; ++var7) {
                    Point var8 = var6[var7];
                    var1 = Math.min(var1, var8.x);
                    var2 = Math.max(var2, var8.x);
                    var3 = Math.min(var3, var8.y);
                    var4 = Math.max(var4, var8.y);
                }
            }

            this.cornerPoints = zza(var1, var3, var2, var4, this.aOI[0].aOR);
        }
    }

    private static Point[] zza(int var0, int var1, int var2, int var3, BoundingBoxParcel var4) {
        int var5 = var4.left;
        int var6 = var4.top;
        double var7 = Math.sin(Math.toRadians((double)var4.aOP));
        double var9 = Math.cos(Math.toRadians((double)var4.aOP));
        Point[] var11 = new Point[]{new Point(var0, var1), new Point(var2, var1), new Point(var2, var3), new Point(var0, var3)};

        for(int var12 = 0; var12 < 4; ++var12) {
            int var13 = (int)((double)var11[var12].x * var9 - (double)var11[var12].y * var7);
            int var14 = (int)((double)var11[var12].x * var7 + (double)var11[var12].y * var9);
            var11[var12].x = var13;
            var11[var12].y = var14;
            var11[var12].offset(var5, var6);
        }

        return var11;
    }

    private static Point[] zza(BoundingBoxParcel var0, BoundingBoxParcel var1) {
        int var2 = -var1.left;
        int var3 = -var1.top;
        double var4 = Math.sin(Math.toRadians((double)var1.aOP));
        double var6 = Math.cos(Math.toRadians((double)var1.aOP));
        Point[] var8 = new Point[4];
        var8[0] = new Point(var0.left, var0.top);
        var8[0].offset(var2, var3);
        int var9 = (int)((double)var8[0].x * var6 + (double)var8[0].y * var4);
        int var10 = (int)((double)(-var8[0].x) * var4 + (double)var8[0].y * var6);
        var8[0].x = var9;
        var8[0].y = var10;
        var8[1] = new Point(var9 + var0.width, var10);
        var8[2] = new Point(var9 + var0.width, var10 + var0.height);
        var8[3] = new Point(var9, var10 + var0.height);
        return var8;
    }

    public Point[] getCornerPoints() {
        if(this.cornerPoints == null) {
            this.zzclu();
        }

        return this.cornerPoints;
    }

    public List<? extends Text> getComponents() {
        return this.zzclv();
    }

    List<Line> zzclv() {
        if(this.aOI.length == 0) {
            return new ArrayList(0);
        } else {
            if(this.aOJ == null) {
                this.aOJ = new ArrayList(this.aOI.length);
                LineBoxParcel[] var1 = this.aOI;
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    LineBoxParcel var4 = var1[var3];
                    this.aOJ.add(new Line(var4));
                }
            }

            return this.aOJ;
        }
    }

    public Rect getBoundingBox() {
        if(this.aOL == null) {
            this.aOL = zza.zza(this);
        }

        return this.aOL;
    }
}
