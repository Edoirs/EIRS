package ng.gov.eirs.mas.erasmpoa.net.exception;


import android.support.annotation.NonNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by himanshusoni on 09/08/16.
 */

public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {

    private final RxJava2CallAdapterFactory mOriginal;

    private RxErrorHandlingCallAdapterFactory() {
        mOriginal = RxJava2CallAdapterFactory.create();
    }

    public static CallAdapter.Factory create() {
        return new RxErrorHandlingCallAdapterFactory();
    }

    @Override
    public CallAdapter<Observable<?>, Object> get(@NonNull Type returnType, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        return new RxCallAdapterWrapper(retrofit, (CallAdapter<Observable<?>, Object>) mOriginal.get(returnType, annotations, retrofit));
    }

    private static class RxCallAdapterWrapper implements CallAdapter<Observable<?>, Object> {
        private final Retrofit mRetrofit;
        private final CallAdapter<Observable<?>, ?> mWrapped;

        RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<Observable<?>, Object> wrapped) {
            this.mRetrofit = retrofit;
            this.mWrapped = wrapped;
        }

        @Override
        public Type responseType() {
            return mWrapped.responseType();
        }


        @SuppressWarnings("unchecked")
        @Override
        public Object adapt(@NonNull Call<Observable<?>> call) {
            return ((Observable) mWrapped.adapt(call)).onErrorResumeNext(new Function<Throwable, ObservableSource>() {
                @Override
                public ObservableSource apply(@NonNull Throwable throwable) {
                    return Observable.error(asRetrofitException(throwable));
                }
            });
        }

        private RetrofitException asRetrofitException(Throwable throwable) {
            // We had non-200 http error
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                Response response = httpException.response();
                return RetrofitException.httpError(response.raw().request().url().toString(),
                        response, mRetrofit);
            }
            // A network error happened
            if (throwable instanceof IOException) {
                return RetrofitException.networkError((IOException) throwable);
            }

            if (throwable instanceof NoInternetConnectionException) {
                return RetrofitException.
                        noInternetConnection((NoInternetConnectionException) throwable);
            }

            // We don't know what happened. We need to simply convert to an unknown error

            return RetrofitException.unexpectedError(throwable);
        }
    }
}
