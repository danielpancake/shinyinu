package net.danielpancake.shinyinu;

import android.graphics.Bitmap;
import android.util.LruCache;

public class MemoryCache {

    private LruCache<String, Bitmap> memoryCache;

    MemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    void addBitmapToMemoryCache(String key, Bitmap bitmap, boolean replace) {
        if (memoryCache.get(key) == null || replace) {
            memoryCache.put(key, bitmap);
        }
    }

    Bitmap getBitmapFromMemoryCache(String key) {
        return memoryCache.get(key);
    }

    void removeBitmapFromMemoryCache(String key) {
        if (memoryCache.get(key) != null) {
            memoryCache.remove(key);
        }
    }

    void removeAllFromMemoryCache() {
        memoryCache.evictAll();
    }
}