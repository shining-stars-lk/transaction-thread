package com.example.transactionthread.core;

/**
 * @program: toolkit
 * @description: 无返回值的主任务接口
 * @author: lk
 * @create: 2021-12-23
 **/
@FunctionalInterface
public interface MajorTaskRunnable {

    /**
     * 执行主任务的方法
     *
     * @throws Exception 抛出的异常
     */
    void launchRun();
}