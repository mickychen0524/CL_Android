package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dev.countryfair.player.playlazlo.com.countryfair.BuildConfig;

/**
 * Created by mymac on 2/27/17.
 */

public class SetInitialVarsOnLocal {
    public SetInitialVarsOnLocal() {

    }
    public SetInitialVarsOnLocal(Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("token", BuildConfig.TOKEN);
        editor.putString("baseURL", BuildConfig.BASE_URL);
        editor.putString("brandLicenseCode", BuildConfig.BRAND_LICENSE_CODE);

        editor.putString("botApiToken", BuildConfig.BOT_API_TOKEN);
        editor.putString("botEndPoint", BuildConfig.BOT_ENDPOINT);

        editor.putString("playerLicenseCode", "");

        editor.putBoolean("ageCertificationHasOccured", false);
        editor.putBoolean("selfieHasBeenTaken", false);
        editor.putBoolean("userRegisterState", false);
        editor.putString("age", "");
        editor.putString("wasBorn", "");
        editor.putBoolean("shoppingCartHelpOverlayHasShown", false);
        editor.putBoolean("channelHelpOverlayHasShown", false);
        editor.putBoolean("addToCartHelpOverlayHasShown", false);
        editor.putBoolean("topViewHasShown", false);
        editor.putBoolean("leftViewHasShown", false);
        editor.putBoolean("rightViewHasShown", false);
        editor.putBoolean("refundTicketViewHelpOverlayShown", false);
        editor.putBoolean("pinEnableState", false);
        editor.putBoolean("powerSavingState", false);
        editor.putBoolean("includeSelfieState", false);
        editor.putBoolean("realDataFlag", true);
        editor.putBoolean("ticketRecentFlag", true);
        editor.putBoolean("isSystemGenerated", true);
        editor.putString("pinHashValue", "");
        editor.putString("shoppingCartData", "");
        editor.putString("successedTicketData", "");
        editor.putString("failedTicketData", "");
        editor.putString("userSelfieImage", "");
        editor.putBoolean("retailerExistFlag", false);
        editor.putString("deviceTokenForPush", "");
        editor.putString("lastUploadedDate", "");
        editor.putString("trainingCode", "");
        editor.putString("QRCodeVal", "");
        editor.putBoolean("socialConnectionImageDownload", false);
        editor.putBoolean("botRegisterState", false);
        editor.putBoolean("startAppState", false);
        editor.putBoolean("cameraState", false);
        editor.putBoolean("locationState", false);
        editor.putBoolean("storageState", false);
        editor.putBoolean("phoneState", false);
        editor.putString("SocialXGetYLicenseCodes", "");


        editor.putString("hockeyAppId", "");
        editor.putString("ocrLicenseCodeAndroid", "");

        editor.apply();
    }

}
