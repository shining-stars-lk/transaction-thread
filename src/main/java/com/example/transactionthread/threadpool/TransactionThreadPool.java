package com.example.transactionthread.threadpool;

import com.example.transactionthread.threadpool.namefactory.TransactionNameThreadFactory;
import com.example.transactionthread.threadpool.rejectedexecutionhandler.ThreadPoolRejectedExecutionHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: transaction-thread
 * @description: 事务线程池(此线程池只做内部工具业务上请使用MsaThreadPool)
 * @author: lk
 * @create: 2021-12-16 14:09
 **/

public class TransactionThreadPool {
    private static ThreadPoolExecutor execute = null;

    static {
        execute = new CustomerThreadPoolExecutor(
                // 核心线程数
                0,
                // 最大线程数
                101,
                // 线程存活时间
                60L,
                // 存活时间单位
                TimeUnit.SECONDS,
                // 队列容量
                new SynchronousQueue<Runnable>(),
                // 线程工厂
                new TransactionNameThreadFactory(),
                // 拒绝策略
                new ThreadPoolRejectedExecutionHandler.TransactionAbortPolicy());
    }

    static class CustomerThreadPoolExecutor extends ThreadPoolExecutor {
        public CustomerThreadPoolExecutor(int corePoolSize,
                                          int maximumPoolSize,
                                          long keepAliveTime,
                                          TimeUnit unit,
                                          BlockingQueue<Runnable> workQueue,
                                          ThreadFactory threadFactory,
                                          RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }
    }

    /**
     * 执行任务
     *
     * @param r 提交的任务
     * @return
     */
    public static void execute(Runnable r) {
        execute.execute(r);
    }

    /**
     * 执行带返回值任务
     *
     * @param c 提交的任务
     * @return
     */
    public static <T> Future<T> submit(Callable<T> c) {
        return execute.submit(c);
    }
}
