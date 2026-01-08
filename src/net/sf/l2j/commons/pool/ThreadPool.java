package net.sf.l2j.commons.pool;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;

import java.util.concurrent.*;

public final class ThreadPool {
    private static final CLogger LOGGER = new CLogger(ThreadPool.class.getName());

    private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2L;
    private static ScheduledThreadPoolExecutor[] _scheduledPools;
    private static ThreadPoolExecutor[] _instantPools;
    private static int _threadPoolRandomizer;

    public static void init() {
        int poolCount = Config.SCHEDULED_THREAD_POOL_COUNT;
        if (poolCount == -1)
            poolCount = Runtime.getRuntime().availableProcessors();
        _scheduledPools = new ScheduledThreadPoolExecutor[poolCount];
        int i;
        for (i = 0; i < poolCount; i++)
            _scheduledPools[i] = new ScheduledThreadPoolExecutor(Config.THREADS_PER_SCHEDULED_THREAD_POOL);
        poolCount = Config.INSTANT_THREAD_POOL_COUNT;
        if (poolCount == -1)
            poolCount = Runtime.getRuntime().availableProcessors();
        _instantPools = new ThreadPoolExecutor[poolCount];
        for (i = 0; i < poolCount; i++)
            _instantPools[i] = new ThreadPoolExecutor(Config.THREADS_PER_INSTANT_THREAD_POOL, Config.THREADS_PER_INSTANT_THREAD_POOL, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100000));
        for (ScheduledThreadPoolExecutor threadPool : _scheduledPools)
            threadPool.prestartAllCoreThreads();
        for (ThreadPoolExecutor threadPool : _instantPools)
            threadPool.prestartAllCoreThreads();
        scheduleAtFixedRate(() -> {
            for (ScheduledThreadPoolExecutor threadPool : _scheduledPools)
                threadPool.purge();
            for (ThreadPoolExecutor threadPool : _instantPools)
                threadPool.purge();
        }, 600000L, 600000L);
        LOGGER.info("Initializing ThreadPool.");
    }

    public static ScheduledFuture<?> schedule(Runnable r, long delay) {
        try {
            return getPool(_scheduledPools).schedule(new TaskWrapper(r), validate(delay), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period) {
        try {
            return getPool(_scheduledPools).scheduleAtFixedRate(new TaskWrapper(r), validate(delay), validate(period), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    public static void execute(Runnable r) {
        try {
            getPool(_instantPools).execute(new TaskWrapper(r));
        } catch (Exception ignored) {
        }
    }

    public static void getStats() {
        int i;
        for (i = 0; i < _scheduledPools.length; i++) {
            ScheduledThreadPoolExecutor threadPool = _scheduledPools[i];
            LOGGER.info("=================================================");
            LOGGER.info("Scheduled pool #" + i + ":");
            LOGGER.info("\tgetActiveCount: ...... " + threadPool.getActiveCount());
            LOGGER.info("\tgetCorePoolSize: ..... " + threadPool.getCorePoolSize());
            LOGGER.info("\tgetPoolSize: ......... " + threadPool.getPoolSize());
            LOGGER.info("\tgetLargestPoolSize: .. " + threadPool.getLargestPoolSize());
            LOGGER.info("\tgetMaximumPoolSize: .. " + threadPool.getMaximumPoolSize());
            LOGGER.info("\tgetCompletedTaskCount: " + threadPool.getCompletedTaskCount());
            LOGGER.info("\tgetQueuedTaskCount: .. " + threadPool.getQueue().size());
            LOGGER.info("\tgetTaskCount: ........ " + threadPool.getTaskCount());
        }
        for (i = 0; i < _instantPools.length; i++) {
            ThreadPoolExecutor threadPool = _instantPools[i];
            LOGGER.info("=================================================");
            LOGGER.info("Instant pool #" + i + ":");
            LOGGER.info("\tgetActiveCount: ...... " + threadPool.getActiveCount());
            LOGGER.info("\tgetCorePoolSize: ..... " + threadPool.getCorePoolSize());
            LOGGER.info("\tgetPoolSize: ......... " + threadPool.getPoolSize());
            LOGGER.info("\tgetLargestPoolSize: .. " + threadPool.getLargestPoolSize());
            LOGGER.info("\tgetMaximumPoolSize: .. " + threadPool.getMaximumPoolSize());
            LOGGER.info("\tgetCompletedTaskCount: " + threadPool.getCompletedTaskCount());
            LOGGER.info("\tgetQueuedTaskCount: .. " + threadPool.getQueue().size());
            LOGGER.info("\tgetTaskCount: ........ " + threadPool.getTaskCount());
        }
    }

    public static void shutdown() {
        try {
            System.out.println("ThreadPool: Shutting down.");
            for (ScheduledThreadPoolExecutor threadPool : _scheduledPools)
                threadPool.shutdownNow();
            for (ThreadPoolExecutor threadPool : _instantPools)
                threadPool.shutdownNow();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static <T> T getPool(T[] threadPools) {
        return threadPools[_threadPoolRandomizer++ % threadPools.length];
    }

    private static long validate(long delay) {
        return Math.max(0L, Math.min(MAX_DELAY, delay));
    }

    public static final class TaskWrapper implements Runnable {
        private final Runnable _runnable;

        public TaskWrapper(Runnable runnable) {
            this._runnable = runnable;
        }

        public void run() {
            try {
                this._runnable.run();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}
