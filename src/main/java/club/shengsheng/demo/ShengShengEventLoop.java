package club.shengsheng.demo;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class ShengShengEventLoop implements EventLoop {

    private static final AtomicInteger ID = new AtomicInteger();

    public static final Runnable WAKEUP = () -> {
    };

    private final Thread thread;

    private final BlockingQueue<Runnable> taskQueue;

    private final BlockingQueue<ScheduleTask> scheduleTaskQueue;

    private volatile boolean shutdown = false;

    public ShengShengEventLoop() {
        taskQueue = new ArrayBlockingQueue<>(1024);
        scheduleTaskQueue = new ArrayBlockingQueue<>(1024);
        this.thread = new EventLoopThread("ShengSheng-EventLoop-" + ID.getAndIncrement());
        thread.start();
    }

    @Override
    public void execute(Runnable runnable) {
        if (!this.taskQueue.offer(runnable)) {
            throw new IllegalArgumentException("队列已满");
        }
    }


    @Override
    public void schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduleTask scheduleTask = new ScheduleTask(task, this, deadlineMs(delay, unit), -1);
        if (!this.scheduleTaskQueue.offer(scheduleTask)) {
            throw new IllegalArgumentException("队列已满");
        }
    }


    @Override
    public void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ScheduleTask scheduleTask = new ScheduleTask(task, this, deadlineMs(initialDelay, unit), unit.toMillis(period));
        if (!this.scheduleTaskQueue.offer(scheduleTask)) {
            throw new IllegalArgumentException("队列已满");
        }
    }

    private long deadlineMs(long delay, TimeUnit unit) {
        long delayMs = unit.toMillis(delay);
        if (delayMs <= 0) {
            return 0;
        }
        return delayMs;
    }

    @Override
    public EventLoop next() {
        return this;
    }

    @Override
    public Queue<ScheduleTask> getScheduleQueue() {
        return scheduleTaskQueue;
    }

    private Runnable takeTask() {
        ScheduleTask scheduleTask = scheduleTaskQueue.peek();
        if (scheduleTask == null) {
            Runnable task = null;
            try {
                task = taskQueue.take();
                if (WAKEUP == task) {
                    task = null;
                }
            } catch (InterruptedException ignore) {

            }
            return task;
        }
        if (scheduleTask.getDeadline() <= System.currentTimeMillis()) {
            return scheduleTaskQueue.poll();
        }
        Runnable task;
        try {
            task = taskQueue.poll(scheduleTask.getDeadline() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
            return null;
        }
        if (task == WAKEUP) {
            return null;
        }
        return task;
    }


    class EventLoopThread extends Thread {

        EventLoopThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                Runnable task = takeTask();
                if (task != null) {
                    task.run();
                }
                if (shutdown) {
                    break;
                }
            }
        }
    }

}
