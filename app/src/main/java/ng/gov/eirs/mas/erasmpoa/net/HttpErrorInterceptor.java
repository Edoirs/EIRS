package ng.gov.eirs.mas.erasmpoa.net;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;

import ng.gov.eirs.mas.erasmpoa.net.exception.NoInternetConnectionException;
import ng.gov.eirs.mas.erasmpoa.util.ContextExtensionsKt;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by himanshusoni on 09/08/16.
 */
public class HttpErrorInterceptor implements Interceptor {

    private Context mContext;

    public HttpErrorInterceptor(Context context) {
        super();
        mContext = context;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if (!ContextExtensionsKt.isConnectedToNetwork(mContext)) {
            throw new NoInternetConnectionException("Not connected to internet");
        }
        return chain.proceed(chain.request());
    }
}
