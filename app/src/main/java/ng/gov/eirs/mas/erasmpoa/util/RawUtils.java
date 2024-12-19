package ng.gov.eirs.mas.erasmpoa.util;

import android.content.Context;
import android.support.annotation.RawRes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by himanshusoni on 25/05/17.
 */

public class RawUtils {

    public static String getString(Context context, @RawRes int rawResourceId) throws IOException {
        InputStream is = context.getResources().openRawResource(rawResourceId);
        String statesText = convertStreamToString(is);
        is.close();
        return statesText;
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int i = is.read();
        while (i != -1) {
            outputStream.write(i);
            i = is.read();
        }
        return outputStream.toString();
    }
}
