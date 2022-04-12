package com.example.transactionthread.core;

/**
 * @program: msa-toolkit
 * @description: 有输入参数的Callable
 * @author: lk
 * @create: 2022-1-11
 **/
@FunctionalInterface
public interface InputParamCallable<T,V> {

    /**
     * 执行异步任务的方法
     *
     * @param v 参数
     * @return T 返回结果
     */
    T acceptCall(V v);
}
