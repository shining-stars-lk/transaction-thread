package com.example.transactionthread.core;


import com.example.transactionthread.base.BaseThreadTransaction;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * @program: toolkit
 * @description: 事务线程工具类
 * @author: lk
 * @create: 2021-12-15
 **/
@Component
public class ThreadTransactionTool extends BaseThreadTransaction {

    /**
     * 异步任务运行
     * @param taskList 需要异步执行的任务
     */
    public void execute(List<Runnable> taskList) {
        super.baseExecute(taskList);
    }

    /**
     * 异步任务运行
     * @param callTaskList 需要异步执行的任务(有返回值)
     * @param resultList 承载异步执行任务结果的集合
     * @param sequence 是否要求执行的任务 和 承载异步执行任务结果的集合保证顺序
     */
    public <V> void call(List<Callable<V>> callTaskList,List<V> resultList,boolean sequence) {
        super.baseCall(callTaskList,resultList,sequence);
    }

    /**
     * 异步任务独立事务运行(出现异常回滚本异步任务，其他异步任务不会回滚)
     * @param taskList 需要异步执行的任务
     */
    public void independenceTransactionExecute(List<Runnable> taskList) {
        super.independenceTransactionBaseExecute(taskList,null,null);
    }

    /**
     * 异步任务独立事务运行(出现异常回滚本异步任务，其他异步任务不会回滚)
     * @param callTaskList 需要异步执行的任务(有返回值)
     * @param resultList 承载异步执行任务结果的集合
     */
    public <V> void independenceTransactionCall(List<Callable<V>> callTaskList,List<V> resultList) {
        super.independenceTransactionBaseExecute(null,callTaskList,resultList);
    }

    /**
     * @param taskList 需要异步执行的任务
     */
    public void transactionExecute(List<Runnable> taskList) {
        super.baseTransactionExecute(null,null,taskList,null,null,null, task -> new ThreadTransactionTask((Runnable) task));
    }

    /**
     * @param callTaskList 需要异步执行的任务(有返回值)
     * @param resultList 承载异步执行任务结果的集合
     */
    public <T> void transactionCall(List<Callable<T>> callTaskList, List<T> resultList) {
        super.baseTransactionExecute(null,null,null,callTaskList,null,resultList, task -> new ThreadTransactionTask((Callable<T>) task));
    }

    /**
     * @param majorTaskRunnable 需要执行的主任务
     * @param taskList          需要异步执行的任务
     * @throws Throwable 抛出的异常
     */
    public void transactionExecute(MajorTaskRunnable majorTaskRunnable, List<Runnable> taskList) {
        super.baseTransactionExecute(majorTaskRunnable,null,taskList,null,null,null, task -> new ThreadTransactionTask((Runnable) task));
    }

    /**
     * @param majorTaskRunnable 需要执行的主任务
     * @param callTaskList 需要异步执行的任务(有返回值)
     * @param resultList 承载异步执行任务结果的集合
     * @throws Throwable 抛出的异常
     */
    public <T> void transactionCall(MajorTaskRunnable majorTaskRunnable, List<Callable<T>> callTaskList, List<T> resultList) {
        super.baseTransactionExecute(majorTaskRunnable,null,null,callTaskList,null,resultList, task -> new ThreadTransactionTask((Callable<T>) task));
    }

    /**
     * @param majorTaskCallable 需要执行的主任务(有返回值)
     * @param taskList          需要异步执行的任务
     * @return T 主任务的返回值
     * @throws Throwable 抛出的异常
     */
    public <T> T transactionExecute(MajorTaskCallable<T> majorTaskCallable, List<Runnable> taskList) {
        return super.baseTransactionExecute(null,majorTaskCallable,taskList,null,null,null, task -> new ThreadTransactionTask((Runnable) task));
    }

    /**
     * @param majorTaskCallable 需要执行的主任务(有返回值)
     * @param callTaskList 需要异步执行的任务(有返回值)
     * @param resultList 承载异步执行任务结果的集合
     * @return T 主任务的返回值
     * @throws Throwable 抛出的异常
     */
    public <T,V> T transactionCall(MajorTaskCallable<T> majorTaskCallable, List<Callable<V>> callTaskList, List<V> resultList) {
        return super.baseTransactionExecute(null,majorTaskCallable,null,callTaskList,null,resultList, task -> new ThreadTransactionTask((Callable<T>) task));
    }

    /**
     * @param majorTaskCallable  需要执行的主任务(有返回值)
     * @param inputParamTaskList 需要异步执行的任务(需要有主任务中的返回值来做输入参数)
     * @return T 主任务的返回值
     * @throws Throwable 抛出的异常
     */
    public <T> T transactionExecuteInputParamTask(MajorTaskCallable<T> majorTaskCallable, List<InputParamRunnable<T>> inputParamTaskList) {
        return super.baseTransactionExecute(null,majorTaskCallable,inputParamTaskList,null, null,null, task -> new ThreadTransactionTask((InputParamRunnable<T>) task));
    }

    /**
     * @param majorTaskCallable  需要执行的主任务(有返回值)
     * @param inputParamCallTaskList 需要异步执行的任务(有返回值)(需要有主任务中的返回值来做输入参数)
     * @param resultList 承载异步执行任务结果的集合
     * @return T 主任务的返回值
     * @throws Throwable 抛出的异常
     */
    public <T,V> T transactionCallInputParamCallTask(MajorTaskCallable<T> majorTaskCallable, List<InputParamCallable<V,T>> inputParamCallTaskList, List<V> resultList) {
        return super.baseTransactionExecute(null,majorTaskCallable,null,null, inputParamCallTaskList,resultList, task -> new ThreadTransactionTask((InputParamCallable<V,T>) task));
    }
}
