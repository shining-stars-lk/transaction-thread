package com.example.transactionthread;

import com.example.transactionthread.base.BaseThreadTransactionTask;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @program: transaction-thread
 * @description: 线程任务
 * @author: lk
 * @create: 2022-01-18
 **/
public class ThreadTask<V> extends BaseThreadTransactionTask<Object,V> {


    @Override
    public void run() {
        try {
            if (task != null) {
                task.run();
            } else if (callTask != null) {
                V v = callTask.call();
                list.add(v);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if(threadCountDownLatch != null) {
                threadCountDownLatch.countDown();
            }
        }
    }

    public ThreadTask(Runnable task){
        this.task = task;
    }

    public ThreadTask(CountDownLatch threadCountDownLatch, Callable<V> callTask, List<V> list){
        this.threadCountDownLatch = threadCountDownLatch;
        this.callTask = callTask;
        this.list = list;
    }
}
