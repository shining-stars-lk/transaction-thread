# transaction-thread项目介绍
**保证异步线程能够保证事务的使用工具**
## 说明
- 此工具是为了解决在使用`spring`框架下，解决使用异步线程执行任务出现的事务问题。
- 针对不同的业务使用提供相应`api`，即可达到目的
- 通过获取`spring`中`bean`的方式引入即可
## 特点
- 支持`批量异步任务`执行
- 支持接收异步任务执行结果的功能，调用api时，传入`List`类型集合参数即可
- 支持`批量异步任务`执行，且支持`事务`，当某个异步任务出现`异常`，`事务`可回滚
- 支持`主任务`和`批量异步任务`的执行，且支持`事务`，当`主任务`和`批量异步任务`中只要有一个任务发生异常`事务`都会`回滚`
- 目前异步任务最大数量为`100`
## 注意
- 底层采用的线程池为核心线程数为`0`，最大线程数为`101`,因为如果核心数不为`0`的话，当任务数大于了线程核心数会发生死锁情况。
- 底层采用的事务级别为`READ_COMMITTED`,如果采用`REPEATABLE_READ`,由于mysql在`REPEATABLE_READ`级别会产生间隙锁，所以可能发生死锁情况。
## 使用
- 项目结果为`springboot`,jar包引入、maven依赖、start依赖均可
- 下面的api举例是为了解释使用，所以并没有提供相应数据库表

# ThreadTransactionTool工具使用
## 提供的api
```java
/**
 * 异步任务运行
 * @param taskList 需要异步执行的任务
 */
public void execute(List<Runnable> taskList)

/**
 * 异步任务运行
 * @param callTaskList 需要异步执行的任务(有返回值)
 * @param resultList 承载异步执行任务结果的集合
 */
public <V> void call(List<Callable<V>> callTaskList,List<V> resultList)
 
/**
 * 异步任务独立事务运行(出现异常回滚本异步任务，其他异步任务不会回滚)
 * @param taskList 需要异步执行的任务
 */
public void independenceTransactionExecute(List<Runnable> taskList)
/**
 * 异步任务独立事务运行(出现异常回滚本异步任务，其他异步任务不会回滚)
 * @param callTaskList 需要异步执行的任务(有返回值)
 * @param resultList 承载异步执行任务结果的集合
 */
public <V> void independenceTransactionCall(List<Callable<V>> callTaskList,List<V> resultList)
/**
* @param taskList 需要异步执行的任务
*/
public void transactionExecute(List<Runnable> taskList)

/**
* @param callTaskList 需要异步执行的任务(有返回值)
* @param resultList 承载异步执行任务结果的集合
*/
public <T> void transactionCall(List<Callable<T>> callTaskList, List<T> resultList)

/**
* @param majorTaskRunnable 需要执行的主任务
* @param taskList          需要异步执行的任务
* @throws Throwable 抛出的异常
*/
public void transactionExecute(MajorTaskRunnable majorTaskRunnable, List<Runnable> taskList)

/**
* @param majorTaskRunnable 需要执行的主任务
* @param callTaskList 需要异步执行的任务(有返回值)
* @param resultList 承载异步执行任务结果的集合
* @throws Throwable 抛出的异常
*/
public <T> void transactionCall(MajorTaskRunnable majorTaskRunnable, 
                      List<Callable<T>> callTaskList, List<T> resultList)

/**
* @param majorTaskCallable 需要执行的主任务(有返回值)
* @param taskList          需要异步执行的任务
* @return T 主任务的返回值
* @throws Throwable 抛出的异常
*/
public <T> T transactionExecute(MajorTaskCallable<T> majorTaskCallable, List<Runnable> taskList)

/**
* @param majorTaskCallable 需要执行的主任务(有返回值)
* @param callTaskList 需要异步执行的任务(有返回值)
* @param resultList 承载异步执行任务结果的集合
* @return T 主任务的返回值
* @throws Throwable 抛出的异常
*/
public <T,V> T transactionCall(MajorTaskCallable<T> majorTaskCallable, 
                      List<Callable<V>> callTaskList, List<V> resultList)

/**
* @param majorTaskCallable  需要执行的主任务(有返回值)
* @param inputParamTaskList 需要异步执行的任务(需要有主任务中的返回值来做输入参数)
* @return T 主任务的返回值
* @throws Throwable 抛出的异常
*/
public <T> T transactionExecuteInputParamTask(MajorTaskCallable<T> majorTaskCallable, 
                      List<InputParamRunnable<T>> inputParamTaskList)

/**
* @param majorTaskCallable  需要执行的主任务(有返回值)
* @param inputParamCallTaskList 需要异步执行的任务(有返回值)(需要有主任务中的返回值来做输入参数)
* @param resultList 承载异步执行任务结果的集合
* @return T 主任务的返回值
* @throws Throwable 抛出的异常
*/
public <T,V> T transactionCallInputParamCallTask(MajorTaskCallable<T> majorTaskCallable, 
                    List<InputParamCallable<V,T>> inputParamCallTaskList, List<V> resultList)
```
## api使用示例
### 1. public void execute(List<Runnable> taskList)
#### 说明
异步任务运行
```java
public Integer addAccount(int id){
    List<Runnable> runnableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Runnable r = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            this.add(account);
        };
        runnableList.add(r);
    }
    threadTransactionTool.execute(runnableList);
    return null;
}
``` 
### 2. public <V> void call(List<Callable<V>> callTaskList,List<V> resultList)
#### 说明
异步任务运行，通过`List类型入参`拿到异步任务结果。  
```java
public Object addAccount(int id){
    List<Callable<Integer>> callableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Callable c = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            this.add(account);
            return temp;
        };
        callableList.add(c);
    }
    List<Integer> resultList = new ArrayList<>();
    threadTransactionTool.call(callableList,resultList);
    return resultList;
}
```  
### 3. public void independenceTransactionExecute(List<Runnable> taskList)
#### 说明
异步任务`独立事务`运行(出现异常`回滚`本异步任务，其他异步任务`不会回滚`)
```java
public Integer addAccount(int id){
    List<Runnable> runnableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Runnable r = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            this.add(account);
        };
        runnableList.add(r);
    }
    threadTransactionTool.independenceTransactionExecute(runnableList);
    return null;
}  
```  

### 4. public <V> void independenceTransactionCall(List<Callable<V>> callTaskList,List<V> resultList)
#### 说明
异步任务`独立事务`运行(出现异常`回滚`本异步任务，其他异步任务`不会回滚`)，通过`List类型入参`拿到异步任务结果。  
```java
public Object addAccount(int id){
    List<Callable<Integer>> callableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Callable c = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            this.add(account);
            return temp;
        };
        callableList.add(c);
    }
    List<Integer> resultList = new ArrayList<>();
    threadTransactionTool.independenceTransactionCall(callableList,resultList);
    return resultList;
}
```  
### 5. public void transactionExecute(List<Runnable> taskList)
#### 说明 
处理异步执行的任务
```java
public Integer addUserAndAccount(User user){
    long startTime = System.currentTimeMillis();
    Integer result = userMapper.add(user);

    List<Runnable> runnableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Runnable r = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            accountService.add(account);
        };
        runnableList.add(r);
    }

    try {
        threadTransactionTool.transactionExecute(runnableList);
    } catch (Throwable e) {
        System.out.println("===检测到异常回滚!!!===");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("===耗时:"+(endTime - startTime)+"===");
    return result;
}
```
#### 特点
批量`异步任务`执行时，当其中一个出现`异常`后，其余的`异步任务`都会`回滚`。但是`当主任务`添加user的操作出现异常，异步任务`不会回滚`，需要使用`threadTransactionTool`提供的另外方法api。
  
### 6. public <T> void transactionCall(List<Callable<T>> callTaskList, List<T> resultList)
#### 说明 
处理异步执行的任务,并拿到异步任务`结果`
```java
public Object addUserAndAccount(User user){
    long startTime = System.currentTimeMillis();

    Integer result = 0;
    List<Callable<Integer>> callableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Callable call = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            accountService.add(account);
            return temp;
        };
        callableList.add(call);
    }

    List<Integer> resultList = new ArrayList<>();
    try {
        threadTransactionTool.transactionCall(callableList,resultList);
    } catch (Throwable e) {
        System.out.println("===检测到异常回滚!!!===");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("===耗时:"+(endTime - startTime)+"===");
    return resultList;
}
```  
#### 特点
对`介绍5`的api增强了`接收异步结果`的功能，通过传入`resultList`参数实现。  

### 7. public void transactionExecute(MajorTaskRunnable majorTaskRunnable, List<Runnable> taskList)
#### 说明
处理主任务和异步执行的任务
```java
public Integer addUserAndAccount(User user){
    long startTime = System.currentTimeMillis();

    Integer result = 0;
    List<Runnable> runnableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Runnable r = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            accountService.add(account);
        };
        runnableList.add(r);
    }
    try {
        threadTransactionTool.transactionExecute(() -> {
            userMapper.add(user);
        }, runnableList);
    } catch (Throwable e) {
        System.out.println("===检测到异常回滚!!!===");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("===耗时:"+(endTime - startTime)+"===");
    return result;
}
```  
#### 特点
批量`异步任务`执行时，当其中一个出现`异常`后，其余的`异步任务`都会`回滚`。并且`主任务`添加user的操作出现`异常`，`异步任务`也能够`回滚`。
  
### 8. public <T> void transactionCall(MajorTaskRunnable majorTaskRunnable, List<Callable<T>> callTaskList, List<T> resultList)
#### 说明
处理主任务和异步执行的任务并拿到异步任务`结果`
```java
public Object addUserAndAccount(User user){
    long startTime = System.currentTimeMillis();

    Integer result = 0;
    List<Callable<Integer>> callableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Callable call = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            accountService.add(account);
            return temp;
        };
        callableList.add(call);
    }

    List<Integer> resultList = new ArrayList<>();
    try {
        threadTransactionTool.transactionCall(() -> {
            userMapper.add(user);
        }, callableList,resultList);
    } catch (Throwable e) {
        System.out.println("===检测到异常回滚!!!===");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("===耗时:"+(endTime - startTime)+"===");
    return resultList;
}
```  
#### 特点
对`介绍7`的api增强了`接收异步结果`的功能，通过传入`resultList`参数实现。  

### 9. public <T> T transactionExecute(MajorTaskCallable<T> majorTaskCallable, List<Runnable> taskList)  
#### 说明
处理主任务和异步执行的任务，并返回主任务`结果`  
```java
public Integer addUserAndAccount(User user){
    long startTime = System.currentTimeMillis();

    Integer result = 0;
    List<Runnable> runnableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Runnable r = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            accountService.add(account);
        };
        runnableList.add(r);
    }
    try {
        result = threadTransactionTool.transactionExecute(() -> {
            int insertResult = userMapper.add(user);
            return insertResult;
        }, runnableList);
    } catch (Throwable e) {
        System.out.println("===检测到异常回滚!!!===");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("===耗时:"+(endTime - startTime)+"===");
    return result;
}
```
#### 特点
对`介绍7`的api增强了`返回主任务结果`的功能。
  
### 10. public <T,V> T transactionCall(MajorTaskCallable<T> majorTaskCallable, List<Callable<V>> callTaskList, List<V> resultList)
#### 说明
处理主任务和异步执行的任务，并返回主任务`结果`，并且拿到异步任务`结果`
```java
public Object addUserAndAccount(User user){
    long startTime = System.currentTimeMillis();

    Integer result = 0;
    List<Callable<Integer>> callableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        Callable call = () -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            accountService.add(account);
            return temp;
        };
        callableList.add(call);
    }

    List<Integer> resultList = new ArrayList<>();
    try {
        result = threadTransactionTool.transactionCall(() -> {
            int insertResult = userMapper.add(user);
            return insertResult;
        }, callableList,resultList);
    } catch (Throwable e) {
        System.out.println("===检测到异常回滚!!!===");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("===耗时:"+(endTime - startTime)+"===");
    return resultList;
}
```  
#### 特点
对`介绍7`的api增强了`接收主任务结果`的功能。也增强了`接收异步结果`的功能，通过传入`resultList`参数实现。 
### 11. public <T> T transactionExecuteInputParamTask(MajorTaskCallable<T> majorTaskCallable, List<InputParamRunnable<T>> inputParamTaskList)
#### 说明
处理主任务和异步执行的任务(`异步任务需要主任务的返回值`)，并返回主任务`结果`  
```java
public Object addUserAndAccount(User user){
    long startTime = System.currentTimeMillis();
    String userNameResult = null;
    List<InputParamRunnable<String>> inputParamRunnableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        InputParamRunnable c = (InputParamRunnable<String>) userName -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            account.setUserName(userName);
            accountService.add(account);
        };
        inputParamRunnableList.add(c);
    }
    try {
        userNameResult = threadTransactionTool.transactionExecuteInputParamTask(() -> {
            userMapper.add(user);
            User userResult = userMapper.get(user.getId());
            return userResult.getName();
        },inputParamRunnableList);
    } catch (Throwable e) {
        System.out.println("===检测到异常回滚!!!"+e+"===");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("===耗时:"+(endTime - startTime)+"===");
    return userNameResult;
}
```  
#### 特点
对`介绍9`的api增加了异步任务需要主任务`返回值`的功能。

### 12. public <T,V> T transactionCallInputParamCallTask(MajorTaskCallable<T> majorTaskCallable, List<InputParamCallable<V,T>> inputParamCallTaskList, List<V> resultList)
#### 说明
处理主任务和异步执行的任务(`异步任务需要主任务的返回值`)，并返回主任务`结果` ,以及拿到异步任务`结果`
```java
public Object addUserAndAccount(User user){
    long startTime = System.currentTimeMillis();
    String userNameResult = null;
    List<InputParamCallable<Integer,String>> inputParamCallableList = new ArrayList<>();
    for(int i = 1; i<= 20; i++){
        int temp = i;
        InputParamCallable c = (InputParamCallable<Integer,String>) userName -> {
            Account account = new Account();
            account.setId(temp);
            account.setAccountId(IdGeneratorUtil.getId());
            account.setName("sss");
            account.setPassword("www");
            account.setAge(temp);
            account.setUserName(userName);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            accountService.add(account);
            return temp;
        };
        inputParamCallableList.add(c);
    }
    List<Integer> resultList = new ArrayList<>();
    try {
        userNameResult = threadTransactionTool.transactionCallInputParamCallTask(() -> {
            userMapper.add(user);
            User userResult = userMapper.get(user.getId());
            return userResult.getName();
        },inputParamCallableList,resultList);
    } catch (Throwable e) {
        System.out.println("===检测到异常回滚!!!"+e+"===");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("===耗时:"+(endTime - startTime)+"===");
    return resultList;
}
```  
  
## 原理

![](https://files.mdnice.com/user/12133/09d545fd-307d-49ed-a626-dbca688be0ac.png)
  