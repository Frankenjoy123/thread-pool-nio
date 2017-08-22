package io.leader.util;

import java.util.concurrent.*;

/**
 * Created by frank on 2017/8/21.
 */
public class ExecutorServiceUtil {

    private static ExecutorService executorService;

    public static ExecutorService getInstance(){
        if (executorService==null){
            synchronized (ExecutorServiceUtil.class){
                if (executorService==null){
                    return new ThreadPoolExecutor(6, 6,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<Runnable>());
                }
            }
        }
        return executorService;
    }
}
