package com.erikeuserr;

import android.graphics.Point;
import android.graphics.Rect;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.internal.client.WordBoxParcel;
import java.util.ArrayList;
import java.util.List;

public class Element implements Text {
    private WordBoxParcel aOF;

    Element(WordBoxParcel var1) {
        this.aOF = var1;
    }

    public String getLanguage() {
        return this.aOF.aOK;
    }

    public String getValue() {
        return this.aOF.aOU;
    }

    public Rect getBoundingBox() {
        return zza.zza(this);
    }

    public Point[] getCornerPoints() {
        return zza.zza(this.aOF.aOR);
    }

    public List<? extends Text> getComponents() {
        return new ArrayList();
    }
}
