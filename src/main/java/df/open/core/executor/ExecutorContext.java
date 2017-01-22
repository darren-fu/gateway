package df.open.core.executor;

import java.util.concurrent.*;

/**
 * Created by darrenfu on 17-1-16.
 */
public class ExecutorContext {

    private static ExecutorService executorService;

    static {
        executorService = new ThreadPoolExecutor(300, 2000,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    public static void execute(Runnable command) {
        executorService.submit(command);
    }

}
