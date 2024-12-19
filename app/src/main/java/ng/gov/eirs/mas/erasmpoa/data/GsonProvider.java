package ng.gov.eirs.mas.erasmpoa.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ng.gov.eirs.mas.erasmpoa.util.DateUtilKt;

public class GsonProvider {
    public static Gson getGson() {
        return new GsonBuilder().setDateFormat(DateUtilKt.getSERVER_DATE_FORMAT())
                .create();
    }
}
