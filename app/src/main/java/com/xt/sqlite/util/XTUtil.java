package com.xt.sqlite.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by xiangleiliu on 2016/8/9.
 */
public class XTUtil {


    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message, int duration) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        final Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    /**
     * Dip转换为实际屏幕的像素值
     *
     * @param dm  设备显示对象描述
     * @param dip dip值
     * @return 匹配当前屏幕的像素值
     */
    public static int getPixelFromDip(DisplayMetrics dm, float dip) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, dm) + 0.5f);
    }

    public static int getPixelFromDip(Context context, float f) {
        return getPixelFromDip(context.getResources().getDisplayMetrics(), f);
    }


    public static int[] getScreenSize(final Context context) {
        if (context == null) {
            return null;
        }
        int[] screen = new int[2];
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screen[0] = wm.getDefaultDisplay().getWidth();
        screen[1] = wm.getDefaultDisplay().getHeight();
        return screen;
    }

}
