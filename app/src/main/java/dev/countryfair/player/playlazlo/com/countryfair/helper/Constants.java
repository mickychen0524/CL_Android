/*
Copyright (c) Microsoft
All Rights Reserved
Apache 2.0 License
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */

package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.BuildConfig;


public class Constants {

    public static final String SDK_VERSION = "1.0";

    /**
     * UTF-8 encoding
     */
    public static final String UTF8_ENCODING = "UTF-8";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String HEADER_AUTHORIZATION_VALUE_PREFIX = "Bearer ";

    public static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES =  1; // in Meters
    public static final long MINIMUM_TIME_BETWEEN_UPDATES =         1000; // in Milliseconds
    public static final long CLICK_DURATION =                       3000; // click duration time

    public static final long INDICATOR_DURATION = 3000; // indicator display timeout
    public static final int MIN_DISTANCE_RETAILER = 50; // min distance retailer

//    public static AuthenticationResult CURRENT_RESULT = null;

    // shopping cart checkout status code 0 ~ 6, 10
    public static final int SHOPPING_CART_DOWNLOADING_SUCCESS =     0;      // ticket downloading complete successfully
    public static final int SHOPPING_CART_GENERATING_SUCCESS =      1;       // getting status complete successfully
    public static final int SHOPPING_CART_GENERATING_SUB_FAILED =   2;    // getting status some tickets sass url is null
    public static final int SHOPPING_CART_GENERATING_FAILED =       3;        // getting status ticket list is empty
    public static final int SHOPPING_CART_GENERATING_API_FAILED =   4;    // getting status api error
    public static final int SHOPPING_CART_GENERATING_LOOP =         5;          // getting status loop signal
    public static final int SHOPPING_CART_GENERATING_CANCELED =     6;      // getting status cancel signal
    public static final int SHOPPING_CART_SERVICE_EXCEPTION =       10;       // shopping cart service all exception

    // checkout file download status code 1 ~ 3, 10
    public static final int DOWNLOAD_COMPLETE =                     1;
    public static final int DOWNLOAD_FAILED =                       2;
    public static final int DOWNLOAD_PICK =                         3;
    public static final int DOWNLOAD_EXCEPRION =                    10;
    public static final boolean USE_TEMP_VALUES = BuildConfig.USE_TEMP_VALUES;
    public static final String TEMP_LATITUDE = "42.129223";
    public static final String TEMP_LONGITUDE = "-80.085060";
    public static final String DB_NAME = "greendb.db";

    // Endpoint we are targeting for the deployed WebAPI service
    public static String SERVICE_URL =                              "";
    public static String BOT_SERVER_URL =                           "";
    public static String TOKEN =                                    "";
    public static String BOT_TOKEN =                                "";
    public static String PLAYER_TOKEN =                             "";
    public static String BRAND_LISENCE_CODE =                       "";
    public static String GEO_LATITUDE =                             "47.9118919631641";
    public static String GEO_LONGITUDE =                            "106.9116368910428";
    public static String GEO_ALTITUDE =                            "0";
    public static String CORRE_REF_ID =                             "00000000-0000-0000-0000-000000000000";

    public static String HOCKEY_APP_ID =                                    "";
    public static String OCR_LICENSE_CODE =                                 "";


    public static List<JSONObject> gameGlobalArr =                  new ArrayList<>();
    public static List<JSONObject> brandGlobalArr =                 new ArrayList<>();
    public static List<JSONObject> channelGroupGlobalArr =          new ArrayList<>();
    public static String retailerRefId =                            "e2a5b746-41b1-490f-ab90-4d53ac8a5d94";
    public static String SETTING_BLE_UUID =                         "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";

    public static final String URL_HOCKEY_APP_ID =                "/api/v1/utility/app/config";
    public static final String URL_GET_RETAILER_WITH_LOCATION =   "/api/v1/retailers/display/bylocation";
    public static final String URL_GET_DRAW_PREFIX =              "/api/v3/draws/bygame/";
    public static final String URL_GET_DRAW_SURFIX =              "/display";
    public static final String URL_REGISTER_USER_WITH_IMAGE =     "/api/v3/player/registration";
    public static final String URL_UPDATE_USER_WITH_IMAGE =       "/api/v3/player/registration/update";
    public static final String URL_REGISTER_PUSH_NOTIFICATION =   "/api/v2/notification/player/register";
    public static final String URL_GET_PROXIMITY_DATA =           "/api/v1/shopping/proximity/upload/url";
    public static final String URL_PROXIMITY_UPLOAD_BLOB =        "/api/v2/shopping/checkoutpendings/display/21ef3d05-127f-49d1-a793-dd0427e2b2b9";
    public static final String URL_CHECKOUT_WITH_SHOPPING =       "/api/v3/shopping/checkout";
    public static final String URL_GET_CHECKOUT_STATUS_SESSION =  "/api/v3/shopping/checkout/status/tickets";
    public static final String URL_CHECKOUT_CANCEL =              "/api/v2/shopping/checkout/cancel";
    public static final String URL_DOWNLOAD_SOCIAL_PHOTO =        "/api/player/social/connect/image";
    public static final String URL_GET_ALL_GAMES_LIST =           "/api/v2/games/display";
    public static final String URL_GET_ALL_BRANDS_LIST =          "/api/v2/brands/display"; // unused url : old one
    public static final String URL_GET_ALL_CHANNEL_GROUP_LIST =   "/api/v1/channelgroups/display"; // unused url : old one
    public static final String URL_GET_CHANNEL_AND_BRANDS_LIST =  "/api/v1/brands/channelgroup/display";
    public static final String URL_TICKET_RECEIVED =              "/api/v1/shopping/checkout/ticket/received";
    public static final String URL_REFUND_TICKET =                "/api/v1/shopping/ticket/refund/initiate";
    public static final String URL_SOCIAL_CONNECT =               "/api/v1/player/social/connect";
    public static final String URL_TICKET_VALIDATE =              "/api/v1/validation/validate";
    public static final String URL_GET_ALL_MERCHANDIES =          "/api/v1/claims/exchange/merchandise";
    public static final String URL_GET_ALL_TERMS =                "/api/v1/claims/exchange/terms";
    public static final String URL_TICKET_CLAIM_COMPLETE =        "/api/v1/claim/exchange/low";
    public static final String URL_GLOBAL_CHECKOUT_GETTING =      "/api/v1/shopping/checkout/status";
    public static final String URL_GETTING_UPLOAD_RECEIPT_IMG =   "/api/v1/shopping/purchase/verification/upload/url";
    public static final String URL_BOT_REGISTER =                 "/v3/directline/conversations";
    public static final String URL_BOT_MESSAGE_SEND_PREFIX =      "/v3/directline/conversations/";
    public static final String URL_BOT_MESSAGE_SEND_SURFIX =      "/activities";
    public static final String URL_GET_REWARDS =                  "/api/v1/loyalty/rewards/";
    public static final String URL_GET_RETAILER_BY_LOCATION =     "/api/v2/retailers/display/bylocation/";
    public static final String URL_RECEIPT_VERIFICATION =         "api/v1/loyalty/purchase/verification";
    public static final String URL_CLAIM_GIFTCARD =                  "/api/v1/claim/giftcard/initiate";
    public static final String URL_COUPON =                  "/api/v1/claim/coupon/initiate";

    // retailer getting flag
    public static boolean b_getRetailerListFlg =                  false;

    public static String DIR_ROOT = "CryptoImaging";
    public static String DIR_TICKETS = "Tickets";
    public static String DIR_GIFTCARDS = "Giftcards";
    public static String DIR_COUPONS = "Coupons";
    public static String FILE_SOCIALIMAGE = "socialconnection.jpg";

    public static String ERROR_TITLE = "Oops!";
    public static String ERROR_MESSAGE = "Our services are not responding, please try back later.";
    public static String ERROR_REGISTRATION = "It's not you, it's me! I can't call home right now. I've tried several times. Try back in a bit?";

    public Constants () {

    }
    public Constants (Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        this.BOT_TOKEN = sharedPref.getString("botApiToken", "");
        this.BOT_SERVER_URL = sharedPref.getString("botEndPoint", "");
        this.SERVICE_URL = sharedPref.getString("baseURL", "");
        this.TOKEN = sharedPref.getString("token", "");
        this.BRAND_LISENCE_CODE = sharedPref.getString("brandLicenseCode", "");
        this.PLAYER_TOKEN = sharedPref.getString("playerLicenseCode","");
        this.HOCKEY_APP_ID = sharedPref.getString("hockeyAppId","");
        this.OCR_LICENSE_CODE = sharedPref.getString("ocrLicenseCodeAndroid","");
    }

}
