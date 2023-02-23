package com.example.transactionthread.core;


/**
 * @program: toolkit
 * @description: 有返回值的主任务接口
 * @author: lk
 * @create: 2021-12-23
 **/
@FunctionalInterface
public interface MajorTaskCallable<V> {

    /**
     * 执行主任务的方法
     *
     * @return V 返回值
     */
    V launchCall();

}