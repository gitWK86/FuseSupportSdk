package cn.houlang.support.thread;

import android.util.Log;

import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author #Suyghur.
 * Created on 2020/7/29
 */
public class ThreadManager {
    private static ThreadManager instance;
    private ExecutorService mThreadPool;

    private WorkTaskQueue queue;

    private Timer mTimer;

    public ThreadManager() {
        mThreadPool = Executors.newCachedThreadPool();
        mTimer = new Timer();
        queue = new WorkTaskQueue();
    }

    public synchronized static ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    public void execute(Runnable task) {
        if (task == null) {
            return;
        }
        mThreadPool.execute(task);
    }

    public void execute(FutureTask<?> task) {
        if (task == null) {
            return;
        }
        mThreadPool.submit(task);
    }


    /**
     * 排队执行  用于访问网络
     *
     * @param task
     */
    public void queueTask(Runnable task) {
        if (task == null) {
            return;
        }
        queue.add(task);
    }

    public ExecutorService getThreadPool() {
        return mThreadPool;
    }

    public Timer getTimer() {
        return mTimer;
    }

    public void destroy() {
        mThreadPool.shutdown();
        mTimer.cancel();
    }

    private class WorkTaskQueue extends ConcurrentLinkedQueue<Runnable> {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(final Runnable e) {
            super.add(e);
            Log.d("rvds", "网络任务队列的个数--> " + size());
            if (size() >= 1) {
                mThreadPool.execute(new Runnable() {

                    @Override
                    public void run() {
                        e.run();
                        poll();
                        Log.d("rvds", "网络任务队列的个数--> " + size());
                        Runnable task = null;
                        while ((task = peek()) != null) {
                            task.run();
                            poll();
                            Log.d("rvds", "网络任务队列的个数--> " + size());
                        }
                    }
                });
            }
            return true;
        }
    }
}
