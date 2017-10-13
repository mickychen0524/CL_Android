package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Android Developer on 06/08/17.
 */

public class APIInterface {

    private static final String TAG = APIInterface.class.getSimpleName();

    public static JSONObject ticketClaimComplete(JSONObject merchandObj,float amount,String claimLicenseCode,String uuid) throws Exception {

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        JSONObject merchandiesData = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        merchandiesData.put("merchandiseRefId", merchandObj.getString("merchandiseRefId"));
        merchandiesData.put("amount", amount);

        midParams.put("claimLicenseCode", claimLicenseCode);
        midParams.put("photoBase64", JSONObject.NULL);
        midParams.put("pin", JSONObject.NULL);
        midParams.put("merchandise", new JSONArray().put(merchandiesData));

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);

        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_TICKET_CLAIM_COMPLETE, postParams.toString());
    }

    public static JSONObject getAllMerchandies() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_ALL_MERCHANDIES);
    }

    public static JSONObject getAllTerms() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_ALL_TERMS);
    }

    public static JSONObject checkoutCancel(JSONObject obj,String uuid) throws Exception {

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        midParams.put("checkoutSessionRefId", obj.getString("checkoutSessionRefId"));
        midParams.put("checkoutSessionLicenseCypherText",  obj.getString("licenseCypherText"));

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);


        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_CHECKOUT_CANCEL, postParams.toString());
    }

    public static JSONObject checkoutWithShopping(List<JSONObject> shoppingCartData, int simulationType, String uuid) throws Exception {

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        JSONArray panelSelectionArr = new JSONArray();
        JSONArray couponSelectionArr = new JSONArray();
        JSONArray giftcardSelectionArr = new JSONArray();

        for (JSONObject obj : shoppingCartData) {

            JSONObject panelSelectionParams = new JSONObject();
            JSONObject componentParams = new JSONObject();
            componentParams.put("componentRefId", "");
            componentParams.put("optionRefId", "");

            panelSelectionParams.put("externalPanelId", JSONObject.NULL);
            panelSelectionParams.put("brandRefId", obj.getString("brandRefId"));
            panelSelectionParams.put("ticketTemplateRefId", obj.getString("templateRefId"));
            panelSelectionParams.put("channelRefId", obj.getString("channelRefId"));
            panelSelectionParams.put("gameRefId", obj.getString("gameRefId"));
            panelSelectionParams.put("drawRefId", obj.getString("drawRefId"));
            panelSelectionParams.put("playAmount", obj.getDouble("playAmount"));
            panelSelectionParams.put("componentSelections", JSONObject.NULL);

            panelSelectionParams.put("data", JSONObject.NULL);
            panelSelectionParams.put("isSystemGenerated", true);

            for (int i = 0; i < obj.getInt("panelCount"); i++) {
                panelSelectionArr.put(panelSelectionParams);
            }
        }

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        midParams.put("retailerRefId", Constants.retailerRefId);
        midParams.put("simulationType", simulationType);
        midParams.put("simulationLicenseCypherText", JSONObject.NULL);
        midParams.put("trainingLicenseCypherText", "");
        midParams.put("panelSelections", panelSelectionArr);
        midParams.put("giftCardSelections", giftcardSelectionArr);
        midParams.put("couponSelections", couponSelectionArr);

        midParams.put("somethingIKnowCredentialCypherText", "8CB2237D0679CA88DB6464EAC60DA96345513964");

        midParams.put("somethingIHaveCredentialCypherText", "");
        midParams.put("isSomethingIAmCredentialImageDisplayed", false);
        midParams.put("socialCheckoutLicenseCodes", JSONObject.NULL);

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);



        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_CHECKOUT_WITH_SHOPPING, postParams.toString());
    }

    public static JSONObject ticketValidate(JSONObject ticketItem,String uuid) throws Exception {

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String sha1MediaSizeStr = AeSimpleSHA1.SHA1(String.valueOf(ticketItem.getInt("mediaSize")));
        byte[] data = AppStringHelper.hexStringToByteArray(sha1MediaSizeStr);
        String base64Str = Base64.encodeToString(data, Base64.DEFAULT);

        midParams.put("mediaHash", base64Str);
        midParams.put("validationLicenseCypherText", ticketItem.getString("licenseCypherText"));

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);

        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_TICKET_VALIDATE, postParams.toString());
    }

    public static JSONObject ticketRefund(JSONObject ticketRefundItem,String uuid) throws Exception {

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        if (ticketRefundItem.isNull("licenseCypherText")) {
            midParams.put("checkoutSessionLicenseCypherText", "");
        } else {
            midParams.put("checkoutSessionLicenseCypherText", ticketRefundItem.getString("licenseCypherText"));
        }

        if (ticketRefundItem.isNull("ticketRefId")) {
            midParams.put("ticketRefId", "00000000-0000-0000-0000-000000000000");
        } else {
            midParams.put("ticketRefId", ticketRefundItem.getString("ticketRefId"));
        }

        if (ticketRefundItem.isNull("amountPaid")) {
            midParams.put("amountDue", 0.0f);
        } else {
            midParams.put("amountDue", ticketRefundItem.getDouble("amountPaid"));
        }

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);

        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_REFUND_TICKET, postParams.toString());
    }

    public static JSONObject getDrawList(String gameRefId) throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_DRAW_PREFIX+gameRefId+Constants.URL_GET_DRAW_SURFIX);
    }

    public static JSONObject checkoutWithShoppingGift(List<JSONObject> shoppingCartData, int simulationType, String uuid) throws Exception {

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        JSONArray panelSelectionArr = new JSONArray();
        JSONArray couponSelectionArr = new JSONArray();
        JSONArray giftcardSelectionArr = new JSONArray();


        for(JSONObject shoppingCartItem:shoppingCartData){
            JSONObject giftcardObj = new JSONObject();
            giftcardObj.put("merchantRefId", shoppingCartItem.get("merchandiseRefId"));
            giftcardObj.put("Value", shoppingCartItem.get("Value"));
            giftcardSelectionArr.put(giftcardObj);
        }

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        midParams.put("retailerRefId", Constants.retailerRefId);
        midParams.put("simulationType", simulationType);
        midParams.put("simulationLicenseCypherText", JSONObject.NULL);
        midParams.put("trainingLicenseCypherText", "");
        midParams.put("giftCardSelections", giftcardSelectionArr);
        midParams.put("couponSelections", couponSelectionArr);
        midParams.put("panelSelections", panelSelectionArr);

        midParams.put("somethingIKnowCredentialCypherText", "8CB2237D0679CA88DB6464EAC60DA96345513964");

        midParams.put("somethingIHaveCredentialCypherText", "");
        midParams.put("isSomethingIAmCredentialImageDisplayed", false);
        midParams.put("socialCheckoutLicenseCodes", JSONObject.NULL);

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);



        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_CHECKOUT_WITH_SHOPPING, postParams.toString());
    }
    public static JSONObject checkoutWithShopping(JSONObject channelData,float value, int simulationType, String uuid) throws Exception {

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        JSONArray panelSelectionArr = new JSONArray();
        JSONArray couponSelectionArr = new JSONArray();
        JSONArray giftcardSelectionArr = new JSONArray();

        JSONObject giftcardObj = new JSONObject();
        giftcardObj.put("merchantRefId", channelData.get("merchandiseRefId"));
        giftcardObj.put("Value", value);
        giftcardSelectionArr.put(giftcardObj);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        midParams.put("retailerRefId", Constants.retailerRefId);
        midParams.put("simulationType", simulationType);
        midParams.put("simulationLicenseCypherText", JSONObject.NULL);
        midParams.put("trainingLicenseCypherText", "");
        midParams.put("giftCardSelections", giftcardSelectionArr);
        midParams.put("couponSelections", couponSelectionArr);
        midParams.put("panelSelections", panelSelectionArr);

        midParams.put("somethingIKnowCredentialCypherText", "8CB2237D0679CA88DB6464EAC60DA96345513964");

        midParams.put("somethingIHaveCredentialCypherText", "");
        midParams.put("isSomethingIAmCredentialImageDisplayed", false);
        midParams.put("socialCheckoutLicenseCodes", JSONObject.NULL);

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);



        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_CHECKOUT_WITH_SHOPPING, postParams.toString());
    }

    public static Bitmap downloadSocialImage() throws Exception{
        return new AdvancedHTTPClient().httpDownloadSocialImage(Constants.URL_DOWNLOAD_SOCIAL_PHOTO);
    }

    public static JSONObject registerPushNotification(String refreshedToken,String uuid) throws Exception {
        Log.i(TAG, "registerPushNotification : " + refreshedToken);

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        midParams.put("app", "plyr");
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

    public static JSONObject registerWithImage(int ageValue,String userSelfie,String uuid) throws Exception {


        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        JSONObject userParams = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        userParams.put("Key", "age");
        userParams.put("value", ageValue);

        midParams.put("data", new JSONArray().put(userParams));
        midParams.put("languageCode", "en");
        midParams.put("countryCode", "US");
        midParams.put("selfieBase64", userSelfie);

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);

        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_REGISTER_USER_WITH_IMAGE,postParams.toString());
    }

    public static JSONObject uploadReceipt(String encodedImage) throws Exception {
        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_GETTING_UPLOAD_RECEIPT_IMG, encodedImage);
    }

    public static JSONObject getChannelAndBrands() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_CHANNEL_AND_BRANDS_LIST);
    }

    public static JSONObject getGames() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_ALL_GAMES_LIST);
    }

    public static JSONObject getCheckoutStatusSession(JSONObject checkoutRes) throws Exception {
        String licenseCypherText = checkoutRes.getString("licenseCypherText");
        return new AdvancedHTTPClient().httpGetMethodWithActionCode(Constants.URL_GET_CHECKOUT_STATUS_SESSION, licenseCypherText);
    }

    public static JSONObject getCheckoutStatusGlobal(JSONObject checkoutRes) throws Exception {
        String licenseCypherText = checkoutRes.getString("licenseCypherText");
        return new AdvancedHTTPClient().httpGetMethodWithActionCode(Constants.URL_GLOBAL_CHECKOUT_GETTING, licenseCypherText);
    }

    public static JSONObject getRewards() throws Exception{
        return  new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_REWARDS);
    }

    public static JSONObject getRetailerByLocation(String uuid) throws Exception {
        return new AdvancedHTTPClient().httpGetBrandMethod(Constants.URL_GET_RETAILER_BY_LOCATION+uuid+"/"+Constants.GEO_LATITUDE+"/"+Constants.GEO_LONGITUDE+"/"+Constants.MIN_DISTANCE_RETAILER);
    }

    public static JSONObject giftcardClaim(JSONObject cardItem, String uuid, double amountClaimed, String validationCode) throws Exception {

        Log.d(TAG, "giftcardClaim() called with: cardItem = [" + cardItem + "], uuid = [" + uuid + "], amountClaimed = [" + amountClaimed + "]");

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String sha1MediaSizeStr = AeSimpleSHA1.SHA1(String.valueOf(cardItem.getInt("mediaSize")));
        byte[] data = AppStringHelper.hexStringToByteArray(sha1MediaSizeStr);
        String base64Str = Base64.encodeToString(data, Base64.DEFAULT);

//        base64Str.replaceAll("\n", "");
        midParams.put("mediaHash", base64Str.trim());
        midParams.put("amountClaimed", amountClaimed);
        midParams.put("validationCode", validationCode);
        midParams.put("somethingIKnowCredentialCypherText", "8CB2237D0679CA88DB6464EAC60DA96345513964");//8CB2237D0679CA88DB6464EAC60DA96345513964 but null for now

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);

        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_CLAIM_GIFTCARD, postParams.toString());
    }

    public static JSONObject getConfiguration() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_HOCKEY_APP_ID);
    }

    public static JSONObject socialConnect(String playerSocialLicenceCodeCypherText) throws Exception {
        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();
        midParams.put("playerSocialLicenceCodeCypherText", playerSocialLicenceCodeCypherText);
        postParams.put("data", midParams);
        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_SOCIAL_CONNECT,postParams.toString());
    }


    public static JSONObject couponItem(JSONObject cardItem, String uuid, String validationCode) throws Exception {

        Log.d(TAG, "giftcardClaim() called with: cardItem = [" + cardItem + "], uuid = [" + uuid + "]");

        JSONObject postParams = new JSONObject();
        JSONObject midParams = new JSONObject();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String sha1MediaSizeStr = AeSimpleSHA1.SHA1(String.valueOf(cardItem.getInt("mediaSize")));
        byte[] data = AppStringHelper.hexStringToByteArray(sha1MediaSizeStr);
        String base64Str = Base64.encodeToString(data, Base64.DEFAULT);

        midParams.put("mediaHash", base64Str);
        midParams.put("validationCode", validationCode);
        midParams.put("somethingIKnowCredentialCypherText", "8CB2237D0679CA88DB6464EAC60DA96345513964");//8CB2237D0679CA88DB6464EAC60DA96345513964 but null for now

        postParams.put("correlationRefId", Constants.CORRE_REF_ID);
        postParams.put("uuid", uuid);
        postParams.put("latitude", Constants.GEO_LATITUDE);
        postParams.put("longitude", Constants.GEO_LONGITUDE);
        postParams.put("createdOn", currentDateTimeString);
        postParams.put("data", midParams);

        return new AdvancedHTTPClient().httpPostMethod(Constants.URL_COUPON, postParams.toString());
    }
    public static JSONObject getProximityUrl() throws Exception {
        return new AdvancedHTTPClient().httpGetMethod(Constants.URL_GET_PROXIMITY_DATA);
    }
}
