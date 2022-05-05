package threads;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class PausableExecutor extends ScheduledThreadPoolExecutor {
    private Continue cont;

    public PausableExecutor(int corePoolSize, Continue c) {
        super(corePoolSize);
        cont = c;
    }

    protected void beforeExecute(Thread t, Runnable r) {
        try {
            cont.checkIn();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.beforeExecute(t, r);
    }
}