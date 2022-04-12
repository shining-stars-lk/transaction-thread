package com.example.transactionthread.core;


import com.example.transactionthread.base.BaseThreadTransactionTask;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;


/**
 * @program: msa-toolkit
 * @description: 独立事务线程任务类
 * @author: lk
 * @create: 2022-01-14
 **/
public class ThreadIndependenceTransactionTask<V> extends BaseThreadTransactionTask<Object,V> {

    /**
     * 事务传播行为
     * */
    private int propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW;


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
            } else if (callTask != null) {
                V v = callTask.call();
                list.add(v);
            }
            txManager.commit(status);
        } catch (Throwable e) {
            txManager.rollback(status);
            throw new RuntimeException(e);
        } finally {
            if(threadCountDownLatch != null) {
                threadCountDownLatch.countDown();
            }
        }
    }

    public ThreadIndependenceTransactionTask(DataSourceTransactionManager txManager, Runnable task) {
        this.txManager = txManager;
        this.task = task;
    }

    public ThreadIndependenceTransactionTask(DataSourceTransactionManager txManager, CountDownLatch threadCountDownLatch, Callable<V> callTask, List<V> list) {
        this.txManager = txManager;
        this.threadCountDownLatch = threadCountDownLatch;
        this.callTask = callTask;
        this.list = list;
    }

    public DataSourceTransactionManager getTxManager() {
        return txManager;
    }

    public void setTxManager(DataSourceTransactionManager txManager) {
        this.txManager = txManager;
    }

    public CountDownLatch getThreadCountDownLatch() {
        return threadCountDownLatch;
    }

    public void setThreadCountDownLatch(CountDownLatch threadCountDownLatch) {
        this.threadCountDownLatch = threadCountDownLatch;
    }

    public Runnable getTask() {
        return task;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }

    public Callable<V> getCallTask() {
        return callTask;
    }

    public void setCallTask(Callable<V> callTask) {
        this.callTask = callTask;
    }

    public List<V> getList() {
        return list;
    }

    public void setList(List<V> list) {
        this.list = list;
    }
}
