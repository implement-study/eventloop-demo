package club.shengsheng.demo;

import java.util.concurrent.TimeUnit;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class ShengShengEventLoopGroup implements EventLoopGroup{
    
    
    @Override
    public void execute(Runnable runnable) {
        
    }

    @Override
    public void schedule(Runnable task, long delay, TimeUnit unit) {

    }

    @Override
    public void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {

    }

    @Override
    public EventLoop next() {
        return null;
    }
}
