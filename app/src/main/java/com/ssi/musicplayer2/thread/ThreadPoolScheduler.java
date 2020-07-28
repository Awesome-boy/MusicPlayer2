package com.ssi.musicplayer2.thread;


import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class ThreadPoolScheduler {
    private static final String TAG = ThreadPoolScheduler.class.getSimpleName();


    private static ThreadPoolScheduler mThreadPoolScheduler;

    public synchronized static ThreadPoolScheduler getInstance() {
        if(mThreadPoolScheduler == null) {
            mThreadPoolScheduler = new ThreadPoolScheduler();
        }
        return mThreadPoolScheduler;
    }





    private final ExecutorService mExecutors;
    private final LocalThreadFactory mLocalThreadFactory;

    private ThreadPoolScheduler() {
        mLocalThreadFactory = new LocalThreadFactory();
        mExecutors = Executors.newFixedThreadPool(10, mLocalThreadFactory);
    }


    public void stop() {
        if(mThreadPoolScheduler != null) {
            mExecutors.shutdownNow();
            mThreadPoolScheduler = null;
        }




    }

    public int getActiveCount() {
        return  0;
    }


    public int getQueueSize() {
        return  0;
    }

    public boolean isTerminated() {
        return mExecutors == null || mExecutors.isTerminated();
    }


    public void post(@NonNull Runnable runnable) {
        if(mExecutors != null) {
            mExecutors.execute(runnable);
        }
    }


    public synchronized ExecutorService getExecutorService() {
        return mExecutors;
    }

    private static class LocalThreadFactory implements ThreadFactory {
        private final AtomicInteger mExecutingCount;
        private final ThreadGroup group;

        public LocalThreadFactory() {
            mExecutingCount = new AtomicInteger(0);
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();

        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            final Thread thread = new Thread(group, r);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY){
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            return thread;
        }
    }



    public synchronized int getCount() {
        return 0;
    }

}

