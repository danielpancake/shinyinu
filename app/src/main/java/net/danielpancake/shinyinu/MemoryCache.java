package net.danielpancake.shinyinu;

import android.graphics.Bitmap;
import android.util.LruCache;

public class MemoryCache {

    private LruCache<String, Bitmap> memoryCache;

    public MemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap, boolean replace) {
        if (memoryCache.get(key) == null || replace) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return memoryCache.get(key);
    }

    public void removeBitmapFromMemoryCache(String key) {
        if (memoryCache.get(key) != null) {
            memoryCache.remove(key);
        }
    }
}
