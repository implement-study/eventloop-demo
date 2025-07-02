package club.shengsheng.demo;


/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class ScheduleTask implements Runnable, Comparable<ScheduleTask> {

    private long deadline;

    private final long period;

    private final Runnable task;

    private final EventLoop executor;

    public ScheduleTask(Runnable task, EventLoop eventLoop, long deadline, long period) {
        this.task = task;
        this.executor = eventLoop;
        this.deadline = deadline;
        this.period = period;
    }

    @Override
    public void run() {
        try {
            task.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (period > 0) {
                this.deadline += period;
                this.executor.getScheduleQueue().offer(this);
            }
        }
    }

    public long getDeadline() {
        return deadline;
    }

    @Override
    public int compareTo(ScheduleTask o) {
        return Long.compare(this.deadline, o.deadline);
    }
}
