package vn.tiki.tiniapp;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TNThreadPool {

    private static class TNThreadFactory implements ThreadFactory {
        private final static String NAME_PREFIX = "tini-thread-pool-";
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        TNThreadFactory() {
            SecurityManager securityManager = System.getSecurityManager();
            group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        /**
         * Constructs a new {@code Thread}.  Implementations may also initialize
         * priority, name, daemon status, {@code ThreadGroup}, etc.
         *
         * @param r A runnable to be executed by new thread instance
         * @return Constructed thread, or {@code null} if the request to
         * create a thread is rejected
         */
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(this.group, r, NAME_PREFIX + this.threadNumber.getAndIncrement(), 0L);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }

            if (thread.getPriority() != 5) {
                thread.setPriority(5);
            }

            return thread;
        }
    }

    private final String TAG = "Tini.ThreadPool";

    /**
     * ExecutorService object (Executors.newCachedThreadPool())
     */
    private final ExecutorService executorServiceImpl;

    /**
     * Constructor and initialize thread pool object
     * default one core pool and the maximum number of threads is 6
     *
     */
    public TNThreadPool() {
        executorServiceImpl = new ThreadPoolExecutor(
                1, 6, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new TNThreadFactory());
    }

    /**
     * Post an runnable to the pool thread
     *
     * @param task The runnable task
     * @return Submit success or not
     */
    public boolean postTask(Runnable task) {
        try {
            executorServiceImpl.execute(task);
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "execute task error:" + e.getMessage());
            return false;
        }
    }
}
