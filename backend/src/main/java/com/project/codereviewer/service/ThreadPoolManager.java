package com.project.codereviewer.service;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Centralized thread pool management for the application.
 * Provides optimized thread pools for different types of operations.
 */
public class ThreadPoolManager {
    private static final Logger logger = Logger.getLogger(ThreadPoolManager.class.getName());
    
    private static volatile ThreadPoolManager instance;
    
    // Different thread pools for different types of operations
    private final ExecutorService analysisExecutor;
    private final ExecutorService aiRequestExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    
    private ThreadPoolManager() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        
        // CPU-intensive tasks (code analysis)
        this.analysisExecutor = Executors.newFixedThreadPool(
            Math.max(2, coreCount - 1), 
            createThreadFactory("Analysis")
        );
        
        // IO-bound tasks (AI API calls)
        this.aiRequestExecutor = Executors.newFixedThreadPool(
            Math.min(10, coreCount * 2), 
            createThreadFactory("AI-Request")
        );
        
        // Scheduled tasks
        this.scheduledExecutor = Executors.newScheduledThreadPool(
            2, 
            createThreadFactory("Scheduled")
        );
        
        logger.info("ThreadPoolManager initialized with " + coreCount + " available processors");
    }
    
    public static ThreadPoolManager getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolManager.class) {
                if (instance == null) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }
    
    private ThreadFactory createThreadFactory(String namePrefix) {
        return new ThreadFactory() {
            private int threadNumber = 1;
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, namePrefix + "-" + threadNumber++);
                t.setDaemon(true);
                return t;
            }
        };
    }
    
    public ExecutorService getAnalysisExecutor() {
        return analysisExecutor;
    }
    
    public ExecutorService getAiRequestExecutor() {
        return aiRequestExecutor;
    }
    
    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }
    
    public void shutdown() {
        logger.info("Shutting down thread pools...");
        analysisExecutor.shutdown();
        aiRequestExecutor.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            if (!analysisExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                analysisExecutor.shutdownNow();
            }
            if (!aiRequestExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                aiRequestExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Thread pool shutdown interrupted");
        }
    }
}
