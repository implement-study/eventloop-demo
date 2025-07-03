package club.shengsheng.demo;

import club.shengsheng.EventLoop;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class ScheduleTask implements Runnable, Comparable<ScheduleTask> {

    private final Runnable runnable;

    private long deadline;

    private long period;

    private EventLoop eventLoop;

    public ScheduleTask(Runnable runnable, EventLoop eventLoop, long deadline, long period) {
        this.runnable = runnable;
        this.eventLoop = eventLoop;
        this.deadline = deadline;
        this.period = period;
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (period > 0) {
                this.deadline += period;
                eventLoop.getScheduleTaskQueue().offer(this);
            }
        }
    }

    public long getDeadline() {
        return deadline;
    }

    @Override
    public int compareTo(ScheduleTask o) {
        return Long.compare(this.deadline,o.deadline);
    }
}
