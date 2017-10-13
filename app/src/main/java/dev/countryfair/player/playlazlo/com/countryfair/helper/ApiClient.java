package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ApiClient {

    public static final String TAG = "ApiClient";

    private static APIInterfaceV2 api;

    public static APIInterfaceV2 getInstance(Context context) {
        if (api == null) {
            init();
        }
        return api;
    }

    private static void init() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(buildHttpClient())
                .baseUrl(Constants.SERVICE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        api = retrofit.create(APIInterfaceV2.class);
    }

    private static OkHttpClient buildHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder();

                //requestBuilder.addHeader("Content-Type", "application/json");
                requestBuilder.addHeader("Lazlo-AuthorityLicenseCode", Constants.TOKEN);
                requestBuilder.addHeader("Lazlo-PlayerLicenseCode", Constants.PLAYER_TOKEN);
                requestBuilder.addHeader("Lazlo-BrandLicenseCode", Constants.BRAND_LISENCE_CODE);

                //.header("Authorization: Bearer :", "auth-value");

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        clientBuilder.addInterceptor(logging);

        //OkHttpClient client = clientBuilder.build();

        return clientBuilder.build();
    }

}
