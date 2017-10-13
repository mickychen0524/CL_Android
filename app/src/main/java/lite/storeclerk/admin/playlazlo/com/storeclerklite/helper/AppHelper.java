package lite.storeclerk.admin.playlazlo.com.storeclerklite.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppHelper {

    private Context context;

    public AppHelper() {

    }

    public AppHelper(Context context) {
        this.context = context;
    }

    public static void close(Closeable obj){
        if(obj!=null){
            try {
                obj.close();
            }catch (IOException e){
                //ignore
            }
        }
    }

    public static List<JSONObject> parseFromJsonList(JSONArray jsonArr) {
        try {
            List<JSONObject> items = new ArrayList<>();
            JSONObject obj = null;
            for (int i = 0; i < jsonArr.length(); i++) {
                obj = jsonArr.getJSONObject(i);
                items.add(obj);
            }
            return items;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Determine font color based on background color
    @ColorInt
    public static int getContrastColor(@ColorInt int color) {
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return a < 0.5 ? Color.BLACK : Color.WHITE;
    }

    //********************************************//
    // Manage local file from device
    //********************************************//
    public void deleteAllLocalTicketFile() {
        String localFilePathForTicket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/VALottery/Tickets/";
        File dir = new File(localFilePathForTicket);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
            saveInitTicketData();
        }
    }

    //********************************************//
    // Shopping Cart JSON helper function for local
    //********************************************//

    public String saveInitShoppingCart() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        JSONArray initShoppingCart = new JSONArray();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("shoppingCartData", initShoppingCart.toString());
        editor.apply();
        return initShoppingCart.toString();
    }
    public JSONArray getShoppingCartDataFromLocal() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("shoppingCartData", "");

        try {
            return new JSONArray(jsonStr);
        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
            return new JSONArray();
        }

    }

    public void saveOneShoppingCartToLocal(JSONObject jsonObj) {

        boolean updateFlg = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("shoppingCartData", "");

        if (jsonStr.equals("")) {
            jsonStr = saveInitShoppingCart();
        }
        try {

            JSONArray shoppingCartArr = new JSONArray(jsonStr);
            List<JSONObject> shopArrList = parseFromJsonList(shoppingCartArr);
            List<JSONObject> newShopArrList = new ArrayList<>();

            for (int i = 0; i < shoppingCartArr.length(); i++) {
                JSONObject eqObj = shopArrList.get(i);
                if (jsonObj.getString("gameRefId").equals(eqObj.getString("gameRefId")) &&
                        jsonObj.getString("drawRefId").equals(eqObj.getString("drawRefId")) &&
                        jsonObj.getString("channelGroupRefId").equals(eqObj.getString("channelGroupRefId")) &&
                        jsonObj.getString("channelRefId").equals(eqObj.getString("channelRefId")) &&
                        jsonObj.getString("playAmount").equals(eqObj.getString("playAmount"))) {

                    int newCount = jsonObj.getInt("panelCount") + eqObj.getInt("panelCount");
                    eqObj.put("panelCount", newCount);
                    updateFlg = true;
                }
                newShopArrList.add(eqObj);
            }

            if (!updateFlg) {
                newShopArrList.add(jsonObj);
            }

            String sam = new JSONArray(newShopArrList).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("shoppingCartData", new JSONArray(newShopArrList).toString());
            editor.apply();

        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
        }
    }

    public void updateOneShoppingCartToLocal(JSONObject jsonObj) {

        boolean updateFlg = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("shoppingCartData", "");

        if (jsonStr.equals("")) {
            jsonStr = saveInitShoppingCart();
        }
        try {

            JSONArray shoppingCartArr = new JSONArray(jsonStr);
            List<JSONObject> shopArrList = parseFromJsonList(shoppingCartArr);
            List<JSONObject> newShopArrList = new ArrayList<>();

            for (JSONObject eqObj : shopArrList) {
                if (jsonObj.getString("gameRefId").equals(eqObj.getString("gameRefId")) &&
                        jsonObj.getString("drawRefId").equals(eqObj.getString("drawRefId")) &&
                        jsonObj.getString("channelGroupRefId").equals(eqObj.getString("channelGroupRefId")) &&
                        jsonObj.getString("channelRefId").equals(eqObj.getString("channelRefId")) &&
                        jsonObj.getString("playAmount").equals(eqObj.getString("playAmount"))) {

                    int newCount = jsonObj.getInt("panelCount");
                    eqObj.put("panelCount", newCount);
                    updateFlg = true;
                }
                newShopArrList.add(eqObj);
            }

            if (!updateFlg) {
                newShopArrList.add(jsonObj);
            }

            String sam = new JSONArray(newShopArrList).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("shoppingCartData", new JSONArray(newShopArrList).toString());
            editor.apply();

        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
        }
    }

    public void deleteOneShoppingCartItem(JSONObject jsonObj) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("shoppingCartData", "");
        if (jsonStr.equals("")) {
            jsonStr = saveInitShoppingCart();
        }
        try {

            JSONArray shoppingCartArr = new JSONArray(jsonStr);
            List<JSONObject> shopArrList = parseFromJsonList(shoppingCartArr);

            for (int i = 0; i < shoppingCartArr.length(); i++) {
                JSONObject eqObj = shopArrList.get(i);

                if (jsonObj.getString("gameRefId").equals(eqObj.getString("gameRefId")) &&
                        jsonObj.getString("drawRefId").equals(eqObj.getString("drawRefId")) &&
                        jsonObj.getString("channelGroupRefId").equals(eqObj.getString("channelGroupRefId")) &&
                        jsonObj.getString("channelRefId").equals(eqObj.getString("channelRefId")) &&
                        jsonObj.getString("playAmount").equals(eqObj.getString("playAmount"))) {
                    shopArrList.remove(i);
                    break;
                }
            }
            String sam = new JSONArray(shopArrList).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("shoppingCartData", new JSONArray(shopArrList).toString());
            editor.apply();

        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
        }

    }

    //********************************************//
    //  Ticket Success JSON helper function for local
    //********************************************//

    public String saveInitTicketData() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        JSONArray initTicketData = new JSONArray();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("successedTicketData", initTicketData.toString());
        editor.apply();
        return initTicketData.toString();
    }
    public JSONArray getTicketDataFromLocal() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("successedTicketData", "");

        try {
            return new JSONArray(jsonStr);
        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
            return new JSONArray();
        }

    }

    public void saveOneTicketDataToLocal(JSONObject jsonObj) {

        boolean isExist = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("successedTicketData", "");

        if (jsonStr.equals("")) {
            jsonStr = saveInitTicketData();
        }
        try {

            JSONArray successedTicketArr = new JSONArray(jsonStr);
            List<JSONObject> ticketArrList = parseFromJsonList(successedTicketArr);
            List<JSONObject> newticketArrList = new ArrayList<>();

            for (int i = 0; i < successedTicketArr.length(); i++) {
                if (jsonObj.getString("ticketRefId").equals(ticketArrList.get(i).getString("ticketRefId")) &&
                        jsonObj.getString("id").equals(ticketArrList.get(i).getString("id"))) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                newticketArrList.add(jsonObj);
            }

            for (JSONObject oldObj : ticketArrList) {
                newticketArrList.add(oldObj);
            }

            String sam = new JSONArray(newticketArrList).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("successedTicketData", new JSONArray(newticketArrList).toString());
            editor.apply();

        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
        }
    }

    public void updateOneTicketDataToLocal(JSONObject jsonObj, int style) {

        boolean updateFlg = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("successedTicketData", "");
        if (jsonStr.equals("")) {
            jsonStr = saveInitTicketData();
        }
        try {

            JSONArray successedTicketArr = new JSONArray(jsonStr);
            List<JSONObject> ticketArrList = parseFromJsonList(successedTicketArr);
            List<JSONObject> newticketArrList = new ArrayList<>();

            for (JSONObject eqObj : ticketArrList) {

                if (jsonObj.getString("ticketRefId").equals(eqObj.getString("ticketRefId")) &&
                        jsonObj.getString("ticketTemplateRefId").equals(eqObj.getString("ticketTemplateRefId")) &&
                        jsonObj.getString("authorityRefId").equals(eqObj.getString("authorityRefId"))) {

                    switch (style) {
                        case 1:
                            eqObj.put("isValid", jsonObj.getBoolean("isValid"));
                            eqObj.put("isCompleted", jsonObj.getBoolean("isCompleted"));
                            break;
                        case 2:
                            eqObj.put("licenseCypherText", jsonObj.getString("licenseCypherText"));
                            break;
                        case 3:
                            eqObj.put("isClaimed", jsonObj.getBoolean("isClaimed"));
                            break;
                        case 4:
                            eqObj.put("downloaded", jsonObj.getBoolean("downloaded"));
                            break;
                    }

                    updateFlg = true;
                }
                newticketArrList.add(eqObj);
            }

            String sam = new JSONArray(newticketArrList).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("successedTicketData", new JSONArray(newticketArrList).toString());
            editor.apply();

        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
        }
    }

    public void deleteOneTicketDataItem(JSONObject jsonObj) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("successedTicketData", "");
        if (jsonStr.equals("")) {
            jsonStr = saveInitTicketData();
        }
        try {

            JSONArray ticketDataArr = new JSONArray(jsonStr);
            List<JSONObject> ticketArrList = parseFromJsonList(ticketDataArr);

            for (int i = 0; i < ticketDataArr.length(); i++) {

                if (jsonObj.getString("ticketRefId").equals(ticketArrList.get(i).getString("ticketRefId")) &&
                        jsonObj.getString("ticketTemplateRefId").equals(ticketArrList.get(i).getString("ticketTemplateRefId")) &&
                        jsonObj.getString("authorityRefId").equals(ticketArrList.get(i).getString("authorityRefId"))) {
                    ticketArrList.remove(i);
                    break;
                }
            }
            String sam = new JSONArray(ticketArrList).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("successedTicketData", new JSONArray(ticketArrList).toString());
            editor.apply();

        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
        }

    }
    //********************************************//
    //  Ticket Error JSON helper function for local
    //********************************************//

    public String saveInitTicketErrorData() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        JSONArray initTicketErrorData = new JSONArray();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("failedTicketData", initTicketErrorData.toString());
        editor.apply();

        return initTicketErrorData.toString();
    }
    public JSONArray getTicketErrorDataFromLocal() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("failedTicketData", "");

        try {
            return new JSONArray(jsonStr);
        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
            return new JSONArray();
        }

    }

    public void saveOneTicketErrorDataToLocal(JSONObject jsonObj) {

        boolean isExist = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("failedTicketData", "");

        if (jsonStr.equals("")) {
            jsonStr = saveInitTicketErrorData();
        }
        try {

            JSONArray failedTicketArr = new JSONArray(jsonStr);
            List<JSONObject> ticketErrorArrList = parseFromJsonList(failedTicketArr);
            List<JSONObject> newticketErrorArrList = new ArrayList<>();

            for (int i = 0; i < failedTicketArr.length(); i++) {
                if (jsonObj.getString("ticketRefId").equals(ticketErrorArrList.get(i).getString("ticketRefId")) &&
                        jsonObj.getString("id").equals(ticketErrorArrList.get(i).getString("id"))) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                newticketErrorArrList.add(jsonObj);
            }

            for (JSONObject oldObj : ticketErrorArrList) {
                newticketErrorArrList.add(oldObj);
            }

            String sam = new JSONArray(newticketErrorArrList).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("failedTicketData", new JSONArray(newticketErrorArrList).toString());
            editor.apply();

        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
        }
    }

    public void deleteOneTicketErrorDataItem(JSONObject jsonObj) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonStr = sharedPref.getString("failedTicketData", "");
        if (jsonStr.equals("")) {
            jsonStr = saveInitTicketErrorData();
        }
        try {

            JSONArray ticketErrorDataArr = new JSONArray(jsonStr);
            List<JSONObject> ticketErrorArrList = parseFromJsonList(ticketErrorDataArr);

            for (int i = 0; i < ticketErrorDataArr.length(); i++) {

                if (jsonObj.getString("ticketRefId").equals(ticketErrorArrList.get(i).getString("ticketRefId")) &&
                        jsonObj.getString("id").equals(ticketErrorArrList.get(i).getString("id"))) {
                    ticketErrorArrList.remove(i);
                    break;
                }
            }
            String sam = new JSONArray(ticketErrorArrList).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("failedTicketData", new JSONArray(ticketErrorArrList).toString());
            editor.apply();

        } catch (Exception e) {
            Log.d("json exception-->", e.getMessage());
        }

    }
}
