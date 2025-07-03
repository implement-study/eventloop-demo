package club.shengsheng.demo;

import club.shengsheng.EventLoop;

import java.util.concurrent.TimeUnit;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public interface EventLoopGroup {

    void execute(Runnable runnable);

    void schedule(Runnable task, long delay, TimeUnit unit);

    void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

    EventLoop next();
}
