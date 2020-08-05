package cn.houlang.rvds.nat.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import cn.houlang.rvds.nat.volley.source.Request;
import cn.houlang.rvds.nat.volley.source.RequestQueue;
import cn.houlang.rvds.nat.volley.source.toolbox.ImageLoader;
import cn.houlang.rvds.nat.volley.source.toolbox.Volley;

/**
 * @author #Suyghur.
 * Created on 2020/7/30
 */
public class VolleySingleton {
    private volatile static VolleySingleton mInstance;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private static Context ctx;

    private VolleySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            synchronized (VolleySingleton.class) {
                if (mInstance == null) {
                    mInstance = new VolleySingleton(context);
                }
            }
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
