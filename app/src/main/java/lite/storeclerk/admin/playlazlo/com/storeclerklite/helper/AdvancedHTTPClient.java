package lite.storeclerk.admin.playlazlo.com.storeclerklite.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mymac on 3/14/17.
 */

public class AdvancedHTTPClient {

    private class HTTPGetWithHeader extends AsyncTask<String, Void, JSONObject> {

//        public HTTPGetWithHeader() {
//
//        }
//
//        public HTTPGetWithHeader(Context context) {
//
//        }
        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                URL url = new URL(Constants.SERVICE_URL + params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Lazlo-AuthorityLicenseCode", Constants.TOKEN);
                conn.setRequestProperty("Lazlo-UserLicenseCode", Constants.PLAYER_TOKEN);
                conn.setRequestProperty("Authorization", Constants.HEADER_AUTHORIZATION_VALUE_PREFIX + Constants.FB_AUTHONTICATION_CODE);

                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String apiOutput = br.readLine();
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }

                br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String apiOutput = br.readLine();
                return new JSONObject(apiOutput);
            } catch (Exception e) {
                Log.d("json_e-->", e.getMessage());
            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    private class HTTPGetWithOutHeader extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                URL url = new URL(Constants.SERVICE_URL + params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Lazlo-AuthorityLicenseCode", Constants.TOKEN);

                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String apiOutput = br.readLine();
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }

                br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String apiOutput = br.readLine();
                return new JSONObject(apiOutput);
            } catch (Exception e) {
                Log.d("json_e-->", e.getMessage());
            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    private class HTTPGetWithHeaderUsingAuthCode extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                URL url = new URL(Constants.SERVICE_URL + params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Lazlo-AuthorityLicenseCode", Constants.TOKEN);
                conn.setRequestProperty("Lazlo-UserLicenseCode", Constants.PLAYER_TOKEN);
                conn.setRequestProperty("Authorization", Constants.HEADER_AUTHORIZATION_VALUE_PREFIX + params[1]);

                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String apiOutput = br.readLine();
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }

                br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String apiOutput = br.readLine();
                return new JSONObject(apiOutput);
            } catch (Exception e) {
                Log.d("json_e-->", e.getMessage());
            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    private class HTTPPostWithBody extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                URL url = new URL(Constants.SERVICE_URL + params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Lazlo-AuthorityLicenseCode", Constants.TOKEN);
                conn.setRequestProperty("Lazlo-UserLicenseCode", Constants.PLAYER_TOKEN);
                conn.setRequestProperty("Authorization", Constants.HEADER_AUTHORIZATION_VALUE_PREFIX + Constants.FB_AUTHONTICATION_CODE);

                // Send post request
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[1]);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String apiOutput = br.readLine();
                    throw new Exception("invalid response code:" + responseCode);
                }
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String apiOutput = br.readLine();
                JSONObject obj = new JSONObject(apiOutput);

                return obj;
            } catch (Exception e) {
                //TODO - do what?
            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    private class HTTPPostWithBodyUsingAuthCode extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                URL url = new URL(Constants.SERVICE_URL + params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Lazlo-AuthorityLicenseCode", Constants.TOKEN);
                conn.setRequestProperty("Lazlo-UserLicenseCode", Constants.PLAYER_TOKEN);
                conn.setRequestProperty("Authorization", Constants.HEADER_AUTHORIZATION_VALUE_PREFIX + params[1]);

                // Send post request
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[2]);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String apiOutput = br.readLine();
                    throw new Exception("invalid response code:" + responseCode);
                }
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String apiOutput = br.readLine();
                JSONObject obj = new JSONObject(apiOutput);

                return obj;
            } catch (Exception e) {
                Log.e("http->error", e.getMessage());
                //TODO - do what?
            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    private class HTTPPutWithBody extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                URL url = new URL(Constants.SERVICE_URL + params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Lazlo-AuthorityLicenseCode", Constants.TOKEN);
                conn.setRequestProperty("Lazlo-UserLicenseCode", Constants.PLAYER_TOKEN);

                // Send post request
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[1]);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String apiOutput = br.readLine();
                    throw new Exception("invalid response code:" + responseCode);
                }
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String apiOutput = br.readLine();
                JSONObject obj = new JSONObject();
                return obj;
            } catch (Exception e) {
                return null;
            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    private class HTTPPutWithUserLicenseCode extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                URL url = new URL(Constants.SERVICE_URL + params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Lazlo-AuthorityLicenseCode", Constants.TOKEN);
                conn.setRequestProperty("Lazlo-UserLicenseCode", params[1]);
                conn.setRequestProperty("Authorization", Constants.HEADER_AUTHORIZATION_VALUE_PREFIX + Constants.FB_AUTHONTICATION_CODE);

                // Send post request
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[1]);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String apiOutput = br.readLine();
                    throw new Exception("invalid response code:" + responseCode);
                }
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String apiOutput = br.readLine();
                JSONObject obj = new JSONObject();
                return obj;
            } catch (Exception e) {
                return null;
            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    public JSONObject httpGetMethod(Context context, String url) throws Exception {
        return new HTTPGetWithOutHeader().execute(url).get();
    }

    public JSONObject httpGetMethod(String url) throws Exception {
        return new HTTPGetWithHeader().execute(url).get();
    }

    public JSONObject httpGetMethodWithAuthCode(String url, String authCode) throws Exception {
        return new HTTPGetWithHeaderUsingAuthCode().execute(url, authCode).get();
    }

    public JSONObject httpPostMethod(String url, String body) throws Exception {
        return new HTTPPostWithBody().execute(url, body).get();
    }

    public JSONObject httpPostMethodWithAuthCode(String url, String authCode, String body) throws Exception {
        return new HTTPPostWithBodyUsingAuthCode().execute(url, authCode, body).get();
    }

    public JSONObject httpPutMethod(String url, String body) throws Exception {
        return new HTTPPutWithBody().execute(url, body).get();
    }

    public JSONObject httpPutMethodWithUserLicenseCode(String url, String userLicenseCode) throws Exception {
        return new HTTPPutWithUserLicenseCode().execute(url, userLicenseCode).get();
    }
}
