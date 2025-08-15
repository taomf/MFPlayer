package com.tmf.player;

import android.content.res.Resources;
import android.util.TypedValue;

public class Utils {
    public static float dp2px(float paramFloat) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paramFloat, Resources.getSystem().getDisplayMetrics());
    }
}
