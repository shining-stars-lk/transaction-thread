package com.example.transactionthread.core;

/**
 * @program: toolkit
 * @description: 有输入参数的Runnable
 * @author: lk
 * @create: 2021-12-23
 **/
@FunctionalInterface
public interface InputParamRunnable<T> {

    /**
     * 执行异步任务的方法
     *
     * @param t 参数
     */
    void acceptRun(T t);
}
