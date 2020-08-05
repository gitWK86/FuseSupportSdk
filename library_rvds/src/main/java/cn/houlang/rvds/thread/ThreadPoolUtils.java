package cn.houlang.rvds.thread;

import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class ThreadPoolUtils {

    private ThreadPoolUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 可用的处理器数量
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * 线程池的核心线程数
     */
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    /**
     * 最大的线程数
     */
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    /**
     * 超时的时间
     */
    private static final int KEEP_ALIVE_SECONDS = 30;

    private static LinkedBlockingDeque workQueue = new LinkedBlockingDeque();
    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "rvds " + integer.getAndIncrement());
        }
    };
    private static ThreadPoolExecutor threadPool;

    static {
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    /**
     * 启动线程
     */
    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    /**
     * 删除线程
     */
    public static void remove(Runnable runnable) {
        threadPool.remove(runnable);
    }

    /**
     * 启动线程
     */
    public static void execute(FutureTask futureTask) {
        threadPool.execute(futureTask);
    }

    /**
     * 删除线程
     */
    public static void remove(FutureTask futureTask) {
        futureTask.cancel(true);
    }
}
