package cn.houlang.rvds;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author #Suyghur.
 * Created on 9/21/20
 */
public class ScheduledWorker {

    private ScheduledExecutorService mService;
    private ScheduledFuture<?> mFuture;

    public ScheduledWorker(int poolSize) {
        this.mService = new ScheduledThreadPoolExecutor(poolSize);
    }

    public void invokeAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        this.mFuture = mService.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }

    public void cancel() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(false);
        }
    }
}
