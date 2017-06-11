package com.hoangthien.hackernews.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * ThienHD
 */
public class ImageLoader {

    public static final int ANIMATED = 2009;

    /**
     * Chọn chỉ số này để load hình với kích thước gốc
     */
    public static final int SIZE_ORIGINAL = 0;

    /**
     * Chọn chỉ số này để load hình với kích thước tự động khớp với ImageView
     */
    public static final int SIZE_AUTO_FIT = -1;

    private static final int READ_SIZE = 4096;

    /**
     * Thread pool dùng cho việc tải ảnh, vì việc tải ảnh có thể sẽ thay đổi thứ
     * tự các ảnh cần tải hoặc hủy bớt những ảnh ko cần thiết nữa nên dùng
     * thread kết hợp với một list để xử lý, dùng handler sẽ không đáp ứng được
     */
    private static final int CORE_DOWNLOAD_POOL_SIZE = 4;
    private static final int MAXIMUM_DOWNLOAD_POOL_SIZE = 4;
    private static final int KEEP_ALIVE_TIME = 3;
    private BlockingQueue<Runnable> mDownloadTaskQueue;
    private ThreadPoolExecutor mDownloadThreadPool;

    /**
     * Thread pool dùng cho việc thực hiện decode ảnh
     */
    private static final int CORE_DECODE_POOL_SIZE = Math.max(Runtime
            .getRuntime().availableProcessors() - 1, 1);
    private BlockingQueue<Runnable> mDecodeTaskQueue;
    private ThreadPoolExecutor mDecodeThreadPool;

    /**
     * Danh sách các url đang chờ download
     */
    private final ArrayList<String> mWaitingDownloadURLs = new ArrayList<>();

    /**
     * Danh sách các url đang chờ decode
     */
    private ArrayList<String> mWaitingDecodeURLs = new ArrayList<>();

    /**
     * Danh sách các url đang được xử lý
     */
    private ArrayList<String> mProcessingURLs = new ArrayList<>();

    /**
     * Bảng mapping giữa một url và những imageview đăng ký hiển thị ảnh load từ
     * url này
     */
    private final HashMap<String, ArrayList<ImageCallback>> mReferenceImageCallbacks = new HashMap<>();

    /**
     * Bảng lưu thông tin ImageView nào hiện đang chờ load url nào, vì mỗi
     * ImageView chỉ có thể chọn một URL duy nhất là url đăng ký gần nhất nên
     * dùng cái này để quản lý
     */
    private volatile WeakHashMap<ImageView, String> mImageViews = new WeakHashMap<>();

    private MemoryCache memoryCache;

    private Resources mResource;

    private static final Object mLock = new Object();
    private static ImageLoader mInstance;
    private AsyncTaskManager mAsyntask;

    public static ImageLoader getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new ImageLoader(context.getApplicationContext());
            }
            return mInstance;
        }
    }

    public static ImageLoader getInstance() {
        return mInstance;
    }

    private ImageLoader(Context context) {
        mResource = context.getResources();
        memoryCache = new MemoryCache();
        mAsyntask = AsyncTaskManager.getInstance(context);
        initPools();
        String cacheFoler =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getAbsolutePath() :
                        context.getCacheDir().getAbsolutePath();
        FileCacheUtils.setCachePath(cacheFoler);
    }

    private void initPools() {
        /* Init download thread pool */
        mDownloadTaskQueue = new LinkedBlockingQueue<>();
        mDownloadThreadPool = new ThreadPoolExecutor(CORE_DOWNLOAD_POOL_SIZE,
                MAXIMUM_DOWNLOAD_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                mDownloadTaskQueue);

        mDownloadThreadPool.allowCoreThreadTimeOut(true);

		/* Init decode thread pool */
        mDecodeTaskQueue = new LinkedBlockingQueue<>();
        mDecodeThreadPool = new ThreadPoolExecutor(CORE_DECODE_POOL_SIZE,
                CORE_DECODE_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                mDecodeTaskQueue);
        mDecodeThreadPool.allowCoreThreadTimeOut(true);
    }

    private class DownloadRunnable implements Runnable {
        private final String mUrl;

        public DownloadRunnable(String url) {
            mUrl = url;
        }

        @Override
        public void run() {
            ArrayList<ImageCallback> callbacks = null;
            ImageCallback icb;
            synchronized (mReferenceImageCallbacks) {
                icb = null;
                if (mReferenceImageCallbacks.containsKey(mUrl)) {
                    callbacks = mReferenceImageCallbacks.get(mUrl);
                    if (callbacks != null && !callbacks.isEmpty()) {
                        icb = callbacks.get(0);
                    }
                }
            }
            if (icb != null && !icb.option.noCache) {
                Bitmap bitmap = memoryCache.get(mUrl);
                if (bitmap != null) {

                    if (icb.imageView.get() != null) {
                        if (icb.option.url.equals(mImageViews.get(icb.imageView.get()))) {
                            setImageAsync(icb.imageView.get(), bitmap, icb.option);
                            mImageViews.remove(icb.imageView.get());
                        }
                    } else if (icb.callback.get() != null) {
                        icb.callback.get().onLoadingFinished(mUrl, bitmap);
                    }

                    synchronized (mReferenceImageCallbacks) {
                        ArrayList<ImageCallback> cbs = mReferenceImageCallbacks
                                .get(mUrl);
                        if (cbs != null)
                            cbs.remove(icb);
                    }
                    while (mProcessingURLs.contains(mUrl)) {
                        mProcessingURLs.remove(mUrl);
                    }
                    return;
                }
            }
            File file = FileCacheUtils.getCacheFileByUrl(
                    FileCacheUtils.CACHE_TYPE_IMAGE, mUrl);
            if (!file.exists()) {
                // Chưa có trong file cache --> tải về
                downloadImage(mUrl, file);
            }

            synchronized (mReferenceImageCallbacks) {
                // Giờ thì file đã được tải về
                if (file.exists()) {
                    // tải xong rồi giờ chạy bên decode thread pool
                    if (mWaitingDecodeURLs.isEmpty()
                            && mDecodeTaskQueue.isEmpty()) {
                        DecodeRunnable decodeRunnable = new DecodeRunnable(mUrl);
                        mDecodeThreadPool.execute(decodeRunnable);
                    } else {
                        mWaitingDecodeURLs.add(mUrl);
                    }
                }

                if (!mWaitingDownloadURLs.isEmpty()) {
                    String url = mWaitingDownloadURLs.remove(0);
                    mProcessingURLs.add(url);
                    DownloadRunnable downloadRunnable = new DownloadRunnable(
                            url);
                    mDownloadThreadPool.execute(downloadRunnable);
                }
            }
            FileCacheUtils.validateCache(FileCacheUtils.CACHE_TYPE_IMAGE);
        }

    }

    private class DecodeRunnable implements Runnable {
        private final String mUrl;

        public DecodeRunnable(String url) {
            mUrl = url;
        }

        @Override
        public void run() {
            ArrayList<ImageCallback> callbacks = null;
            ImageCallback icb;

            while (true) {
                synchronized (mReferenceImageCallbacks) {
                    icb = null;
                    if (mReferenceImageCallbacks.containsKey(mUrl)) {
                        callbacks = mReferenceImageCallbacks.get(mUrl);
                        if (callbacks != null && !callbacks.isEmpty()) {
                            icb = callbacks.get(0);
                        }
                    }
                }

                if (icb != null) {
                    if (icb.imageView.get() != null) {
                        if (icb.option.url.equals(mImageViews.get(icb.imageView.get()))) {
                            File file = FileCacheUtils.getCacheFileByUrl(
                                    FileCacheUtils.CACHE_TYPE_IMAGE, mUrl);
                            decodeImageAndSet(file, icb.imageView, mUrl, icb.option);
                            mImageViews.remove(icb.imageView.get());
                            final ImageCallback temp = icb;
                            if (!TextUtils.isEmpty(icb.option.fullImage)) {
                                temp.option.url = temp.option.fullImage;
                                mAsyntask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadImage(temp.imageView.get(), temp.option);
                                    }
                                }, AsyncTaskManager.PRIORITY_RUN_ON_MAIN);
                            }
                        }
                    } else if (icb.callback.get() != null) {
                        File file = FileCacheUtils.getCacheFileByUrl(
                                FileCacheUtils.CACHE_TYPE_IMAGE, mUrl);
                        decodeImageAndCallback(file, icb.callback, mUrl,
                                icb.option);
                    }

                    synchronized (mReferenceImageCallbacks) {
                        ArrayList<ImageCallback> cbs = mReferenceImageCallbacks
                                .get(mUrl);
                        if (cbs != null)
                            cbs.remove(icb);
                    }
                } else {
                    synchronized (mReferenceImageCallbacks) {
                        if (!mWaitingDecodeURLs.isEmpty()) {
                            String url = mWaitingDecodeURLs.remove(0);
                            mProcessingURLs.add(url);
                            DecodeRunnable decodeRunnable = new DecodeRunnable(
                                    url);
                            mDecodeThreadPool.execute(decodeRunnable);
                        }
                        break;
                    }
                }
                while (mProcessingURLs.contains(mUrl)) {
                    mProcessingURLs.remove(mUrl);
                }
            }
        }
    }

    /**
     * Decode ảnh, kèm hiệu ứng và đưa vào imageView để hiển thị
     */
    private void decodeImageAndSet(File file,
                                   final WeakReference<ImageView> imageViewRef, final String url,
                                   Option option) {

        try {
            if (!option.noCache) {
                final Bitmap bitmapCache = memoryCache.get(url);
                if (bitmapCache != null && checkImageAndUrl(imageViewRef.get(), url)) {
                    ImageView imageView = imageViewRef.get();
                    setImageAsync(imageView, bitmapCache, option);
                    return;
                }
            }
            final Bitmap bitmap;
            InputStream stream = new FileInputStream(file);

            if (option.outputWidth == SIZE_AUTO_FIT
                    && option.outputHeight == SIZE_AUTO_FIT) {
                if (imageViewRef.get().getWidth() > 0) {
                    option.outputWidth = imageViewRef.get().getWidth();
                    option.outputHeight = imageViewRef.get().getHeight();
                } else {
                    option.outputWidth = SIZE_ORIGINAL;
                    option.outputHeight = SIZE_ORIGINAL;
                }
            }

            if (option.outputWidth > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                // Decode tạm để lấy thông tin tỉ lệ kích thước ảnh
                BitmapFactory.decodeStream(stream, null, options);
                stream.close();

                int hScale;
                if (option.outputHeight != SIZE_ORIGINAL) {
                    hScale = Math.round((float) options.outHeight
                            / (float) option.outputHeight);
                } else {
                    hScale = 1;
                }
                int wScale;
                if (option.outputHeight != SIZE_ORIGINAL) {
                    wScale = Math.round((float) options.outWidth
                            / (float) option.outputWidth);
                } else {
                    wScale = 1;
                }
                int sampleSize = Math.max(hScale, wScale);
                if (sampleSize > 1) {
                    options.inSampleSize = sampleSize;
                }
                options.inJustDecodeBounds = false;

                stream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(stream, null, options);
            } else if (option.outputHeight > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                // Decode tạm để lấy thông tin tỉ lệ kích thước ảnh
                BitmapFactory.decodeStream(stream, null, options);
                stream.close();

                int hScale = Math.round((float) options.outHeight
                        / (float) option.outputHeight);
                int wScale = Math.round((float) options.outWidth
                        / (float) option.outputWidth);
                int sampleSize = Math.max(hScale, wScale);
                if (sampleSize > 1) {
                    options.inSampleSize = sampleSize;
                }
                options.inJustDecodeBounds = false;

                stream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(stream, null, options);
            } else {
                bitmap = BitmapFactory.decodeStream(stream);
            }
            if (!option.noCache) {
                memoryCache.put(url, bitmap);
            }


            //For the case that we cancel(imageview) during the time we decode image
            ImageView imageView = imageViewRef.get();
            if (checkImageAndUrl(imageView, url)) {
                setImageAsync(imageView, bitmap, option);
            }

        } catch (FileNotFoundException e) {
            Log.e("ImageLoader", file.getAbsolutePath() + " " + e.getMessage());
        } catch (IOException e) {
            Log.e("ImageLoader", file.getAbsolutePath() + " " + e.getMessage());
        }
    }

    private boolean checkImageAndUrl(ImageView imageView, String url) {
        return mImageViews != null && imageView != null && mImageViews.containsKey(imageView) && mImageViews.get(imageView) != null && mImageViews.get(imageView).equals(url);
    }

    /**
     * Decode ảnh, kèm hiệu ứng và đưa vào imageView để hiển thị
     */
    private void decodeImageAndCallback(File file,
                                        final WeakReference<OnImageLoadingFinishListener> callbackRef,
                                        final String url, Option option) {

        try {
            if (!option.noCache) {
                final Bitmap bitmapCache = memoryCache.get(url);
                if (bitmapCache != null) {
                    mAsyntask.execute(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (mReferenceImageCallbacks) {
                                OnImageLoadingFinishListener cb = callbackRef.get();
                                if (cb != null) {
                                    cb.onLoadingFinished(url, bitmapCache);
                                }
                            }
                        }
                    }, AsyncTaskManager.PRIORITY_RUN_ON_MAIN);
                    return;
                }
            }
            final Bitmap bitmap;

            InputStream stream = new FileInputStream(file);

            if (option.outputWidth > 0) {
                // Lấy chiều rộng mong muốn làm chuẩn
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                // Decode tạm để lấy thông tin tỉ lệ kích thước ảnh
                BitmapFactory.decodeStream(stream, null, options);
                stream.close();

                int wScale = (int) Math.floor((float) options.outWidth
                        / (float) option.outputWidth);
                if (wScale > 1) {
                    options.inSampleSize = wScale;
                }
                options.inJustDecodeBounds = false;

                stream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(stream, null, options);

            } else if (option.outputHeight > 0) {
                // Lấy chiều cao mong muốn làm chuẩn
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                // Decode tạm để lấy thông tin tỉ lệ kích thước ảnh
                BitmapFactory.decodeStream(stream, null, options);
                stream.close();

                int hScale = (int) Math.floor((float) options.outHeight
                        / (float) option.outputHeight);
                if (hScale > 1) {
                    options.inSampleSize = hScale;
                }
                options.inJustDecodeBounds = false;

                stream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(stream, null, options);

            } else {
                // Lấy theo kích thước gốc
                bitmap = BitmapFactory.decodeStream(stream);
            }
            if (!option.noCache) {
                memoryCache.put(url, bitmap);
            }
            mAsyntask.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (mReferenceImageCallbacks) {
                        OnImageLoadingFinishListener cb = callbackRef.get();
                        if (cb != null) {
                            cb.onLoadingFinished(url, bitmap);
                        }
                    }
                }
            }, AsyncTaskManager.PRIORITY_RUN_ON_MAIN);
        } catch (FileNotFoundException e) {
            Log.e("ImageLoader", file.getAbsolutePath() + " " + e.getMessage());
        } catch (IOException e) {
            Log.e("ImageLoader", file.getAbsolutePath() + " " + e.getMessage());
        }
    }

    @SuppressWarnings("resource")
    private void downloadImage(String urlStr, File targetFile) {
        InputStream byteStream = null;
        FileOutputStream fos = null;

        try {

            HttpURLConnection httpConn = (HttpURLConnection) new URL(urlStr)
                    .openConnection();

            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                String newUrl = httpConn.getHeaderField("Location");
                httpConn = (HttpURLConnection) new URL(newUrl).openConnection();
            }

            byteStream = httpConn.getInputStream();
            int contentSize = httpConn.getContentLength();

            if (-1 == contentSize) {
                byte[] tempBuffer = new byte[READ_SIZE];
                int bufferLeft = tempBuffer.length;
                int bufferOffset = 0;
                int readResult = 0;

                outer:
                do {
                    while (bufferLeft > 0) {
                        readResult = byteStream.read(tempBuffer, bufferOffset,
                                bufferLeft);
                        if (readResult < 0) {
                            break outer;
                        }
                        bufferOffset += readResult;
                        bufferLeft -= readResult;
                    }

                    bufferLeft = READ_SIZE;
                    int newSize = tempBuffer.length + READ_SIZE;

                    byte[] expandedBuffer = new byte[newSize];
                    System.arraycopy(tempBuffer, 0, expandedBuffer, 0,
                            tempBuffer.length);
                    tempBuffer = expandedBuffer;
                } while (true);

                // Ghi file
                fos = new FileOutputStream(targetFile);
                fos.write(tempBuffer, 0, bufferOffset);
                fos.flush();
                fos.close();
            } else {
                byte[] byteBuffer = new byte[contentSize];
                int remainingLength = contentSize;
                int bufferOffset = 0;

                while (remainingLength > 0) {
                    int readResult = byteStream.read(byteBuffer, bufferOffset,
                            remainingLength);
                    if (readResult < 0) {
                        throw new EOFException();
                    }
                    bufferOffset += readResult;
                    remainingLength -= readResult;
                }

                fos = new FileOutputStream(targetFile);
                fos.write(byteBuffer, 0, contentSize);
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            Log.e("ImageLoader",
                    "error loading file from url " + e.getMessage());
        } finally {
            if (null != byteStream) {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    byteStream.close();
                } catch (Exception e) {
                    Log.e("ImageLoader",
                            "error closing byte stream " + e.getMessage());
                }
            }
        }
    }

    /**
     * Load ảnh xong thì gọi callback chứ ko gắn vào ImageView nào cả
     */
    public void loadImage(final Option option,
                          final OnImageLoadingFinishListener callback) {


        if (!TextUtils.isEmpty(option.path)) {
            Bitmap bitmap = null;
            if (!option.noCache) {
                bitmap = memoryCache.get(option.path);
            }
            if (bitmap == null) {
                mAsyntask.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bm = ImageUtils.decodeBitmapFromImagePath(option.path, option.outputWidth, option.outputHeight);
                        if (bm != null && bm.getWidth() > 1 && bm.getHeight() > 1) {
                            mAsyntask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onLoadingFinished(option.path, bm);
                                    if (!option.noCache) {
                                        memoryCache.put(option.path, bm);
                                    }
                                }
                            }, AsyncTaskManager.PRIORITY_RUN_ON_MAIN);

                        }
                    }
                });
            } else {
                if (bitmap.getWidth() > 1 && bitmap.getHeight() > 1) {
                    callback.onLoadingFinished(option.path, bitmap);
                    if (!option.noCache) {
                        memoryCache.put(option.path, bitmap);
                    }
                }
            }
        } else {

            if (!checkUrl(option.url)) {
                return;
            }
            if (!option.noCache) {
                Bitmap bitmap = memoryCache.get(option.url);
                if (bitmap != null) {
                    if (callback != null) {
                        callback.onLoadingFinished(option.url, bitmap);
                    }
                    return;
                }
            }
            synchronized (mReferenceImageCallbacks) {
                // Đưa callback vào danh sách liên quan với url này
                ArrayList<ImageCallback> imageViews = mReferenceImageCallbacks
                        .get(option.url);
                if (imageViews == null) {
                    imageViews = new ArrayList<ImageCallback>();
                }
                imageViews.add(new ImageCallback(null, option, callback));
                mReferenceImageCallbacks.put(option.url, imageViews);

                if (!mProcessingURLs.contains(option.url)) {
                    if (mWaitingDownloadURLs.contains(option.url)) {
                        mWaitingDownloadURLs.remove(option.url);
                        mWaitingDownloadURLs.add(0, option.url);
                    } else if (mWaitingDecodeURLs.contains(option.url)) {
                        if (mWaitingDecodeURLs.size() > 30) {
                            // Nhiều quá, nguy hiểm, reset lại
                            mDecodeTaskQueue = new LinkedBlockingQueue<Runnable>();
                            mDecodeThreadPool = new ThreadPoolExecutor(
                                    CORE_DECODE_POOL_SIZE, CORE_DECODE_POOL_SIZE,
                                    KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                                    mDecodeTaskQueue);
                            mDecodeThreadPool.allowCoreThreadTimeOut(true);

                            mWaitingDecodeURLs.remove(option.url);
                            mProcessingURLs.clear();
                            mProcessingURLs.add(option.url);
                            DecodeRunnable decodeRunnable = new DecodeRunnable(option.url);
                            mDecodeThreadPool.execute(decodeRunnable);
                        } else {
                            mWaitingDecodeURLs.remove(option.url);
                            mWaitingDecodeURLs.add(0, option.url);
                        }
                    } else {
                        // Nên ktra file cache trước
                        File file = FileCacheUtils.getCacheFileByUrl(
                                FileCacheUtils.CACHE_TYPE_IMAGE, option.url);

                        if (!file.exists()) {
                            if (mWaitingDownloadURLs.isEmpty()
                                    && mDownloadTaskQueue.isEmpty()) {
                                mProcessingURLs.add(option.url);
                                DownloadRunnable downloadRunnable = new DownloadRunnable(
                                        option.url);
                                mDownloadThreadPool.execute(downloadRunnable);
                            } else {
                                mWaitingDownloadURLs.add(0, option.url);
                            }
                        } else {
                            if (mWaitingDecodeURLs.isEmpty()
                                    && mDecodeTaskQueue.isEmpty()) {
                                mProcessingURLs.add(option.url);
                                DecodeRunnable decodeRunnable = new DecodeRunnable(
                                        option.url);
                                mDecodeThreadPool.execute(decodeRunnable);
                            } else {
                                mWaitingDecodeURLs.add(0, option.url);
                            }
                        }
                    }
                }
            } // end synchronized (mReferenceImageCallbacks)
        }
    }

    public void loadImage(final ImageView imageView, final Option option) {

        if (imageView == null) {
            return;
        }

        if (!TextUtils.isEmpty(option.path)) {
            Bitmap bitmap = null;
            if (!option.noCache) {
                bitmap = memoryCache.get(option.path);
            }
            if (bitmap == null) {
                if (option.defaultImageId > 0) {
                    if (option.defaultImageId != ANIMATED) {
                        imageView.setImageResource(option.defaultImageId);
                    } else {
                        imageView.setImageDrawable(null);
                    }
                }
                mAsyntask.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bm = ImageUtils.decodeBitmapFromImagePath(option.path, option.outputWidth, option.outputHeight);
                        if (bm != null && bm.getWidth() > 1 && bm.getHeight() > 1) {
                            mAsyntask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    setImageSync(imageView, bm, option);
                                    if (!option.noCache) {
                                        memoryCache.put(option.path, bm);
                                    }
                                }
                            }, AsyncTaskManager.PRIORITY_RUN_ON_MAIN);

                        }
                    }
                });
            } else {
                if (bitmap.getWidth() > 1 && bitmap.getHeight() > 1) {
                    setImageSync(imageView, bitmap, option);
                    if (!option.noCache) {
                        memoryCache.put(option.path, bitmap);
                    }
                }
            }
        } else if (checkUrl(option.url)) {

            if (checkUrl(option.fullImage)) {
                Bitmap bitmap = memoryCache.get(option.fullImage);

                if (bitmap != null) {
                    setImageSync(imageView, bitmap, option);
                    return;
                }
            }

            Bitmap bitmap = null;
            if (!option.noCache) {
                bitmap = memoryCache.get(option.url);
            }

            if (bitmap != null) {
                if (option.defaultImageId == ANIMATED) {
                    setImageAsync(imageView, bitmap, option);
                } else {
                    if (option.rotate) {
                        bitmap = rotateBitmap(bitmap);
                    }
                    setImageSync(imageView, bitmap, option);
                }
                if (!TextUtils.isEmpty(option.fullImage)) {
                    option.url = option.fullImage;
                    option.fullImage = null;
                    loadImage(imageView, option);
                }
                return;
            }
            if (option.busy) {
                if (option.defaultImageId > 0) {
                    if (option.defaultImageId != ANIMATED) {
                        imageView.setImageResource(option.defaultImageId);
                    } else {
                        imageView.setImageDrawable(null);
                    }
                }
                Option opt = new Option();
                opt.url = option.url;
                opt.outputWidth = option.outputWidth;
                opt.outputHeight = option.outputHeight;
                loadImage(opt, null);
                return;
            }
            synchronized (mReferenceImageCallbacks) {
            /*
             * Thêm imageView này vào danh sách mapping với url này
			 */
                String prevUrl = mImageViews.put(imageView, option.url);
                // Xử lý các mapping giữa url và imageview
                if (!option.url.equals(prevUrl)) {
                    if (prevUrl != null) {
                        // Remove cái liên kết giữa url cũ và imageview này
                        ArrayList<ImageCallback> imageViews = mReferenceImageCallbacks
                                .get(prevUrl);
                        if (imageViews != null && imageViews.size() > 0) {
                            for (int i = imageViews.size() - 1; i >= 0; i--) {
                                ImageCallback icb = imageViews.get(i);
                                if (icb.imageView.get() == null) {
                                    imageViews.remove(i);
                                } else if (icb.imageView.get() == imageView) {
                                    imageViews.remove(i);
                                    break;
                                }
                            }
                        }
                    }

                    // Đưa imageview vào danh sách liên quan với url này
                    ArrayList<ImageCallback> imageViews = mReferenceImageCallbacks
                            .get(option.url);
                    if (imageViews == null) {
                        imageViews = new ArrayList<ImageCallback>();
                    }
                    imageViews.add(new ImageCallback(imageView, option, null));
                    mReferenceImageCallbacks.put(option.url, imageViews);
                }

                if (mProcessingURLs.contains(option.url)) {
                    if (!mWaitingDecodeURLs.contains(option.url)
                            && !mWaitingDownloadURLs.contains(option.url)) {
                        while (mProcessingURLs.contains(option.url)) {
                            mProcessingURLs.remove(option.url);
                        }
                    }
                }

                if (!mProcessingURLs.contains(option.url)) {
                    if (mWaitingDownloadURLs.contains(option.url)) {
                        mWaitingDownloadURLs.remove(option.url);
                        mWaitingDownloadURLs.add(0, option.url);
                    } else if (mWaitingDecodeURLs.contains(option.url)) {
                        if (mWaitingDecodeURLs.size() > 30) {
                            // Nhiều quá, nguy hiểm, reset lại
                            mDecodeTaskQueue = new LinkedBlockingQueue<Runnable>();
                            mDecodeThreadPool = new ThreadPoolExecutor(
                                    CORE_DECODE_POOL_SIZE, CORE_DECODE_POOL_SIZE,
                                    KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                                    mDecodeTaskQueue);
                            mDecodeThreadPool.allowCoreThreadTimeOut(true);

                            mWaitingDecodeURLs.remove(option.url);
                            mProcessingURLs.clear();
                            mProcessingURLs.add(option.url);
                            DecodeRunnable decodeRunnable = new DecodeRunnable(
                                    option.url);
                            mDecodeThreadPool.execute(decodeRunnable);
                        } else {
                            mWaitingDecodeURLs.remove(option.url);
                            mWaitingDecodeURLs.add(0, option.url);
                        }
                    } else {
                        // Nên ktra file cache trước
                        File file = FileCacheUtils.getCacheFileByUrl(
                                FileCacheUtils.CACHE_TYPE_IMAGE, option.url);
                        if (!file.exists()) {
                            if (mWaitingDownloadURLs.isEmpty()
                                    && mDownloadTaskQueue.isEmpty()) {
                                mProcessingURLs.add(option.url);
                                DownloadRunnable downloadRunnable = new DownloadRunnable(
                                        option.url);
                                mDownloadThreadPool.execute(downloadRunnable);
                            } else {
                                mWaitingDownloadURLs.add(0, option.url);
                            }
                        } else {
                            if (mWaitingDecodeURLs.isEmpty()
                                    && mDecodeTaskQueue.isEmpty()) {
                                mProcessingURLs.add(option.url);
                                DecodeRunnable decodeRunnable = new DecodeRunnable(
                                        option.url);
                                mDecodeThreadPool.execute(decodeRunnable);
                            } else {
                                mWaitingDecodeURLs.add(0, option.url);
                            }
                        }
                    }
                }
            } // end synchronized (mReferenceImageCallbacks)

            if (option.defaultImageId > 0) {
                if (option.defaultImageId != ANIMATED) {
                    imageView.setImageResource(option.defaultImageId);
                } else {
                    imageView.setImageDrawable(null);
                }
            }
        }
    }


    /**
     * Hoãn việc load ảnh với url này
     */
    public void cancel(String url) {
        synchronized (mWaitingDownloadURLs) {
            mWaitingDownloadURLs.remove(url);
        }

        synchronized (mReferenceImageCallbacks) {
            mReferenceImageCallbacks.remove(url);
        }
    }

    /**
     * Hoãn việc load ảnh vào imageView này
     */
    public void cancel(ImageView imageView) {
        String url;
        synchronized (mReferenceImageCallbacks) {
            url = mImageViews.remove(imageView);
            if (url != null) {
                ArrayList<ImageCallback> imageCallbacks = mReferenceImageCallbacks
                        .get(url);
                if (imageCallbacks != null && !imageCallbacks.isEmpty()) {
                    for (int i = imageCallbacks.size() - 1; i >= 0; i--) {
                        ImageCallback icb = imageCallbacks.get(i);
                        if (icb.imageView.get() == imageView) {
                            imageCallbacks.remove(i);
                        }
                    }

                    if (imageCallbacks.isEmpty()) {
                        cancel(url);
                    }
                }
            }
        }
    }

    private boolean checkUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return true;
    }

    private Bitmap rotateBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > height) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                    true);
        }
        return bitmap;
    }

    private void setImageAsync(final ImageView imageView, Bitmap bitmap,
                               final Option option) {
        if (imageView == null || bitmap == null) {
            return;
        }
        if (option.rotate) {
            bitmap = rotateBitmap(bitmap);
        }

        if (option.changeImageSize && imageView.getWidth() > 0) {
            imageView.getLayoutParams().height = imageView.getWidth() * bitmap.getHeight() / bitmap.getWidth();
        }

        Drawable drawable1 = imageView.getDrawable();
        if (drawable1 instanceof TransitionDrawable) {
            drawable1 = ((TransitionDrawable) drawable1).getDrawable(1);
        }
        if (drawable1 == null) {
            drawable1 = new ColorDrawable(Color.TRANSPARENT);
        }
        Drawable drawable2;
        if (option.roundedConer) {
            int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
            bitmap = Bitmap.createBitmap(bitmap,
                    (bitmap.getWidth() - size) / 2,
                    (bitmap.getHeight() - size) / 2, size, size);
            drawable2 = RoundedBitmapDrawableFactory.create(mResource,
                    bitmap);
            if (option.cornerRadius == -1) {
                ((RoundedBitmapDrawable) drawable2).setCornerRadius(Math.min(
                        drawable2.getMinimumWidth(),
                        drawable2.getMinimumHeight()));
            } else {
                ((RoundedBitmapDrawable) drawable2).setCornerRadius(option.cornerRadius);
            }
        } else {
            drawable2 = new BitmapDrawable(mResource, bitmap);
        }
        final TransitionDrawable td = new TransitionDrawable(
                new Drawable[]{drawable1, drawable2});
        mAsyntask.execute(new Runnable() {
            @Override
            public void run() {
                imageView.setImageDrawable(td);
                td.setCrossFadeEnabled(true);
                td.startTransition(200);
            }
        }, AsyncTaskManager.PRIORITY_RUN_ON_MAIN_FRONT);
    }

    public void setImageSync(ImageView imageView, Bitmap bitmap, Option option) {
        if (option.roundedConer) {
            int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
            bitmap = Bitmap.createBitmap(bitmap,
                    (bitmap.getWidth() - size) / 2,
                    (bitmap.getHeight() - size) / 2, size, size);
            RoundedBitmapDrawable drawable2 = RoundedBitmapDrawableFactory.create(mResource,
                    bitmap);
            if (option.cornerRadius == -1) {
                drawable2.setCornerRadius(Math.min(
                        drawable2.getMinimumWidth(),
                        drawable2.getMinimumHeight()));
            } else {
                drawable2.setCornerRadius(option.cornerRadius);
            }
            imageView.setImageDrawable(drawable2);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Với mỗi ảnh hiện thị trong ứng dụng này còn kèm theo những effect khác
     * nhau nên ở đây tạo một lớp wrapper để kèm thêm những thông tin về các
     * effect tương ứng với một ảnh cần hiển thị lên imageview
     *
     * @author <Tran Vu Tat Binh>
     */
    static class ImageCallback {
        final WeakReference<ImageView> imageView;
        final WeakReference<OnImageLoadingFinishListener> callback;
        final Option option;

        public ImageCallback(ImageView imageView, Option option,
                             OnImageLoadingFinishListener callback) {
            this.imageView = new WeakReference<>(imageView);
            this.callback = new WeakReference<>(
                    callback);
            this.option = option;
        }

    }

    public interface OnImageLoadingFinishListener {
        void onLoadingFinished(String url, Bitmap resultBitmap);
    }

    public void release() {
        memoryCache.clear();
    }

    public static class Option {

        public int outputWidth = -1;
        public int outputHeight = -1;
        public int defaultImageId = -1;
        /**
         * 90 degree rotate
         */
        public boolean rotate = false;
        /**
         * for list view
         */
        public boolean busy = false;
        /**
         * dont cache this image
         */
        public boolean noCache = false;

        /**
         * change image size
         */
        public boolean changeImageSize = false;
        /**
         * use when load image with 2 url (thumbnail and original)
         */
        public String fullImage = "";
        /**
         * round image
         */
        public boolean roundedConer = false;


        public int cornerRadius = -1;
        /**
         * http url
         */
        public String url;
        /**
         * path to local file
         */
        public String path;

        public Option() {

        }

        public Option(String url) {
            this.url = url;
        }
    }

    public static class MemoryCache {

        private static final String TAG = "MemoryCache";
        // Last argument true for LRU ordering
        private Map<String, Bitmap> cache = Collections
                .synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));
        // current allocated size
        private long size = 0;
        private long limit = 1000000;// max memory in bytes
        private static final Object mLock = new Object();

        public MemoryCache() {
            // use 25% of available heap size
            setLimit(Runtime.getRuntime().maxMemory() / 6);
        }

        public void setLimit(long new_limit) {
            limit = new_limit;
            Log.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
        }

        public Bitmap get(String id) {
            try {
                synchronized (mLock) {
                    if (!cache.containsKey(id))
                        return null;
                    // NullPointerException sometimes happen here
                    // http://code.google.com/p/osmdroid/issues/detail?id=78
                    return cache.get(id);
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        public void put(String id, Bitmap bitmap) {
            try {
                synchronized (mLock) {
                    if (cache.containsKey(id))
                        size -= getSizeInBytes(cache.get(id));
                    cache.put(id, bitmap);
                    size += getSizeInBytes(bitmap);
                    checkSize();
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }

        private void checkSize() {
            if (size > limit) {
                // least recently accessed item will be the first one iterated
                Iterator<Map.Entry<String, Bitmap>> iter = cache.entrySet().iterator();

                while (iter.hasNext()) {
                    Map.Entry<String, Bitmap> entry = iter.next();
                    size -= getSizeInBytes(entry.getValue());
                    iter.remove();
                    if (size <= limit)
                        break;
                }
            }
        }

        public void clear() {
            try {
                // NullPointerException sometimes happen here
                // http://code.google.com/p/osmdroid/issues/detail?id=78
                cache.clear();
                size = 0;
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }

        long getSizeInBytes(Bitmap bitmap) {
            if (bitmap == null)
                return 0;
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    public static class FileCacheUtils {
        /*
         * Các loại cache hỗ trợ
         */
        public static final int CACHE_TYPE_OTHERS = 0;
        public static final int CACHE_TYPE_IMAGE = 1;
        public static final int CACHE_TYPE_VIDEO = 2;

        /*
         * Dung lượng giới hạn cho thư mục cache từng loại
         */
        private static final long CACHE_FOLDER_SIZE_LIMIT_IMAGES = 1024 * 1024 * 500; // 500MB
        private static final long CACHE_FOLDER_SIZE_LIMIT_VIDEOS = 1024 * 1024 * 500; // 500MB
        private static final long CACHE_FOLDER_SIZE_LIMIT_OTHERS = 1024 * 1024 * 2000; // 2GB

        /*
         * Dung lượng định mức sẽ giảm xuống khi cần xóa bớt cache
         */
        private static final long CACHE_FOLDER_SIZE_STANDARD_IMAGES = 1024 * 1024 * 350; // 350MB
        private static final long CACHE_FOLDER_SIZE_STANDARD_VIDEOS = 1024 * 1024 * 350; // 350MB
        private static final long CACHE_FOLDER_SIZE_STANDARD_OTHERS = 1024 * 1024 * 1500; // 1.5GB

        /*
         * Thời gian dự kiến để ktra cache lần kế tiếp
         */
        private static long mNextTimeCheckImage = 0;
        private static long mNextTimeCheckVideo = 0;
        private static long mNextTimeCheckOthers = 0;

        /**
         * Đường dẫn mặc định cho thư mục cache
         */
        private static String mCachePath = Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/.eplatform/cache";

        public static void setCachePath(String path) {
            mCachePath = path;
        }

        /*
         * sub folder cho từng loại cache
         */
        private static final String SUB_IMAGES = "/images";
        private static final String SUB_VIDEOS = "/videos";
        private static final String SUB_OTHERS = "/others";

        /**
         * Thời gian tối thiếu giữa 2 lần kiểm tra dung lượng cache
         */
        private static final long MIN_NEXT_CHECK_INTERVAL = 30000;

        /**
         * Chuyển từ url của file sang tên file trên hệ thống cache hiện tại
         */
        public static String getFilenameForUrl(String url) {
            return String.valueOf(url.hashCode());
        }

        /**
         * Lấy file cache dựa trên loại file và tên file
         */
        public static File getCacheFile(int cacheType, String name) {
            StringBuilder path = new StringBuilder(mCachePath);
            switch (cacheType) {
                case CACHE_TYPE_IMAGE:
                    path.append(SUB_IMAGES);
                    break;

                case CACHE_TYPE_VIDEO:
                    path.append(SUB_VIDEOS);
                    break;

                default:
                    path.append(SUB_OTHERS);
            }

            File folder = new File(path.toString());
            if (!folder.exists()) {
                folder.mkdirs();
            }
            path.append("/").append(name);

            return new File(path.toString());
        }

        /**
         * Lấy file cache dựa trên loại file và url của file cần tải về
         */
        public static File getCacheFileByUrl(int cacheType, String url) {
            return getCacheFile(cacheType, Integer.toString(url.hashCode()));
        }

        /**
         * Kiểm tra dung lượng cache và xóa bớt nếu vượt quá giới hạn
         */
        public static void validateCache(int cacheType) {
            // Ktra xem đã cần phải ktra cache hay chưa
            File folder;
            long size;
            long next;

            switch (cacheType) {
                case CACHE_TYPE_IMAGE:
                    if (System.currentTimeMillis() > mNextTimeCheckImage) {
                        folder = new File(mCachePath + SUB_IMAGES);
                        size = calculateFolderSize(folder);
                        if (size > CACHE_FOLDER_SIZE_LIMIT_IMAGES) {
                            size = resizeFolder(folder, size,
                                    CACHE_FOLDER_SIZE_STANDARD_IMAGES);
                        }
                        next = Math.max((CACHE_FOLDER_SIZE_LIMIT_IMAGES - size) / 60,
                                MIN_NEXT_CHECK_INTERVAL);
                        mNextTimeCheckImage = System.currentTimeMillis() + next;
                    }
                    break;

                case CACHE_TYPE_VIDEO:
                    if (System.currentTimeMillis() > mNextTimeCheckVideo) {
                        folder = new File(mCachePath + SUB_VIDEOS);
                        size = calculateFolderSize(folder);
                        if (size > CACHE_FOLDER_SIZE_LIMIT_VIDEOS) {
                            size = resizeFolder(folder, size,
                                    CACHE_FOLDER_SIZE_STANDARD_VIDEOS);
                        }
                        next = Math.max((CACHE_FOLDER_SIZE_LIMIT_VIDEOS - size) / 60,
                                MIN_NEXT_CHECK_INTERVAL);
                        mNextTimeCheckVideo = System.currentTimeMillis() + next;
                    }
                    break;

                default:
                    if (System.currentTimeMillis() > mNextTimeCheckOthers) {
                        folder = new File(mCachePath + SUB_OTHERS);
                        size = calculateFolderSize(folder);
                        if (size > CACHE_FOLDER_SIZE_LIMIT_OTHERS) {
                            size = resizeFolder(folder, size,
                                    CACHE_FOLDER_SIZE_STANDARD_OTHERS);
                        }
                        next = Math.max((CACHE_FOLDER_SIZE_LIMIT_OTHERS - size) / 60,
                                MIN_NEXT_CHECK_INTERVAL);
                        mNextTimeCheckOthers = System.currentTimeMillis() + next;
                    }
            }

        }

        /**
         * Xóa các file trong thư mục này đến khi còn mức dung lượng giới hạn hoặc
         * ko còn file nào có thể xóa
         */
        private static long resizeFolder(File folder, long currentSize,
                                         long targetSize) {
            if (currentSize > targetSize) {
                File[] files = folder.listFiles();
                Arrays.sort(files, new FileDateModifiedComparator());
                for (File file : files) {
                    long size = file.length();
                    if (file.delete()) {
                        currentSize -= size;
                        if (currentSize <= targetSize) {
                            break;
                        }
                    }
                }
            }

            return currentSize;
        }

        /**
         * So sánh 2 file dựa trên ngày cuối cùng sửa file
         */
        static class FileDateModifiedComparator implements Comparator<File> {

            @Override
            public int compare(File lhs, File rhs) {
                if ((lhs.lastModified() - rhs.lastModified()) > 0) {
                    return 1;
                } else if ((lhs.lastModified() - rhs.lastModified()) < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }

        }

        /**
         * Tính toán dung lượng của một folder
         */
        private static long calculateFolderSize(File folder) {
            long size = 0;
            if (folder.exists()) {
                // Ktra lại dung lượng thư mục cache
                long start = System.currentTimeMillis();
                File[] files = folder.listFiles();
                for (File child : files) {
                    size += child.length();
                }
                Log.i("filecache", "cache folder size = " + size
                        + " *** time calculate = "
                        + (System.currentTimeMillis() - start));
            }

            return size;
        }

        public static void clearCache(int type) {
            File folder = null;
            switch (type) {
                case CACHE_TYPE_IMAGE:
                    folder = new File(mCachePath + SUB_IMAGES);
                    break;

                case CACHE_TYPE_VIDEO:
                    folder = new File(mCachePath + SUB_VIDEOS);
                    break;

                case CACHE_TYPE_OTHERS:
                    folder = new File(mCachePath + SUB_VIDEOS);
                    break;
            }

            if (folder != null && folder.exists()) {
                File[] files = folder.listFiles();
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    public static class ImageUtils {

        private static final String LOG_TAG = "ImageUtils";

        public static Bitmap decodeBitmapFromImagePath(String imagePath,
                                                       int reqWidth, int reqHeight) {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            Bitmap sourceBitmap = BitmapFactory.decodeFile(imagePath, options);

            if (sourceBitmap != null) {
                int rotationRadius = getRotationRadius(imagePath);
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationRadius);
                sourceBitmap = Bitmap
                        .createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(),
                                sourceBitmap.getHeight(), matrix, true);
            }
            return sourceBitmap;
        }

        private static int calculateInSampleSize(BitmapFactory.Options options,
                                                 int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            if (reqWidth < 0) {
                reqWidth = width;
            }
            if (reqHeight < 0) {
                reqHeight = height;
            }
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height;
                final int halfWidth = width;

                // Calculate the largest inSampleSize value that is a power of 2 and
                // keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        // check orientation and return radius of rotation
        public static int getRotationRadius(String imagePath) {
            try {
                ExifInterface exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 1);
                switch (orientation) {
                    case 3:
                        return 180;

                    case 6:
                        return 90;

                    case 8:
                        return -90;

                    default:
                        return 0;
                }

            } catch (IOException e) {
                Log.w(LOG_TAG, "getRotationRadius error: " + e.getMessage());
            }

            return 0;
        }


    }

}
