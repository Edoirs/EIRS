package ng.gov.eirs.mas.erasmpoa.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CombBitmap {
    private static final String LOG_TAG = "CombBitmap";

    private int maxHeight = Integer.MAX_VALUE;  // max height
    private int currentHeight = 0;
    private final List<Bitmap> bitmapList = new ArrayList<>(); // store per bitmap

    /**
     * max bitmap height
     * @param height
     */
    public void setMaxHeight(int height){
        maxHeight = height;
    }

    /**
     * add bitmap
     * @param bitmap
     * @return
     */
    public boolean addBitmap(@Nullable Bitmap bitmap){
        if(bitmap == null){
//            AppLogUtils.warn(true, LOG_TAG, "addBitmap: bitmap = null ");
            return false;
        }
        if((currentHeight + bitmap.getHeight()) >= maxHeight){
//            AppLogUtils.warn(true, LOG_TAG, "addBitmap: Height error ");
            return false;
        }

        bitmapList.add(bitmap);
        currentHeight += bitmap.getHeight();

        return true;
    }

    /**
     * get comb bitmap
     * @return
     */
    @Nullable
    public Bitmap getCombBitmap(){
        if(bitmapList.size() == 0){
//            AppLogUtils.warn(true, LOG_TAG, "No bitmap found");
            return null;
        }

        int width = 0;   // calac max width
        for (Bitmap bitmap : bitmapList) {
            if(bitmap.getWidth() > width){
                width = bitmap.getWidth();
            }
        }

        int height = currentHeight;

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawRGB(255, 255, 255);

        Paint paint = new Paint();

        int heightIndex = 0;
        for (Bitmap bitmapTmp : bitmapList) {
            canvas.drawBitmap(bitmapTmp, 0, heightIndex, paint);
            heightIndex += bitmapTmp.getHeight();
        }

        canvas.save();
        canvas.restore();

        return result;

    }

    /**
     * remove all bitmaps
     */
    public void removeAll(){
        bitmapList.clear();
        currentHeight = 0;
    }
}
