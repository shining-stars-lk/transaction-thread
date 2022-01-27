package com.example.transactionthread.base;

import com.example.transactionthread.InputParamCallable;
import com.example.transactionthread.MajorTaskCallable;
import com.example.transactionthread.MajorTaskRunnable;
import com.example.transactionthread.ThreadIndependenceTransactionTask;
import com.example.transactionthread.ThreadTask;
import com.example.transactionthread.ThreadTransactionTask;
import com.example.transactionthread.threadpool.TransactionThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @program: transaction-thread
 * @description: 事务线程基类（不对外暴露，是作为ThreadTransactionTool的抽象基类）
 * @author: lk
 * @create: 2022-01-12
 **/
@Component
public class BaseThreadTransaction {

    /**
     * Spring事务管理器
     * */
    @Autowired
    private DataSourceTransactionManager txManager;

    /**
     * 异步任务最大数量
     * */
    private final static int MAX_TASK = 100;


    /**
     * 异步任务基础执行
     * @param taskList 需要异步执行的任务
     * @param callTaskList 需要异步执行的任务(有返回值)
     * @param resultList 载异步执行任务结果的集合
     * */
    public <V> void baseExecute(List<Runnable> taskList, List<Callable<V>> callTaskList,List<V> resultList) {
        try{
            if (taskList != null && taskList.size() > 0) {
                checkTaskList(taskList, MAX_TASK);
                for (Runnable task : taskList) {
                    ThreadTask threadTask =
                            new ThreadTask(task);
                    TransactionThreadPool.execute(threadTask);
                }
            }else if (callTaskList != null && callTaskList.size() > 0) {
                checkTaskList(callTaskList, MAX_TASK);
                //任务线程计数器
                CountDownLatch threadCountDownLatch = threadLatch(getSize(taskList,callTaskList,null));
                List<V> executeResultList = Collections.synchronizedList(new ArrayList<>());
                for (Callable<V> callTask : callTaskList) {
                    ThreadTask threadTask =
                            new ThreadTask(threadCountDownLatch,callTask,executeResultList);
                    TransactionThreadPool.execute(threadTask);
                }
                threadCountDownLatch.await(20, TimeUnit.SECONDS);
                resultList.addAll(executeResultList);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 异步任务独立事务基础执行
     * @param taskList 需要异步执行的任务
     * @param callTaskList 需要异步执行的任务(有返回值)
     * @param resultList 载异步执行任务结果的集合
     * */
    public <V> void independenceTransactionBaseExecute(List<Runnable> taskList, List<Callable<V>> callTaskList,List<V> resultList){

        try{
            if (taskList != null && taskList.size() > 0) {
                checkTaskList(taskList, MAX_TASK);
                for (Runnable task : taskList) {
                    ThreadIndependenceTransactionTask taskTask =
                            new ThreadIndependenceTransactionTask(txManager,task);
                    TransactionThreadPool.execute(taskTask);
                }
            }else if (callTaskList != null && callTaskList.size() > 0) {
                checkTaskList(callTaskList, MAX_TASK);
                //任务线程计数器
                CountDownLatch threadCountDownLatch = threadLatch(getSize(taskList,callTaskList,null));
                List<V> executeResultList = Collections.synchronizedList(new ArrayList<>());
                for (Callable<V> callTask : callTaskList) {
                    ThreadIndependenceTransactionTask taskTask =
                            new ThreadIndependenceTransactionTask(txManager,threadCountDownLatch,callTask,executeResultList);
                    TransactionThreadPool.execute(taskTask);
                }
                threadCountDownLatch.await(20, TimeUnit.SECONDS);
                resultList.addAll(executeResultList);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 基础执行
     * @param majorTaskRunnable 需要执行的主任务
     * @param majorTaskCallable 需要执行的主任务(有返回值)
     * @param taskList 需要异步执行的任务(包括Runnable、InputParamRunnable类型)
     * @param callTaskList 需要异步执行的任务(有返回值)
     * @param inputParamCallableList 需要异步执行的任务(有返回值)(需要有主任务中的返回值来做输入参数)
     * @param resultList 载异步执行任务结果的集合
     * @param threadTransaction 需要自定义实现的转换任务接口
     * */
    public <T,V> T baseTransactionExecute(MajorTaskRunnable majorTaskRunnable, MajorTaskCallable<T> majorTaskCallable,
                                          List taskList, List<Callable<V>> callTaskList, List<InputParamCallable<V,T>> inputParamCallableList,
                                          List<V> resultList, ThreadTransaction threadTransaction){
        //主线程计数器
        CountDownLatch mainCountDownLatch = mainLatch();
        //任务线程计数器
        CountDownLatch threadCountDownLatch = threadLatch(getSize(taskList,callTaskList,inputParamCallableList));
        //回滚标识符
        AtomicBoolean rollBackFlag = rollbackFlag();
        //存放每个任务回滚标识的集合
        List<Boolean> taskRollBackFlagList = taskRollBackFlagList();
        T t = null;
        try{
            //没有返回值的主任务执行
            if (majorTaskRunnable != null) {
                try {
                    majorTaskRunnable.launchRun();
                } catch (Throwable e) {
                    rollBackFlag.set(true);
                    throw new RuntimeException(e);
                }
            }
            //有返回值的主任务执行
            if (majorTaskCallable != null) {
                try {
                    t = majorTaskCallable.launchCall();
                } catch (Throwable e) {
                    rollBackFlag.set(true);
                    throw new RuntimeException(e);
                }
            }

            if (taskList != null && taskList.size() > 0) {
                checkTaskList(taskList, MAX_TASK);
                for (Object task : taskList) {
                    ThreadTransactionTask threadTransactionTask = threadTransaction.getThreadTransactionTask(task);

                    threadTransactionTask.build()
                                    .setRollBackFlag(rollBackFlag)
                                    .setTxManager(txManager)
                                    .setMainCountDownLatch(mainCountDownLatch)
                                    .setThreadCountDownLatch(threadCountDownLatch)
                                    .setTaskRollBackFlagList(taskRollBackFlagList)
                                    .setInputParam(t);

                    TransactionThreadPool.execute(threadTransactionTask);
                }

                threadCountDownLatch.await(20, TimeUnit.SECONDS);

                setRollBackFlag(taskRollBackFlagList,rollBackFlag);
            }else if (callTaskList != null && callTaskList.size() > 0) {
                checkTaskList(callTaskList, MAX_TASK);
                List<V> executeResultList = Collections.synchronizedList(new ArrayList<>());
                for (Callable callTask : callTaskList) {
                    ThreadTransactionTask threadTransactionTask = threadTransaction.getThreadTransactionTask(callTask);
                    threadTransactionTask.build()
                                    .setRollBackFlag(rollBackFlag)
                                    .setTxManager(txManager)
                                    .setMainCountDownLatch(mainCountDownLatch)
                                    .setThreadCountDownLatch(threadCountDownLatch)
                                    .setTaskRollBackFlagList(taskRollBackFlagList)
                                    .setInputParam(t)
                                    .setList(executeResultList);

                    TransactionThreadPool.execute(threadTransactionTask);
                }

                threadCountDownLatch.await(20, TimeUnit.SECONDS);

                setRollBackFlag(taskRollBackFlagList,rollBackFlag);

                resultList.addAll(executeResultList);
            }else if (inputParamCallableList != null && inputParamCallableList.size() > 0) {
                checkTaskList(inputParamCallableList, MAX_TASK);
                List<V> executeResultList = Collections.synchronizedList(new ArrayList<>());
                for (InputParamCallable<V,T> inputParamCallTask : inputParamCallableList) {
                    ThreadTransactionTask threadTransactionTask = threadTransaction.getThreadTransactionTask(inputParamCallTask);
                    threadTransactionTask.build()
                                    .setRollBackFlag(rollBackFlag)
                                    .setTxManager(txManager)
                                    .setMainCountDownLatch(mainCountDownLatch)
                                    .setThreadCountDownLatch(threadCountDownLatch)
                                    .setTaskRollBackFlagList(taskRollBackFlagList)
                                    .setInputParam(t)
                                    .setList(executeResultList);

                    TransactionThreadPool.execute(threadTransactionTask);
                }

                threadCountDownLatch.await(20, TimeUnit.SECONDS);

                setRollBackFlag(taskRollBackFlagList,rollBackFlag);

                resultList.addAll(executeResultList);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            mainCountDownLatch.countDown();
        }
        return t;
    }


    /**
     * 验证异步任务的数量
     * @param taskList 异步任务
     * @taskMaximum 最大数量限制
     * */
    private void checkTaskList(List taskList, int taskMaximum) {
        if ((taskList != null && taskList.size() > taskMaximum)) {
            throw new RuntimeException("大于最大任务数");
        }
    }

    /**
     * 获得异步任务的数量
     * @param taskList 异步任务
     * @param callTaskList 异步任务(有返回值)
     * @param inputParamCallableList 异步任务(需要主任务输入参数、有返回值)
     * @taskMaximum 最大数量限制
     * */
    public <T,V> int getSize(List taskList, List<Callable<V>> callTaskList, List<InputParamCallable<V,T>> inputParamCallableList){
        if (taskList != null) {
            return taskList.size();
        }
        if (callTaskList != null) {
            return callTaskList.size();
        }
        if (inputParamCallableList != null) {
            return inputParamCallableList.size();
        }
        return 0;
    }

    /**
     * 主任务计数器
     * @return CountDownLatch
     * */
    public CountDownLatch mainLatch(){
        //主线程计数器
        CountDownLatch mainCountDownLatch = new CountDownLatch(1);
        return mainCountDownLatch;
    }

    /**
     * 任务线程计数器
     * @param size 任务数量
     * @return CountDownLatch
     * */
    public CountDownLatch threadLatch(int size) {
        //任务线程计数器
        CountDownLatch threadCountDownLatch = new CountDownLatch(size);
        return threadCountDownLatch;
    }

    /**
     * 回滚标识符
     * @return AtomicBoolean
     * */
    public AtomicBoolean rollbackFlag(){
        //回滚标识符
        AtomicBoolean rollBackFlag = new AtomicBoolean(false);
        return rollBackFlag;
    }

    /**
     * 存放每个任务回滚标识的集合
     * @return List<Boolean>
     * */
    public List<Boolean> taskRollBackFlagList(){
        //存放每个任务回滚标识的集合
        List<Boolean> taskRollBackFlagList = Collections.synchronizedList(new ArrayList<>());
        return taskRollBackFlagList;
    }

    /**
     * 判断异步的任务来设置回滚标识
     * @param taskRollBackFlagList 异步任务回滚标识集合
     * @param rollBackFlag 是否回滚标识
     * */
    public void setRollBackFlag(List<Boolean> taskRollBackFlagList,AtomicBoolean rollBackFlag){
        for (Boolean taskRollBackFlag : taskRollBackFlagList) {
            if (taskRollBackFlag) {
                rollBackFlag.set(true);
                throw new RuntimeException("任务出现异常");
            }
        }
    }
}
