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

package lite.storeclerk.admin.playlazlo.com.storeclerklite.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    public static final int DOWNLOAD_EXCEPTION =                    10;

    // Endpoint we are targeting for the deployed WebAPI service
    public static String SERVICE_URL =                              "";
    public static String TOKEN =                                    "";
    public static String PLAYER_TOKEN =                             "";
    public static String FB_AUTHONTICATION_CODE =                   "";
    public static String FB_ACCESS_TOKEN =                          "";
    public static String GEO_LATITUDE =                             "0.00";
    public static String GEO_LONGITUDE =                            "0.00";
    public static double GEO_LATITUDE_DOUB =                        0.00;
    public static double GEO_LONGITUDE_DOUB =                       0.00;
    public static String CORRE_REF_ID =                             "00000000-0000-0000-0000-000000000000";

    public static List<JSONObject> gameGlobalArr =                  new ArrayList<>();
    public static List<JSONObject> brandGlobalArr =                 new ArrayList<>();
    public static List<JSONObject> globalRetailerArr =                 new ArrayList<>();
    public static List<JSONObject> channelGroupGlobalArr =          new ArrayList<>();
    public static String retailerRefId =                            "e2a5b746-41b1-490f-ab90-4d53ac8a5d94";
    public static String SETTING_BLE_UUID =                         "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";

    public static final String URL_GET_RETAILER_WITH_LOCATION =   "/api/v1/retailers/display/bylocation";

    public static final String URL_GET_ALL_GAMES =                  "/api/v2/games/display";
    public static final String URL_CHECKOUT_COMPLETE =              "/api/v1/shopping/checkout/complete/pending/lite";
    public static final String URL_CHECKOUT_WITH_QRCODE =           "/api/v3/shopping/checkout/complete/pending";
    public static final String URL_PAID_IN_FULL_COMPLETE =          "/api/v2/shopping/checkout/complete";
    public static final String URL_CLAIM_WITH_CODE =                "/api/v1/claim/coupon/claim/pending/";
    public static final String URL_CLAIM_COMPLETE =                 "/api/v1/claim/coupon/claim/complete";
    public static final String URL_VERIFY_FB_AUTHCODE =             "/api/v1/user/fbaccountkit/login";
    public static final String URL_FB_ACCOUNT_LOGOUT =              "/api/v1/user/fbaccountkit/logout";
    public static final String URL_CART_REFUND =                    "/api/v1/shopping/entity/refund";
    public static final String URL_REGISTER_USER_DATA =             "/api/v1/user/clerk/create";
    public static final String URL_REPORT_BY_DAY =                  "/api/admin/v1/reports/user/panels/byday";
    public static final String URL_PROXIMITY_UPLOAD =               "/api/v1/shopping/proximity/upload/url";

    public static final String URL_REGISTER_PUSH_NOTIFICATION =   "/api/v2/notification/user/register";

    // retailer getting flag
    public static boolean b_getRetailerListFlg =                  false;

    public Constants () {

    }
    public Constants (Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

        this.SERVICE_URL = sharedPref.getString("serviceURL", "");
        this.TOKEN = sharedPref.getString("token", "");
        this.PLAYER_TOKEN = sharedPref.getString("playerToken", "");
        this.FB_AUTHONTICATION_CODE = sharedPref.getString("fbAuthenticationCode", "");
        this.FB_ACCESS_TOKEN = sharedPref.getString("fbAccessToken", "");

    }

}
