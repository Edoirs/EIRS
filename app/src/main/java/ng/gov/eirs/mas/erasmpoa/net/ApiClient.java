package ng.gov.eirs.mas.erasmpoa.net;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import ng.gov.eirs.mas.erasmpoa.FlavouredConstants;
import ng.gov.eirs.mas.erasmpoa.data.ApiEndpoint;
import ng.gov.eirs.mas.erasmpoa.data.GsonProvider;
import ng.gov.eirs.mas.erasmpoa.data.model.ApiResponse;
import ng.gov.eirs.mas.erasmpoa.data.model.SubmissionApiResponse;
import ng.gov.eirs.mas.erasmpoa.net.exception.RxErrorHandlingCallAdapterFactory;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by himanshusoni on Oct 4, 2018
 */
public class ApiClient {

    public static Api getClient(Context context, boolean addAuthHeader) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        HttpHeaderInterceptor headers = new HttpHeaderInterceptor();

        if (addAuthHeader) {
            String creds = String.format("%s:%s", "revenue", "revenuelogin");
            // no wrap works in multipart
            String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
            headers.addHeader("Authorization", auth);
        }
        headers.addHeader("Accept-Language", "en-US");
        headers.addHeader("Connection", "close"); // to avoid unexpected end of stream error

        HttpErrorInterceptor errorInterceptor = new HttpErrorInterceptor(context);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(headers);

        // solution 1
        enableTls12OnPreLollipop(builder);

        // solution 2
        // builder.sslSocketFactory(new TLSSocketFactory())

        // solution 3
        // ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
        //        .supportsTlsExtensions(true)
        //        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
        //        .cipherSuites(
        //                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
        //                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
        //                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
        //                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
        //        )
        //        .build();
        // builder.followSslRedirects(true);
        // builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, spec));

        // solution 4
        setSocketFactoryIfNeeded(context, builder);

        builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(logging)
                .addInterceptor(errorInterceptor);
        OkHttpClient client = builder.build();

        Gson gson = GsonProvider.getGson();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiEndpoint.SERVER)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .client(client)
                .build();

        return retrofit.create(Api.class);
    }

    private static void setSocketFactoryIfNeeded(Context context, OkHttpClient.Builder builder) {
        try {
            if (FlavouredConstants.CERTIFICATE_RESOURCE == 0) {
                return;
            }
            // Load CAs from an InputStream
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            InputStream inputStream = context.getResources().openRawResource(FlavouredConstants.CERTIFICATE_RESOURCE); //(.crt)
            Certificate certificate = certificateFactory.generateCertificate(inputStream);
            inputStream.close();

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            // Create a TrustManager that trusts the CAs in our KeyStore.
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
            trustManagerFactory.init(keyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            X509TrustManager x509TrustManager = (X509TrustManager) trustManagers[0];

            // Create an SSLSocketFactory that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            builder.sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

//    private static KeyStore readKeyStore(Context context) {
//        KeyStore ks = null;
//        try {
//            ks = KeyStore.getInstance(KeyStore.getDefaultType());
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }
//
//        // get user password and file input stream
//        char[] password = "ez24get".toCharArray();
//
//        InputStream fis = null;
//        try {
//            fis = context.getResources().openRawResource(R.raw.doberman2cert);
//            try {
//                if (ks != null) {
//                    ks.load(fis, password);
//                }
//            } catch (CertificateException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            }
//        } finally {
//            if (fis != null) {
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return ks;
//    }

//    private static SSLSocketFactory getSocketFactory(Context context) {
//        SSLSocketFactory socketFactory = null;
//
//        try {
//            SSLContext sslContext = getSslContext();
//            KeyStore keyStore = readKeyStore(context); //your method to obtain KeyStore
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(keyStore);
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            keyManagerFactory.init(keyStore, "ez24get".toCharArray());
//            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
//            socketFactory = sslContext.getSocketFactory();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnrecoverableKeyException e) {
//            e.printStackTrace();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        }
//
//        return socketFactory;
//    }

    private static SSLContext getSslContext() throws NoSuchAlgorithmException {
        if (Build.VERSION.SDK_INT < 21) {
            return SSLContext.getInstance("TLSv1.2");
        } else {
            return SSLContext.getInstance("SSL");
        }
    }

    private static void enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT < 21) {
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:"
                            + Arrays.toString(trustManagers));
                }
                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()), trustManager);

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
            }
        }
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
        @POST("mpoapi/appVersion/getVersion")
        Observable<ApiResponse> checkVersion(
                @Field("token") String token,
                @Field("employeeId") long employeeId,
                @Field("organizationId") long organizationId
        );

        @FormUrlEncoded
        @POST("mpoapi/auth/login")
        Observable<ApiResponse> login(
                @Field("userName") String email,
                @Field("password") String password
        );

        @FormUrlEncoded
        @POST("mpoapi/auth/forgotPassword")
        Observable<ApiResponse> recover(
                @Field("businessPhone") String businessPhone
        );

        @FormUrlEncoded
        @POST("mpoapi/auth/resetPassword")
        Observable<ApiResponse> resetPassword(
                @Field("employeeId") long employeeId,
                @Field("organizationId") long organizationId,
                @Field("password") String password
        );

        @FormUrlEncoded
        @POST("mpoapi/assessment/detailByRefNo")
        Observable<ApiResponse> assessmentDetailByRef(
                @Field("token") String token,
                @Field("refNo") String refNo
        );

        @FormUrlEncoded
        @POST("mpoapi/serviceBill/detailByRefNo")
        Observable<ApiResponse> serviceBillDetailByRef(
                @Field("token") String token,
                @Field("refNo") String refNo
        );

        @FormUrlEncoded
        @POST("mpoapi/assessment/ruleDetail")
        Observable<ApiResponse> assessmentRuleDetail(
                @Field("token") String token,
                @Field("id") Long id
        );

        @FormUrlEncoded
        @POST("mpoapi/serviceBill/serviceDetail")
        Observable<ApiResponse> serviceBillMdaServiceDetail(
                @Field("token") String token,
                @Field("id") Long id
        );

        @FormUrlEncoded
        @POST("mpoapi/assessment/itemDetail")
        Observable<ApiResponse> assessmentItemDetail(
                @Field("token") String token,
                @Field("id") Long id
        );

        @FormUrlEncoded
        @POST("mpoapi/serviceBill/itemDetail")
        Observable<ApiResponse> serviceBillItemDetail(
                @Field("token") String token,
                @Field("id") Long id
        );

        @FormUrlEncoded
        @POST("mpoapi/assessment/settlementDetail")
        Observable<ApiResponse> assessmentSettlementDetail(
                @Field("token") String token,
                @Field("id") Long id
        );

        @FormUrlEncoded
        @POST("mpoapi/serviceBill/settlementDetail")
        Observable<ApiResponse> serviceBillSettlementDetail(
                @Field("token") String token,
                @Field("id") Long id
        );

        @FormUrlEncoded
        @POST("mpoapi/scratchcard/verifyAmount")
        Observable<ApiResponse> scratchCardVerify(
                @Field("token") String token,
                @Field("scratchCard") String scratchCard
        );

        @FormUrlEncoded
        @POST("mpoapi/scratchcard/verifyMpoaScratchcard")
        Observable<ApiResponse> scratchCardVerifyMpoa(
                @Field("token") String token,
                @Field("scratchCard") String scratchCard
        );

        @FormUrlEncoded
        @POST("mpoapi/scratchcard/addSettlementBillInfo")
        Observable<ApiResponse> saveCollection(
                @Field("token") String token,
                @Field("billRefType") String billRefType,
                @Field("billRef") String billRef,
                @Field("billId") long billId,
                @Field("scratchCard") String scratchCard,
                @Field("amount") String amount,
                @Field("notes") String notes,
                @Field("lstSettlementItems") String lstSettlementItems
        );

        @GET("mpoapi/scratchcard/scDenomList/")
        Observable<ApiResponse> denominations(
                @Query("token") String token,
                @Query("datetime") String datetime
        );

        @GET("mpoapi/scratchcard/scDenominationScratchCardList/")
        Observable<ApiResponse> scratchCards(
                @Query("token") String token,
                @Query("set") int set,
                @Query("count") int count,
                @Query("onlyActivated") int onlyActivated,
                @Query("datetime") String datetime,
                @Query("denomination") double denomination
        );

        @FormUrlEncoded
        @POST("mpoapi/pricesheet/groupList")
        Observable<ApiResponse> psGroupList(
                @Field("token") String token,
                @Field("menuSection") String menuSection
        );

        @FormUrlEncoded
        @POST("mpoapi/pricesheet/subGroupList")
        Observable<ApiResponse> psSubGroupList(
                @Field("token") String token,
                @Field("menuSection") String menuSection,
                @Field("groupName") String groupName
        );

        @FormUrlEncoded
        @POST("mpoapi/pricesheet/categoryList")
        Observable<ApiResponse> psCategoryList(
                @Field("token") String token,
                @Field("menuSection") String menuSection,
                @Field("groupName") String groupName,
                @Field("subGroupName") String subGroupName
        );

        @FormUrlEncoded
        @POST("mpoapi/pricesheet/edoAreaList")
        Observable<ApiResponse> lgaList(
                @Field("token") String token
        );

        @FormUrlEncoded
        @POST("mpoapi/pricesheet/taxPayerTypeList")
        Observable<ApiResponse> taxPayerTypeList(
                @Field("token") String token
        );

        @FormUrlEncoded
        @POST("mpoapi/pricesheet/priceSheetAggregateList")
        Observable<ApiResponse> psAmount(
                @Field("token") String token,
                @Field("menuSection") String menuSection,
                @Field("groupName") String groupName,
                @Field("subGroupName") String subGroupName,
                @Field("categoryName") String categoryName
        );

        @FormUrlEncoded
        @POST("mpoapi/pricesheet/allPriceSheetAggregateList")
        Observable<ApiResponse> psList(
                @Field("token") String token,
                @Field("menuSection") String menuSection,
                @Field("perPage") int perPage,
                @Field("page") int page
        );

        @FormUrlEncoded
        @Headers({
                "X-Username: spcaepointe",
                "X-Password: 2:vTyYAm6L",
        })
        @POST("mcav1/submission/add/")
        Observable<SubmissionApiResponse> saveSubmission(
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

        // ------------------ Haulage

        @GET("mpoapi/haulage/lgalist")
        Observable<ApiResponse> haulageLgaList(
                @Query("token") String token
        );

        @GET("mpoapi/haulage/revenueTypeList")
        Observable<ApiResponse> haulageRevenueTypeList(
                @Query("token") String token
        );

        @GET("mpoapi/haulage/beatList")
        Observable<ApiResponse> haulageBeatList(
                @Query("token") String token
        );

        @FormUrlEncoded
        @POST("mpoapi/haulage/submissionAdd")
        Observable<ApiResponse> haulageSubmissionAdd(
                @Field("token") String token,
                @Field("areaId") int areaId,
                @Field("revenueTypeId") int revenueTypeId,
                @Field("beatId") int beatId,
                @Field("vehicleRegistrationNo") String vehicleRegistrationNo,
                @Field("taxPayerTypeId") int taxPayerTypeId,
                @Field("name") String taxPayerName,
                @Field("phoneNumber") String phoneNumber,
                @Field("offlineId") String offlineId
        );

        // ------------------ Haulage End

        @FormUrlEncoded
        @POST("mpoapi/targetAmount/getTargetAmount")
        Observable<ApiResponse> getTargetAmount(
                @Field("token") String token,
                @Field("employeeId") long employeeId,
                @Field("organizationId") long organizationId
        );
    }
}
