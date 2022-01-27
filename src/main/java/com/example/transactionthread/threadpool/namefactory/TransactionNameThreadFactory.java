package com.example.transactionthread.threadpool.namefactory;

/**
 * @program: transaction-thread
 * @description: 事务线程工厂
 * @author: lk
 * @create: 2021-12-16 14:09
 **/
public class TransactionNameThreadFactory extends AbstractNameThreadFactory {

    /**
     * 将线程池工厂的前缀
     * 例子:transaction-task-pool--1(线程池的数量)
     */
    @Override
    public String getNamePrefix() {
        return "transaction-task-pool" + "--" + poolNum.getAndIncrement();
    }
}
