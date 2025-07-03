package club.shengsheng.demo;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class TargetRunnable implements Runnable {


    private final int index;
    private final Runnable task;

    public TargetRunnable(int index, Runnable task) {
        this.index = index;
        this.task = task;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void run() {
        task.run();
    }
}
