/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;


import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.VIBRATOR_SERVICE;

public class AndroidUtilities {

    public static String APP_TIMEZONE = "GMT";
    public static float density = 1;


    public static int dp(float value, Context c) {
        density = c.getResources().getDisplayMetrics().density;
        return (int) Math.ceil(density * value);
    }

    public static Spannable replaceTags(String str, Context context) {
        try {
            int start = -1;
            int startColor = -1;
            int end = -1;
            StringBuilder stringBuilder = new StringBuilder(str);
            while ((start = stringBuilder.indexOf("<br>")) != -1) {
                stringBuilder.replace(start, start + 4, "\n");
            }
            while ((start = stringBuilder.indexOf("<br/>")) != -1) {
                stringBuilder.replace(start, start + 5, "\n");
            }
            ArrayList<Integer> bolds = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();
            while ((start = stringBuilder.indexOf("<b>")) != -1 || (startColor = stringBuilder.indexOf("<c")) != -1) {
                if (start != -1) {
                    stringBuilder.replace(start, start + 3, "");
                    end = stringBuilder.indexOf("</b>");
                    stringBuilder.replace(end, end + 4, "");
                    bolds.add(start);
                    bolds.add(end);
                } else if (startColor != -1) {
                    stringBuilder.replace(startColor, startColor + 2, "");
                    end = stringBuilder.indexOf(">", startColor);
                    int color = Color.parseColor(stringBuilder.substring(startColor, end));
                    stringBuilder.replace(startColor, end + 1, "");
                    end = stringBuilder.indexOf("</c>");
                    stringBuilder.replace(end, end + 4, "");
                    colors.add(startColor);
                    colors.add(end);
                    colors.add(color);
                }
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(stringBuilder);
            for (int a = 0; a < colors.size() / 3; a++) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(colors.get(a * 3 + 2)), colors.get(a * 3), colors.get(a * 3 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannableStringBuilder;
        } catch (Exception e) {

        }
        return new SpannableStringBuilder(str);
    }

    public static void fadeoutView(final View view)
    {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(2000);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                view.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        view.startAnimation(fadeOut);
    }

    public static void loadImage(final ImageView view, final String url)
    {
        Picasso.with(view.getContext()).load(url).into(view);
    }

    public static void loadImage(final ImageView view, final File file)
    {
        Ion.with(view.getContext()).load(file).intoImageView(view);
    }

    public static String getUUID(Context context){
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static WindowManager.LayoutParams setDialogLayoutParams(Activity activity, Dialog dialog)
    {
        try
        {
            dialog.setCancelable(false);

            Display display = activity.getWindowManager().getDefaultDisplay();
            Point screenSize = new Point();
            display.getSize(screenSize);
            int width = screenSize.x;

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (width - (width * 0.30) );//reduce 30%
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            dialog.getWindow().setAttributes(layoutParams);
            return layoutParams;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static double getMiles(double meters) {
        return meters*0.000621371192;
    }

    public static double getMeters(double miles) {
        return miles*1609.344;
    }


    public static class DateUtils {
        private static SimpleDateFormat targetFormat = new SimpleDateFormat();
        private static SimpleDateFormat originalFormat = new SimpleDateFormat();
        private static String formattedDate = "";
        public static String timestampPattern = "yyyy-MM-dd HH:mm:ss";

        public static long getCurrentUTCMillis(){
            return Calendar.getInstance(TimeZone.getTimeZone(APP_TIMEZONE)).getTimeInMillis();
        }

        public static String getCurrentUTCDate(String targetPattern){
            long millis = getCurrentUTCMillis();
            return getUTCDate(targetPattern,millis);
        }

        public static String getUTCDate(String targetPattern, long millis){
            targetFormat.applyPattern(targetPattern);
            targetFormat.setTimeZone(TimeZone.getTimeZone(APP_TIMEZONE));

            return targetFormat.format(new Date(millis));
        }

        public static long getMillisFromUTCDate(String existingPattern, String existingValue){
            targetFormat.applyPattern(existingPattern);
            targetFormat.setTimeZone(TimeZone.getTimeZone(APP_TIMEZONE));
            Date date = new Date();
            try {
                date = targetFormat.parse(existingValue);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return date.getTime();
        }

        public static String getFormattedDate(String targetPattern,
                                              String existingPattern, String existingValue, String existingTZ, String targetTZ) {
            System.out.println(existingValue);
            formattedDate = existingValue;

            originalFormat.applyPattern(existingPattern);
            targetFormat.applyPattern(targetPattern);

            DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
            symbols.setAmPmStrings(new String[]{"AM", "PM"});
            targetFormat.setDateFormatSymbols(symbols);

            originalFormat.setTimeZone(TimeZone.getTimeZone(existingTZ));
            targetFormat.setTimeZone(TimeZone.getTimeZone(targetTZ));

            try {
                System.out.println(originalFormat.format(originalFormat.parse(existingValue)));
                formattedDate = targetFormat.format(originalFormat
                        .parse(existingValue));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return formattedDate;
        }
    }

    public static void vibrateDevice(Context context) {
        ((Vibrator) context.getSystemService(VIBRATOR_SERVICE)).vibrate(500);
    }

    public static void hideKeyboard(Activity activity) {
        if(activity!=null) {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((activity.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
    }


}
