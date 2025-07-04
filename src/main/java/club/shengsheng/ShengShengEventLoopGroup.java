package club.shengsheng;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class ShengShengEventLoopGroup implements EventLoopGroup {

    private final EventLoop[] children;

    private final AtomicInteger index = new AtomicInteger(0);

    public ShengShengEventLoopGroup(int threadNum) {
        this.children = new EventLoop[threadNum];
        for (int i = 0; i < children.length; i++) {
            children[i] = new ShengShengEventLoop();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        if (runnable instanceof TargetRunnable targetRunnable) {
            children[targetRunnable.getIndex()].execute(runnable);
        } else {
            next().execute(runnable);
        }
    }

    @Override
    public void schedule(Runnable runnable, long delay, TimeUnit unit) {
        if (runnable instanceof TargetRunnable targetRunnable) {
            children[targetRunnable.getIndex()].schedule(runnable, delay, unit);
        } else {
            next().schedule(runnable, delay, unit);
        }

    }

    @Override
    public void scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        if (runnable instanceof TargetRunnable targetRunnable) {
            children[targetRunnable.getIndex()].scheduleAtFixedRate(runnable, initialDelay, period, unit);
        } else {
            next().scheduleAtFixedRate(runnable, initialDelay, period, unit);
        }
    }

    @Override
    public EventLoop next() {
        return children[index.getAndIncrement() % children.length];
    }
}
