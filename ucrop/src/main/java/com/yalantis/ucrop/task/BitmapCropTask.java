package com.yalantis.ucrop.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.yalantis.ucrop.callback.BitmapCropCallback;
import com.yalantis.ucrop.model.CropParameters;
import com.yalantis.ucrop.model.ExifInfo;
import com.yalantis.ucrop.model.ImageState;
import com.yalantis.ucrop.util.BitmapLoadUtils;
import com.yalantis.ucrop.util.ColorFilterGenerator;
import com.yalantis.ucrop.util.FileUtils;
import com.yalantis.ucrop.util.ImageHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * Crops part of image that fills the crop bounds.
 * <p/>
 * First image is downscaled if max size was set and if resulting image is larger that max size.
 * Then image is rotated accordingly.
 * Finally new Bitmap object is created and saved to file.
 */
public class BitmapCropTask extends AsyncTask<Void, Void, Throwable> {

    private static final String TAG = "BitmapCropTask";

    static {
        System.loadLibrary("ucrop");
    }

    private final WeakReference<Context> contextRef;
    private Bitmap mViewBitmap;

    private final RectF mCropRect;
    private final RectF mCurrentImageRect;

    private float mCurrentScale;
    private final float mCurrentAngle;
    private final int mMaxResultImageSizeX, mMaxResultImageSizeY;

    private final Bitmap.CompressFormat mCompressFormat;
    private final int mCompressQuality;
    private final String mImageInputPath, mImageOutputPath;
    private final Uri mImageInputUri, mImageOutputUri;
    private final ExifInfo mExifInfo;
    private final BitmapCropCallback mCropCallback;

    private final float mBrightness;
    private final float mContrast;
    private final float mSaturation;

    private final float mSharpness;

    private int mCroppedImageWidth, mCroppedImageHeight;
    private int cropOffsetX, cropOffsetY;

    public BitmapCropTask(@NonNull Context context, @Nullable Bitmap viewBitmap, @NonNull ImageState imageState, @NonNull CropParameters cropParameters,
                          @Nullable BitmapCropCallback cropCallback) {
        contextRef = new WeakReference<>(context);
        mViewBitmap = viewBitmap;
        mCropRect = imageState.getCropRect();
        mCurrentImageRect = imageState.getCurrentImageRect();

        mCurrentScale = imageState.getCurrentScale();
        mCurrentAngle = imageState.getCurrentAngle();
        mMaxResultImageSizeX = cropParameters.getMaxResultImageSizeX();
        mMaxResultImageSizeY = cropParameters.getMaxResultImageSizeY();

        mCompressFormat = cropParameters.getCompressFormat();
        mCompressQuality = cropParameters.getCompressQuality();

        mImageInputPath = cropParameters.getImageInputPath();
        mImageOutputPath = cropParameters.getImageOutputPath();
        mImageInputUri = cropParameters.getContentImageInputUri();
        mImageOutputUri = cropParameters.getContentImageOutputUri();
        mExifInfo = cropParameters.getExifInfo();

        mBrightness = cropParameters.getBrightness();
        mContrast = cropParameters.getContrast();
        mSaturation = cropParameters.getSaturation();

        mSharpness = cropParameters.getSharpness();

        mCropCallback = cropCallback;
    }

    @Override
    @Nullable
    protected Throwable doInBackground(Void... params) {
        if (mViewBitmap == null) {
            return new NullPointerException("ViewBitmap is null");
        } else if (mViewBitmap.isRecycled()) {
            return new NullPointerException("ViewBitmap is recycled");
        } else if (mCurrentImageRect.isEmpty()) {
            return new NullPointerException("CurrentImageRect is empty");
        }

        float resizeScale = resize();

        try {
            crop(resizeScale);

            if (mBrightness != 0.0f || mContrast != 0.0f || mSaturation != 0.0f || mSharpness != 0.0f) {
                Bitmap sourceBitmap = BitmapFactory.decodeFile(mImageOutputPath);
                Bitmap alteredBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), sourceBitmap.getConfig());

                ColorMatrix cm = new ColorMatrix();
                ColorFilterGenerator.adjustBrightness(cm, mBrightness);
                ColorFilterGenerator.adjustContrast(cm, mContrast);
                ColorFilterGenerator.adjustSaturation(cm, mSaturation);

                ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(cm);

                Canvas canvas = new Canvas(alteredBitmap);
                Paint paint = new Paint();
                paint.setColorFilter(colorFilter);
                Matrix matrix = new Matrix();
                canvas.drawBitmap(sourceBitmap, matrix, paint);

                if (mSharpness != 0.0f) {
                    RenderScript rs = RenderScript.create(contextRef.get());

                    // Allocate buffers
                    Allocation inAllocation = Allocation.createFromBitmap(rs, sourceBitmap);
                    Allocation outAllocation = Allocation.createFromBitmap(rs, alteredBitmap);

                    // Load script
                    ScriptIntrinsicConvolve3x3 sharpnessScript = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
                    sharpnessScript.setInput(inAllocation);
                    float[] coefficients = {
                            0, -mSharpness, 0,
                            -mSharpness, 1 + (4 * mSharpness), -mSharpness,
                            0, -mSharpness, 0};
                    sharpnessScript.setCoefficients(coefficients);
                    sharpnessScript.forEach(outAllocation);
                    outAllocation.copyTo(alteredBitmap);

                    inAllocation.destroy();
                    outAllocation.destroy();
                    sharpnessScript.destroy();
                    rs.destroy();
                }

                File file = new File(mImageOutputPath);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                alteredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
            }

            mViewBitmap = null;
        } catch (Throwable throwable) {
            return throwable;
        }

        return null;
    }

    private float resize() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageInputPath, options);

        boolean swapSides = mExifInfo.getExifDegrees() == 90 || mExifInfo.getExifDegrees() == 270;
        float scaleX = (swapSides ? options.outHeight : options.outWidth) / (float) mViewBitmap.getWidth();
        float scaleY = (swapSides ? options.outWidth : options.outHeight) / (float) mViewBitmap.getHeight();

        float resizeScale = Math.min(scaleX, scaleY);

        mCurrentScale /= resizeScale;

        resizeScale = 1;
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            float cropWidth = mCropRect.width() / mCurrentScale;
            float cropHeight = mCropRect.height() / mCurrentScale;

            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {

                scaleX = mMaxResultImageSizeX / cropWidth;
                scaleY = mMaxResultImageSizeY / cropHeight;
                resizeScale = Math.min(scaleX, scaleY);

                mCurrentScale /= resizeScale;
            }
        }
        return resizeScale;
    }

    private boolean crop(float resizeScale) throws IOException {
        Context context = contextRef.get();
        if (context == null) {
            return false;
        }

        // Downsize if needed
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            float cropWidth = mCropRect.width() / mCurrentScale;
            float cropHeight = mCropRect.height() / mCurrentScale;

            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(mViewBitmap,
                        Math.round(mViewBitmap.getWidth() * resizeScale),
                        Math.round(mViewBitmap.getHeight() * resizeScale), false);
                if (mViewBitmap != resizedBitmap) {
                    mViewBitmap.recycle();
                }
                mViewBitmap = resizedBitmap;

                mCurrentScale /= resizeScale;
            }
        }

        // Rotate if needed
        if (mCurrentAngle != 0) {
            Matrix tempMatrix = new Matrix();
            tempMatrix.setRotate(mCurrentAngle, (float) mViewBitmap.getWidth() / 2, (float) mViewBitmap.getHeight() / 2);

            Bitmap rotatedBitmap = Bitmap.createBitmap(mViewBitmap, 0, 0, mViewBitmap.getWidth(), mViewBitmap.getHeight(),
                    tempMatrix, true);
            if (mViewBitmap != rotatedBitmap) {
                mViewBitmap.recycle();
            }
            mViewBitmap = rotatedBitmap;
        }

        cropOffsetX = Math.round((mCropRect.left - mCurrentImageRect.left) / mCurrentScale);
        cropOffsetY = Math.round((mCropRect.top - mCurrentImageRect.top) / mCurrentScale);
        mCroppedImageWidth = Math.round(mCropRect.width() / mCurrentScale);
        mCroppedImageHeight = Math.round(mCropRect.height() / mCurrentScale);

        boolean shouldCrop = shouldCrop(mCroppedImageWidth, mCroppedImageHeight);
        Log.i(TAG, "Should crop: " + shouldCrop);

        if (shouldCrop) {
            saveImage(Bitmap.createBitmap(mViewBitmap, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight));
            if (mCompressFormat.equals(Bitmap.CompressFormat.JPEG)) {
                copyExifForOutputFile(context);
            }
            return true;
        } else {
            FileUtils.copyFile(context, mImageInputUri, mImageOutputUri);
            return false;
        }
    }

    private void copyExifForOutputFile(Context context) throws IOException {
        boolean hasImageInputUriContentSchema = BitmapLoadUtils.hasContentScheme(mImageInputUri);
        boolean hasImageOutputUriContentSchema = BitmapLoadUtils.hasContentScheme(mImageOutputUri);
        /*
         * ImageHeaderParser.copyExif with output uri as a parameter
         * uses ExifInterface constructor with FileDescriptor param for overriding output file exif info,
         * which doesn't support ExitInterface.saveAttributes call for SDK lower than 21.
         *
         * See documentation for ImageHeaderParser.copyExif and ExifInterface.saveAttributes implementation.
         */
        if (hasImageInputUriContentSchema && hasImageOutputUriContentSchema) {
            ImageHeaderParser.copyExif(context, mCroppedImageWidth, mCroppedImageHeight, mImageInputUri, mImageOutputUri);
        } else if (hasImageInputUriContentSchema) {
            ImageHeaderParser.copyExif(context, mCroppedImageWidth, mCroppedImageHeight, mImageInputUri, mImageOutputPath);
        } else if (hasImageOutputUriContentSchema) {
            ExifInterface originalExif = new ExifInterface(mImageInputPath);
            ImageHeaderParser.copyExif(context, originalExif, mCroppedImageWidth, mCroppedImageHeight, mImageOutputUri);
        } else {
            ExifInterface originalExif = new ExifInterface(mImageInputPath);
            ImageHeaderParser.copyExif(originalExif, mCroppedImageWidth, mCroppedImageHeight, mImageOutputPath);
        }
    }

    private void saveImage(@NonNull Bitmap croppedBitmap) {
        Context context = contextRef.get();
        if (context == null) {
            return;
        }

        OutputStream outputStream = null;
        ByteArrayOutputStream outStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(mImageOutputUri);
            outStream = new ByteArrayOutputStream();
            croppedBitmap.compress(mCompressFormat, mCompressQuality, outStream);
            outputStream.write(outStream.toByteArray());
            croppedBitmap.recycle();
        } catch (IOException exc) {
            Log.e(TAG, exc.getLocalizedMessage());
        } catch (NullPointerException exc) {
            Log.e(TAG, exc.getLocalizedMessage());
        } finally {
            BitmapLoadUtils.close(outputStream);
            BitmapLoadUtils.close(outStream);
        }
    }

    /**
     * Check whether an image should be cropped at all or just file can be copied to the destination path.
     * For each 1000 pixels there is one pixel of error due to matrix calculations etc.
     *
     * @param width  - crop area width
     * @param height - crop area height
     * @return - true if image must be cropped, false - if original image fits requirements
     */
    private boolean shouldCrop(int width, int height) {
        int pixelError = 1;
        pixelError += Math.round(Math.max(width, height) / 1000f);
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0)
                || Math.abs(mCropRect.left - mCurrentImageRect.left) > pixelError
                || Math.abs(mCropRect.top - mCurrentImageRect.top) > pixelError
                || Math.abs(mCropRect.bottom - mCurrentImageRect.bottom) > pixelError
                || Math.abs(mCropRect.right - mCurrentImageRect.right) > pixelError
                || mCurrentAngle != 0;
    }

    @Override
    protected void onPostExecute(@Nullable Throwable t) {
        if (mCropCallback != null) {
            if (t == null) {
                Uri uri = Uri.fromFile(new File(mImageOutputPath));
                mCropCallback.onBitmapCropped(uri, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight);
            } else {
                mCropCallback.onCropFailure(t);
            }
        }
    }
}