package ng.gov.eirs.mas.erasmpoa.utils;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.horizonpay.utils.BaseUtils;

import java.util.HashMap;
import java.util.Map;

import ng.gov.eirs.mas.erasmpoa.R;

public class GenerateBitmap {
    private GenerateBitmap() {

    }

    private static int PAPER_WIDTH = 384;  // bitmap width
//     private static Typeface FONT = Typeface.MONOSPACE;
//    private static Typeface FONT = Typeface.SERIF;
//    private static Typeface FONT = Typeface.DEFAULT;
//    private static Typeface FONT = Typeface.DEFAULT_BOLD;
//    private static Typeface FONT = Typeface.SANS_SERIF;
//    private static final Typeface FONT = Typeface.createFromAsset(BaseUtils.getApp().getAssets(), "fonts/calibri.ttf");
    static final int fontRes = R.font.texgyreheroscn_regular;
    @Nullable
    private static final Typeface FONT = ResourcesCompat.getFont(BaseUtils.getApp(), fontRes); //Typeface.SANS_SERIF;
    /**
     * setPaperWidth
     *
     * @param paperWidth
     */
    public static void setPaperWidth(int paperWidth) {
       GenerateBitmap.PAPER_WIDTH = paperWidth;
    }

    /**
     * string -> bitmap
     *
     * @param content
     * @param textSize
     * @param align
     * @param bold
     * @param dispInvert
     * @return
     */
    public static Bitmap str2Bitmap(@NonNull String content, int textSize, @NonNull AlignEnum align, boolean bold, boolean dispInvert) {
        TextPaint paint = new TextPaint();
        paint.setTypeface(FONT);
        paint.setFakeBoldText(bold);
        paint.setTextSize(textSize);
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        Rect bounds = new Rect();
        paint.getTextBounds(content, 0, content.length(), bounds);
        if (dispInvert) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }
        Layout.Alignment alignment;
        switch (align) {
            case LEFT:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
            case RIGHT:
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
            default:
                alignment = Layout.Alignment.ALIGN_CENTER;
                break;
        }

        StaticLayout myStaticLayout = new StaticLayout(content, paint, PAPER_WIDTH, alignment, 1.0f, 0.0f, false);

        int height = myStaticLayout.getHeight();
        if (height <= 0) {
            height = 30;
        }
        Bitmap bitmap = Bitmap.createBitmap(PAPER_WIDTH, (int) (height * 1.2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if (dispInvert) {
            canvas.drawRGB(0, 0, 0);
        }
        myStaticLayout.draw(canvas);

        canvas.save();
        canvas.restore();

        return bitmap;
    }

    /**
     * str->Bitmap  with append
     * @param content
     * @param textSize
     * @param align
     * @param bold
     * @param append
     * @return
     */
    public static Bitmap str2Bitmap(@NonNull String content, int textSize, @NonNull AlignEnum align, boolean bold, char append) {
        TextPaint paint = new TextPaint();
        paint.setTypeface(FONT);
        paint.setFakeBoldText(bold);
        paint.setTextSize(textSize);
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);

        Rect textBounds = new Rect(0, 0, 0, 30);    // measure content size
        if (!TextUtils.isEmpty(content)) {
            paint.getTextBounds(content, 0, content.length(), textBounds);
        }

        int height = textBounds.height();
        Bitmap bitmap = Bitmap.createBitmap(PAPER_WIDTH, (int) (height * 1.2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawRGB(255, 0, 0);

        if (!TextUtils.isEmpty(content)) {  // draw content in center
            switch (align) {
                case LEFT:
                    paint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(content, 0, bitmap.getHeight() / 2.0f + textBounds.height() / 2.0f, paint);
                    break;
                case CENTER:
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(content, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f + textBounds.height() / 2.0f, paint);
                    break;
                case RIGHT:
                    paint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(content, bitmap.getWidth(), bitmap.getHeight() / 2.0f + textBounds.height() / 2.0f, paint);
                    break;
            }
        }

        int appendPerLen = (int) paint.measureText("" + append);
        if (appendPerLen >= 1) {  // draw append
            StringBuffer realAppendStrSb = new StringBuffer();

            if (TextUtils.isEmpty(content)) {   // full of append
                int appendCount = PAPER_WIDTH / appendPerLen;
                for (int i = 0; i < appendCount; i++) {
                    realAppendStrSb.append(append);
                }
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(realAppendStrSb.toString(), bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f + textBounds.height() / 2.0f, paint);
            } else {

                switch (align) {
                    case CENTER: {   // content in center
                        int appendCount = ((PAPER_WIDTH - textBounds.width()) / 2) / appendPerLen;
                        if (appendCount >= 1) {
                            for (int i = 0; i < appendCount; i++) {  //  comb append
                                realAppendStrSb.append(append);
                            }

                            paint.setTextAlign(Paint.Align.LEFT);  // draw left append
                            canvas.drawText(realAppendStrSb.toString(), 0, bitmap.getHeight() / 2.0f + textBounds.height() / 2.0f, paint);

                            paint.setTextAlign(Paint.Align.RIGHT);  // draw right append
                            canvas.drawText(realAppendStrSb.toString(), bitmap.getWidth(), bitmap.getHeight() / 2.0f + textBounds.height() / 2.0f, paint);
                        }
                    }
                    break;

                    case LEFT: {
                        int appendCount = (PAPER_WIDTH - textBounds.width()) / appendPerLen;
                        if (appendCount >= 1) {
                            for (int i = 0; i < appendCount; i++) {  //  comb append
                                realAppendStrSb.append(append);
                            }
                            paint.setTextAlign(Paint.Align.RIGHT);  // draw right append
                            canvas.drawText(realAppendStrSb.toString(), bitmap.getWidth(), bitmap.getHeight() / 2.0f + textBounds.height() / 2.0f, paint);
                        }
                    }break;
                    case RIGHT:{
                        int appendCount = (PAPER_WIDTH - textBounds.width()) / appendPerLen;
                        if (appendCount >= 1) {
                            for (int i = 0; i < appendCount; i++) {  //  comb append
                                realAppendStrSb.append(append);
                            }
                            paint.setTextAlign(Paint.Align.LEFT);  // draw right append
                            canvas.drawText(realAppendStrSb.toString(), 0, bitmap.getHeight() / 2.0f + textBounds.height() / 2.0f, paint);
                        }
                    }break;
                }
            }
        }

        canvas.save();
        canvas.restore();

        return bitmap;
    }

    /**
     * string -> bitmap
     *
     * @param leftContent
     * @param rightContent
     * @param textSize
     * @param bold
     * @param dispInvert
     * @return
     */
    public static Bitmap str2Bitmap(@NonNull String leftContent, @NonNull String rightContent, int textSize, boolean bold, boolean dispInvert) {
        Paint paint = new Paint();
        paint.setTypeface(FONT);
        paint.setFakeBoldText(bold);
        paint.setTextSize(textSize);
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);

        Rect leftBounds = new Rect();
        paint.getTextBounds(leftContent, 0, leftContent.length(), leftBounds);

        Rect rightBounds = new Rect();
        paint.getTextBounds(rightContent, 0, rightContent.length(), rightBounds);

        if (dispInvert) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }
        int height = Math.max(leftBounds.height(), rightBounds.height());
        if (height <= 0) {
            height = 30;
        }
        Bitmap bitmap = Bitmap.createBitmap(PAPER_WIDTH, (int) (height * 1.6), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (dispInvert) {
            canvas.drawRGB(0, 0, 0);
        }

        // paint left string
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(leftContent, 0, bitmap.getHeight() / 2.0f + leftBounds.height() / 2.0f, paint);

        // paint right string
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(rightContent, bitmap.getWidth(), bitmap.getHeight() / 2.0f + leftBounds.height() / 2.0f, paint);

        return bitmap;
    }

    /**
     * 
     * @param leftContent
     * @param centerContent
     * @param rightContent
     * @param textSize
     * @param bold
     * @param dispInvert
     * @return Bitmap
     */
    public static Bitmap str2Bitmap(@NonNull String leftContent, @NonNull String centerContent, @NonNull String rightContent, int textSize, boolean bold, boolean dispInvert) {
        Paint paint = new Paint();
        paint.setTypeface(FONT);
        paint.setFakeBoldText(bold);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        Rect leftBounds = new Rect();
        paint.getTextBounds(leftContent, 0, leftContent.length(), leftBounds);  //Get rector bounds

        Rect centerBounds = new Rect();
        paint.getTextBounds(centerContent, 0, centerContent.length(), centerBounds);

        Rect rightBounds = new Rect();
        paint.getTextBounds(rightContent, 0, rightContent.length(), rightBounds);

        if (dispInvert) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }
        //Get the required length
        int height = Math.max(Math.max(leftBounds.height(), rightBounds.height()), centerBounds.height());
        if (height <= 0) {
            height = 30;
        }

        //generate canvas(background)
        Bitmap bitmap = Bitmap.createBitmap(PAPER_WIDTH, (int) (height * 1.6), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if (dispInvert) {
            canvas.drawRGB(0, 0, 0);    //background :black
        }

        // paint left string
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(leftContent, 0, bitmap.getHeight() / 2.0f + leftBounds.height() / 2.0f, paint);

        // paint CENTER string
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(centerContent, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f + centerBounds.height() / 2.0f, paint);

        // paint right string
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(rightContent, bitmap.getWidth(), bitmap.getHeight() / 2.0f + rightBounds.height() / 2.0f, paint);

        return bitmap;
    }

    /**
     * string -> bitmap
     *
     * @param leftContent
     * @param centerContent
     * @param rightContent
     * @param positionPercent center content position percentage: range: 0.0f ~ 1.0f
     * @param textSize
     * @param bold
     * @param dispInvert
     * @return
     */
    public static Bitmap str2Bitmap(@NonNull String leftContent, @NonNull String centerContent, @NonNull String rightContent, float positionPercent, int textSize, boolean bold, boolean dispInvert) {
        if (positionPercent > 1 || positionPercent < 0) {
            positionPercent = 0.5f;
        }

        Paint paint = new Paint();
        paint.setTypeface(FONT);
        paint.setFakeBoldText(bold);
        paint.setTextSize(textSize);

        Rect leftBounds = new Rect();
        paint.getTextBounds(leftContent, 0, leftContent.length(), leftBounds);  //Get rector bounds

        Rect centerBounds = new Rect();
        paint.getTextBounds(centerContent, 0, centerContent.length(), centerBounds);

        Rect rightBounds = new Rect();
        paint.getTextBounds(rightContent, 0, rightContent.length(), rightBounds);

        if (dispInvert) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.BLACK);
        }
        //Get the required length
        int height = Math.max(Math.max(leftBounds.height(), rightBounds.height()), centerBounds.height());
        if (height <= 0) {
            height = 30;
        }

        //generate canvas(background)
        Bitmap bitmap = Bitmap.createBitmap(PAPER_WIDTH, (int) (height * 1.6), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if (dispInvert) {
            canvas.drawRGB(0, 0, 0);    //background :black
        }

        // paint left string
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(leftContent, 0, bitmap.getHeight() / 2.0f + leftBounds.height() / 2.0f, paint);

        // paint CENTER string
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(centerContent, bitmap.getWidth() * positionPercent, bitmap.getHeight() / 2.0f + centerBounds.height() / 2.0f, paint);

        // paint right string
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(rightContent, bitmap.getWidth(), bitmap.getHeight() / 2.0f + rightBounds.height() / 2.0f, paint);

        return bitmap;
    }

    @Nullable
    public static Bitmap generateQRCodeBitmap(String content, int width) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            //noinspection SuspiciousNameCombination
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, width, hints);
            int[] pixels = new int[width * width];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }

            //noinspection SuspiciousNameCombination
            return Bitmap.createBitmap(pixels, 0, width, width, width, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static Bitmap generateBarCodeBitmap(String content, int width, int height) {
        MultiFormatWriter barCodeWriter = new MultiFormatWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            BitMatrix encode = barCodeWriter.encode(content, BarcodeFormat.CODE_128, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }

            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * generate a line bitmap
     *
     * @param height
     * @return
     */
    public static Bitmap generateLine(int height) {
        Bitmap bitmap = Bitmap.createBitmap(PAPER_WIDTH, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRGB(0, 0, 0);
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    /**
     * @param height
     * @return
     */
    public static Bitmap generateGap(int height) {
        Bitmap bitmap = Bitmap.createBitmap(PAPER_WIDTH, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRGB(255, 255, 255);
        canvas.save();
        canvas.restore();
        return bitmap;
    }


    /**
     * scale bitmap
     *
     * @param bitmap
     * @param xScale
     * @param yScale
     * @return
     */
    public static Bitmap scaleBitmap(@NonNull Bitmap bitmap, float xScale, float yScale) {
        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * put 1 bitmap into bitmap with align
     *
     * @param bitmap
     * @param align
     * @return
     */
    public static Bitmap formatBitmap(@NonNull Bitmap bitmap, @NonNull AlignEnum align) {
        Bitmap bitmapTmp = Bitmap.createBitmap(PAPER_WIDTH, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapTmp);
        Paint paint = new Paint();

        paint.setTextAlign(Paint.Align.LEFT);  // 先做左对齐字符串
        switch (align) {
            case LEFT:
                canvas.drawBitmap(bitmap, 0, 0, paint);
                break;
            case RIGHT:
                canvas.drawBitmap(bitmap, bitmapTmp.getWidth() - bitmap.getWidth(), 0, paint);
                break;
            default:
                canvas.drawBitmap(bitmap, (bitmapTmp.getWidth() - bitmap.getWidth()) / 2.0f, 0, paint);
                break;
        }

        canvas.save();
        canvas.restore();

        return bitmapTmp;
    }

    /**
     * put 2 bitmaps in one bitmap
     *
     * @param leftBitmap
     * @param rightBitmap
     * @return
     */
    public static Bitmap formatBitmap(@NonNull Bitmap leftBitmap, @NonNull Bitmap rightBitmap) {
        int bitmapHeight = Math.max(leftBitmap.getHeight(), rightBitmap.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(PAPER_WIDTH, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawBitmap(leftBitmap, 3, (bitmap.getHeight() - leftBitmap.getHeight()) / 2.0f, paint);

        canvas.drawBitmap(rightBitmap, bitmap.getWidth() - rightBitmap.getWidth() - 3, (bitmap.getHeight() - rightBitmap.getHeight()) / 2.0f, paint);

        canvas.save();
        canvas.restore();

        return bitmap;
    }

    /**
     * Align type
     */
    public enum AlignEnum {
        LEFT,
        CENTER,
        RIGHT
    }
}
