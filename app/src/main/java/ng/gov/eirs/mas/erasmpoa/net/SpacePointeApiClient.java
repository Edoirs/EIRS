package ng.gov.eirs.mas.erasmpoa.net;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.google.gson.Gson;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import ng.gov.eirs.mas.erasmpoa.data.ApiEndpoint;
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider;
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse;
import ng.gov.eirs.mas.erasmpoa.net.exception.RxErrorHandlingCallAdapterFactory;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by himanshusoni on Oct 24, 2018
 */
public class SpacePointeApiClient {

    public static Api getClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        HttpHeaderInterceptor headers = new HttpHeaderInterceptor();

        String creds = String.format("%s:%s", "revenue", "revenuelogin");
        // no wrap works in multipart
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        headers.addHeader("Authorization", auth);
        headers.addHeader("Accept-Language", "en-US");
        headers.addHeader("Connection", "close"); // to avoid unexpected end of stream error

        HttpErrorInterceptor errorInterceptor = new HttpErrorInterceptor(context);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(headers)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(logging)
                .addInterceptor(errorInterceptor)
                .build();

        Gson gson = GsonProvider.getGson();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEndpoint.SPACE_POINTE_SERVER)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .client(client)
                .build();

        return retrofit.create(Api.class);
    }

    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    @NonNull
    public static RequestBody createRequestBody(String descriptionString) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }

    @NonNull
    public static RequestBody createRequestBody(@NonNull File file) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), file);
    }

    @NonNull
    public static MultipartBody.Part prepareFilePart(String partName, File file) {
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    public interface Api {

        @FormUrlEncoded
        @POST("masapi/auth/login")
        Observable<ApiResponse> login(
                @Field("userName") String email,
                @Field("password") String password
        );

        @FormUrlEncoded
        @POST("masapiv1/submission/add")
        Observable<ApiResponse> saveSubmission(
                @Field("token") String token,

                @Field("MenuSection") String menuSection,
                @Field("GroupName") String groupName,
                @Field("SubGroupName") String subGroupName,
                @Field("CategoryName") String categoryName,
                @Field("priceSheetAggregateId") int priceSheetAggregateId,
                @Field("expectedAmount") String expectedAmount,

                @Field("taxPayerTypeId") int taxPayerTypeId,
                @Field("taxPayerName") String taxPayerName,
                @Field("taxPayerPhoneNumber") String taxPayerPhoneNumber,
                @Field("areaId") int areaId,
                @Field("address") String address,
                @Field("assessableIncome") String assessableIncome,
                @Field("taxAssessed") String taxAssessed,

                @Field("amountToPay") String amountToPay,
                @Field("notes") String notes,
                @Field("settlementMethod") int settlementMethod,
                @Field("scratchCard") String scratchCard,

                @Field("submissionType") String submissionType,
                @Field("offlineId") String offlineId
        );
    }
}
