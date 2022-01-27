package com.example.transactionthread.core;

import com.example.transactionthread.base.BaseThreadTransactionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @program: transaction-thread
 * @description: 事务线程任务类
 * @author: lk
 * @create: 2021-12-15
 **/
public class ThreadTransactionTask<T,V> extends BaseThreadTransactionTask<T,V> {

    private Logger logger = LoggerFactory.getLogger(ThreadTransactionTask.class);


    @Override
    public void run() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        // 事务传播行为
        def.setPropagationBehavior(propagationBehavior);
        // 事务隔离级别
        def.setIsolationLevel(isolationLevel);
        // 获得事务状态
        TransactionStatus status = txManager.getTransaction(def);
        try {
            if (task != null) {
                task.run();
            } else if (inputParamTask != null) {
                inputParamTask.acceptRun(inputParam);
            } else if (callTask != null) {
                V v = callTask.call();
                list.add(v);
            } else if (inputParamCallable != null) {
                V v = inputParamCallable.acceptCall(inputParam);
                list.add(v);
            }
            taskRollBackFlagList.add(false);
        } catch (Throwable e) {
            taskRollBackFlagList.add(true);
            throw new RuntimeException(e);
        } finally {
            threadCountDownLatch.countDown();
        }
        try {
            mainCountDownLatch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (rollBackFlag.get()) {
            txManager.rollback(status);
        } else {
            txManager.commit(status);
        }
    }

    public ThreadTransactionTask(Runnable task) {
        this.task = task;
    }

    public ThreadTransactionTask(Callable callTask) {
        this.callTask = callTask;
    }

    public ThreadTransactionTask(InputParamRunnable inputParamTask) {
        this.inputParamTask = inputParamTask;
    }

    public ThreadTransactionTask(InputParamCallable inputParamCallable) {
        this.inputParamCallable = inputParamCallable;
    }

    public ThreadTransactionTask build() {
        return this;
    }

    public AtomicBoolean getRollBackFlag() {
        return rollBackFlag;
    }

    public ThreadTransactionTask setRollBackFlag(AtomicBoolean rollBackFlag) {
        this.rollBackFlag = rollBackFlag;
        return this;
    }

    public DataSourceTransactionManager getTxManager() {
        return txManager;
    }

    public ThreadTransactionTask setTxManager(DataSourceTransactionManager txManager) {
        this.txManager = txManager;
        return this;
    }

    public CountDownLatch getMainCountDownLatch() {
        return mainCountDownLatch;
    }

    public ThreadTransactionTask setMainCountDownLatch(CountDownLatch mainCountDownLatch) {
        this.mainCountDownLatch = mainCountDownLatch;
        return this;
    }

    public CountDownLatch getThreadCountDownLatch() {
        return threadCountDownLatch;
    }

    public ThreadTransactionTask setThreadCountDownLatch(CountDownLatch threadCountDownLatch) {
        this.threadCountDownLatch = threadCountDownLatch;
        return this;
    }

    public List<Boolean> getTaskRollBackFlagList() {
        return taskRollBackFlagList;
    }

    public ThreadTransactionTask setTaskRollBackFlagList(List<Boolean> taskRollBackFlagList) {
        this.taskRollBackFlagList = taskRollBackFlagList;
        return this;
    }

    public Runnable getTask() {
        return task;
    }

    public ThreadTransactionTask setTask(Runnable task) {
        this.task = task;
        return this;
    }

    public Callable<V> getCallTask() {
        return callTask;
    }

    public ThreadTransactionTask setCallTask(Callable<V> callTask) {
        this.callTask = callTask;
        return this;
    }

    public List<V> getList() {
        return list;
    }

    public ThreadTransactionTask setList(List<V> list) {
        this.list = list;
        return this;
    }

    public T getInputParam() {
        return inputParam;
    }

    public ThreadTransactionTask setInputParam(T inputParam) {
        this.inputParam = inputParam;
        return this;
    }

    public InputParamRunnable getInputParamTask() {
        return inputParamTask;
    }

    public ThreadTransactionTask setInputParamTask(InputParamRunnable inputParamTask) {
        this.inputParamTask = inputParamTask;
        return this;
    }

    public InputParamCallable<V, T> getInputParamCallable() {
        return inputParamCallable;
    }

    public ThreadTransactionTask setInputParamCallable(InputParamCallable<V, T> inputParamCallable) {
        this.inputParamCallable = inputParamCallable;
        return this;
    }
}
