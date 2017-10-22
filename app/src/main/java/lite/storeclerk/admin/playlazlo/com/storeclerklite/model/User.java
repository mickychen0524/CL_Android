package lite.storeclerk.admin.playlazlo.com.storeclerklite.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by administrator on 10/20/17.
 */

public class User {
    public String firstname;
    public String lastname;
    public String base64QRCode;

    public User(JSONObject userObject) {
        try {
            firstname = userObject.getJSONObject("user").getString("nameFirst");
            lastname = userObject.getJSONObject("user").getString("nameLast");
            base64QRCode = userObject.getString("qrCodeBase64");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
