package com.example.transactionthread.threadpool.rejectedexecutionhandler;



import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @program: transaction-thread
 * @description: 线程池异常处理
 * @author: lk
 * @create: 2021-12-06
 **/
public class ThreadPoolRejectedExecutionHandler {

    /**
     * 事务线程池快速拒绝
     */
    public static class TransactionAbortPolicy implements RejectedExecutionHandler {

        public TransactionAbortPolicy() {
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            throw new RejectedExecutionException("ThreadPoolApplicationName Transaction Task " + r.toString() +
                    " rejected from " +
                    executor.toString());
        }
    }
}
