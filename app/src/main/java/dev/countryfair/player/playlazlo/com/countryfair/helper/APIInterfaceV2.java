package dev.countryfair.player.playlazlo.com.countryfair.helper;


import java.util.Map;

import dev.countryfair.player.playlazlo.com.countryfair.model.ReceiptRequestBody;
import dev.countryfair.player.playlazlo.com.countryfair.model.ReceiptResponse;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface APIInterfaceV2 {


    @GET(Constants.URL_GETTING_UPLOAD_RECEIPT_IMG)
    Call<ReceiptResponse> getUploadUrl();

    @Headers({
            "Content-Type: image/png",
            "Accept: application/json",
            "x-ms-blob-type: BlockBlob",
            "x-ms-blob-content-type: image/png"
    })

    @Multipart
    @PUT
    Call<ResponseBody> uploadReceiptImage(@Url String uploadUrl, @HeaderMap Map<String, String> headers, @Part MultipartBody.Part image);

    @PUT(Constants.URL_RECEIPT_VERIFICATION)
    Call<ResponseBody> putReceiptOCR(@HeaderMap Map<String, String> headers, @Body ReceiptRequestBody body);

    @Multipart
    @PUT
    Call<ResponseBody> uploadBleData(@Url String uploadUrl, @HeaderMap Map<String, String> headers, @Part MultipartBody.Part bleData);

}
