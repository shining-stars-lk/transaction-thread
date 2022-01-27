package com.example.transactionthread.base;

import com.example.transactionthread.core.InputParamCallable;
import com.example.transactionthread.core.InputParamRunnable;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @program: transaction-thread
 * @description: 事务线程基础类
 * @author: lk
 * @create: 2022-01-14
 **/
public abstract class BaseThreadTransactionTask<T,V> implements Runnable {

    /**
     * 事务传播行为
     * */
    protected int propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW;

    /**
     * 事务隔离级别
     * */
    protected int isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED;

    /**
     * 回滚标识符
     */
    protected AtomicBoolean rollBackFlag;
    /**
     * 事务管理器
     */
    protected DataSourceTransactionManager txManager;
    /**
     * 主线程计数器
     */
    protected CountDownLatch mainCountDownLatch;
    /**
     * 任务线程计数器
     */
    protected CountDownLatch threadCountDownLatch;
    /**
     * 存放每个任务回滚标识的集合
     */
    protected List<Boolean> taskRollBackFlagList;
    /**
     * 任务
     */
    protected Runnable task;
    /**
     * 有返回值的任务
     * */
    protected Callable<V> callTask;
    /**
     * 装入异步任务结果的list
     * */
    protected List<V> list;
    /**
     * 输入参数
     */
    protected T inputParam;
    /**
     * 带输入参数的任务
     */
    protected InputParamRunnable inputParamTask;
    /**
     * 带输入参数的任务(有返回值)
     * */
    protected InputParamCallable<V,T> inputParamCallable;
}
