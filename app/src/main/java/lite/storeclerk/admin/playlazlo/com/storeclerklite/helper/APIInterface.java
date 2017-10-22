package lite.storeclerk.admin.playlazlo.com.storeclerklite.helper;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Dev01 on 9/4/2017.
 */

public class APIInterface {

    private static final String TAG = "APIInterface";

    public static JSONObject getRetailerList(String uuid) throws Exception {
        JSONObject postParams = new JSONObject();
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("data", new JSONObject());
        Log.d(TAG, "getRetailerList: "+postParams.toString());
        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_GET_RETAILER_WITH_LOCATION, postParams.toString());
    }

    public static JSONObject getAllGameList() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_ALL_GAMES);
    }

    public static JSONObject checkoutWithCode(String code,String uuid) throws Exception {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        midParams.put("checkoutSessionShortCode", code);
        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);
        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_CHECKOUT_COMPLETE, postParams.toString());
    }

    public static JSONObject checkoutWithQRCode(String code,String uuid) throws Exception {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        midParams.put("checkoutSessionLicenseCode", code);
        midParams.put("retailerRefId", Constants.retailerRefId);
        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);
        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_CHECKOUT_WITH_QRCODE, postParams.toString());
    }

    public static JSONObject paidFullWithLicenseCode(JSONObject obj,String uuid) throws Exception {
        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        midParams.put("licenseCypherText", obj.getString("validateLicenseCode"));
        midParams.put("amountPaid", obj.getDouble("amount"));
        midParams.put("retailerRefId", Constants.retailerRefId);
        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);
        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_PAID_IN_FULL_COMPLETE, postParams.toString());
    }

    public static JSONObject claimWithCode(String code) throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_CLAIM_WITH_CODE + code);
    }

    public static JSONObject completeClaim(JSONObject obj,String uuid) throws Exception {
        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        midParams.put("claimLicenseCode", obj.getString("claimLicenseCode"));
        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);
        return new AdvancedHTTPClient().httpPutMethod(Constants.URL_CLAIM_COMPLETE, postParams.toString());
    }

    public static JSONObject verifyFBCode(String code) throws Exception {
        return new AdvancedHTTPClient().httpGetMethodWithAuthCode(Constants.URL_VERIFY_FB_AUTHCODE, code);
    }

    public static JSONObject refund(String code,String uuid) throws Exception {
        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        midParams.put("refundLicenseCode", code);
        midParams.put("amountDue", 0);
        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);
        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_CART_REFUND, postParams.toString());
    }

    public static JSONObject activateUser(String userLicenseCode) throws Exception {
        return new AdvancedHTTPClient().httpPutMethodWithUserLicenseCode(Constants.URL_ACTIVATE_USER, userLicenseCode);
    }

    public static JSONObject registerUser(String photoIdCode,String firstName,String lastName,String email,String phone,String userAccessToken,String uuid) throws Exception{
        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        JSONObject photoIdData = new JSONObject();
        JSONObject endPointMiddleData = new JSONObject();
        JSONArray middleArr = new JSONArray();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        photoIdData.put("photoIdCode", photoIdCode);
        endPointMiddleData.put("address", "");
        endPointMiddleData.put("isVerified", false);
        endPointMiddleData.put("isDefault", false);
        midParams.put("avatarBase64", "");
        midParams.put("photoId", photoIdData);
        midParams.put("retailerRefId", Constants.retailerRefId);
        midParams.put("roles", "");
        midParams.put("simulationType", "");
        midParams.put("nameFirst", firstName);
        midParams.put("nameMiddle", "");
        midParams.put("nameLast", lastName);
        midParams.put("nameAlias", "");
        endPointMiddleData.put("address", email);
        middleArr.put(endPointMiddleData);
        midParams.put("endpointsEmail", middleArr);
        endPointMiddleData.put("address", phone);
        middleArr = new JSONArray();
        middleArr.put(endPointMiddleData);
        midParams.put("endpointsVoice", middleArr);
        endPointMiddleData.put("address", phone);
        middleArr = new JSONArray();
        middleArr.put(endPointMiddleData);
        midParams.put("endpointsText", middleArr);
        midParams.put("addressLine1", "");
        midParams.put("addressLine2", "");
        midParams.put("addressCity", "");
        midParams.put("addressStateProvince", "");
        midParams.put("addressCounty", "");
        midParams.put("addressZipPostalCode", "");
        midParams.put("addressCountryCode", "");
        midParams.put("addressLocation", "");
        midParams.put("age", 0);
        midParams.put("languageCode", "");
        midParams.put("countryCode", "");
        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);
        return new AdvancedHTTPClient().httpPostMethodWithAuthCode(Constants.URL_REGISTER_USER_DATA, userAccessToken, postParams.toString());
    }

    public static JSONObject getChartData() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_REPORT_BY_DAY);
    }

    public static JSONObject registerPushNotification(String refreshedToken,String uuid) throws Exception {

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        midParams.put("app", "clrk");
        midParams.put("platform", "gcm");
        midParams.put("handle", refreshedToken);

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);

        return new AdvancedHTTPClient().httpPutMethod(Constants.URL_REGISTER_PUSH_NOTIFICATION,postParams.toString());
    }

    public static JSONObject getInactiveUsers() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_INACTIVE_USERS);
    }

    public static JSONObject getConfiguration(Context context) throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(context, Constants.URL_HOCKEY_APP_ID);
    }
}
