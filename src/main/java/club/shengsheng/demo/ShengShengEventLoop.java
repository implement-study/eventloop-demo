package club.shengsheng.demo;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class ShengShengEventLoop implements EventLoop {

    private static final AtomicInteger THREAD_NAME_INDEX = new AtomicInteger();

    private static final Runnable WAKE_UP = () -> {
    };

    private final BlockingQueue<Runnable> taskQueue;

    private final PriorityBlockingQueue<ScheduleTask> scheduleTaskBlockingQueue;

    private final Thread thread;


    public ShengShengEventLoop() {
        this.taskQueue = new ArrayBlockingQueue<>(1024);
        this.scheduleTaskBlockingQueue = new PriorityBlockingQueue<>(1024);
        this.thread = new EventLoopThread("shengsheng-eventLoop-thread-" + THREAD_NAME_INDEX.incrementAndGet());
        this.thread.start();
    }

    @Override
    public void execute(Runnable runnable) {
        if (!taskQueue.offer(runnable)) {
            throw new RuntimeException("阻塞队列已经满了");
        }
    }

    @Override
    public void schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduleTask scheduleTask = new ScheduleTask(task, this, deadlineMs(delay, unit), -1);
        if (!scheduleTaskBlockingQueue.offer(scheduleTask)) {
            throw new RuntimeException("阻塞队列已经满了");
        }
        execute(WAKE_UP);
    }

    @Override
    public void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ScheduleTask scheduleTask = new ScheduleTask(task, this, deadlineMs(initialDelay, unit), unit.toMillis(period));
        if (!scheduleTaskBlockingQueue.offer(scheduleTask)) {
            throw new RuntimeException("阻塞队列已经满了");
        }
        execute(WAKE_UP);
    }

    private long deadlineMs(long delay, TimeUnit unit) {
        return unit.toMillis(delay) + System.currentTimeMillis();
    }

    @Override
    public EventLoop next() {
        return this;
    }

    private Runnable getTask() {
        ScheduleTask scheduleTask = scheduleTaskBlockingQueue.peek();
        if (scheduleTask == null) {
            Runnable task = null;
            try {
                task = taskQueue.take();
                if(task == WAKE_UP){
                    task = null;
                }
            } catch (InterruptedException ignore) {

            }
            return task;
        }
        if (scheduleTask.getDeadline() <= System.currentTimeMillis()) {
            return scheduleTaskBlockingQueue.poll();
        }
        Runnable task = null;
        try {
            task = taskQueue.poll(scheduleTask.getDeadline() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            if(task == WAKE_UP){
                task = null;
            }
        } catch (InterruptedException ignore) {

        }
        return task;

    }

    @Override
    public Queue<ScheduleTask> getScheduleTaskQueue() {
        return this.scheduleTaskBlockingQueue;
    }

    class EventLoopThread extends Thread {

        public EventLoopThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                Runnable task = getTask();
                if (task != null) {
                    task.run();
                }
            }
        }
    }
}
