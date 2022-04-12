package com.example.transactionthread.base;


import com.example.transactionthread.core.ThreadTransactionTask;

/**
 * @program: msa-toolkit
 * @description: 具体执行
 * @author: lk
 * @create: 2022-01-12
 **/
@FunctionalInterface
public interface ThreadTransaction {

    /**
     * 具体执行
     * */
    ThreadTransactionTask getThreadTransactionTask(Object task);
}
