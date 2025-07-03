package club.shengsheng.demo;


import java.util.Queue;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public interface EventLoop extends EventLoopGroup {


    Queue<ScheduleTask> getScheduleTaskQueue();
}
