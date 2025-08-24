package com.project.codereviewer.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Simple LRU-style cache with time-based expiration for code analysis results.
 * Thread-safe implementation using ConcurrentHashMap.
 */
public class AnalysisCache<K, V> {
    private static final Logger logger = Logger.getLogger(AnalysisCache.class.getName());
    
    private final ConcurrentHashMap<K, CacheEntry<V>> cache;
    private final long maxAgeMs;
    private final int maxSize;
    private final ScheduledExecutorService cleanupExecutor;
    
    private static class CacheEntry<V> {
        final V value;
        final long timestamp;
        volatile long lastAccessed;
        
        CacheEntry(V value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
            this.lastAccessed = timestamp;
        }
    }
    
    public AnalysisCache(int maxSize, long maxAgeMs) {
        this.cache = new ConcurrentHashMap<>(maxSize);
        this.maxSize = maxSize;
        this.maxAgeMs = maxAgeMs;
        this.cleanupExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "Cache-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        // Schedule periodic cleanup
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 60, 60, TimeUnit.SECONDS);
        logger.info("AnalysisCache initialized with maxSize=" + maxSize + ", maxAge=" + maxAgeMs + "ms");
    }
    
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        
        // Check if expired
        if (System.currentTimeMillis() - entry.timestamp > maxAgeMs) {
            cache.remove(key);
            return null;
        }
        
        entry.lastAccessed = System.currentTimeMillis();
        return entry.value;
    }
    
    public void put(K key, V value) {
        // Simple size-based eviction
        if (cache.size() >= maxSize) {
            evictOldest();
        }
        
        cache.put(key, new CacheEntry<>(value));
    }
    
    private void evictOldest() {
        K oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (var entry : cache.entrySet()) {
            if (entry.getValue().lastAccessed < oldestTime) {
                oldestTime = entry.getValue().lastAccessed;
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        int removed = 0;
        
        var iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (now - entry.getValue().timestamp > maxAgeMs) {
                iterator.remove();
                removed++;
            }
        }
        
        if (removed > 0) {
            logger.fine("Cache cleanup removed " + removed + " expired entries");
        }
    }
    
    public void clear() {
        cache.clear();
    }
    
    public int size() {
        return cache.size();
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
