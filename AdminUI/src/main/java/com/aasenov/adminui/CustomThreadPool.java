package com.aasenov.adminui;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bounded thread pool used to execute tasks, reusing existing threads.
 */
public class CustomThreadPool extends ThreadPoolExecutor {

    /**
     * Lock to use to protect mQueueFullCond condition.
     */
    private final ReentrantLock mQueueLock;

    /**
     * Condition to signal when task get executed.
     */
    private final Condition mQueueFullCond;

    public CustomThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ReentrantLock queueLock, Condition queueFullCond) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

        mQueueLock = queueLock;
        mQueueFullCond = queueFullCond;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        mQueueLock.lock();
        try {
            mQueueFullCond.signal();
        } finally {
            mQueueLock.unlock();
        }
    }
}