package com.erikeuserr;

import android.graphics.Point;
import android.graphics.Rect;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.internal.client.LineBoxParcel;
import com.google.android.gms.vision.text.internal.client.WordBoxParcel;
import java.util.ArrayList;
import java.util.List;

public class Line implements Text {
    private LineBoxParcel aOG;
    private List<Element> aOH;

    Line(LineBoxParcel var1) {
        this.aOG = var1;
    }

    List<Element> zzclt() {
        if(this.aOG.aOQ.length == 0) {
            return new ArrayList(0);
        } else {
            if(this.aOH == null) {
                this.aOH = new ArrayList(this.aOG.aOQ.length);
                WordBoxParcel[] var1 = this.aOG.aOQ;
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    WordBoxParcel var4 = var1[var3];
                    this.aOH.add(new Element(var4));
                }
            }

            return this.aOH;
        }
    }

    public String getLanguage() {
        return this.aOG.aOK;
    }

    public String getValue() {
        return this.aOG.aOU;
    }

    public Rect getBoundingBox() {
        return zza.zza(this);
    }

    public Point[] getCornerPoints() {
        return zza.zza(this.aOG.aOR);
    }

    public List<? extends Text> getComponents() {
        return this.zzclt();
    }

    public float getAngle() {
        return this.aOG.aOR.aOP;
    }

    public boolean isVertical() {
        return this.aOG.aOX;
    }
}
