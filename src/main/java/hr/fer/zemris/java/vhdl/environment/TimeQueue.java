package hr.fer.zemris.java.vhdl.environment;

import hr.fer.zemris.java.vhdl.models.jobs.AbstractJob;

import java.util.concurrent.DelayQueue;

/**
 * Created by Dominik on 24.2.2017..
 */
public class TimeQueue implements IModelListener {
    private Environment environment;
    private DelayQueue<AbstractJob> queue = new DelayQueue<>();

    public TimeQueue(Environment environment) {
        this.environment = environment;
    }

    public AbstractJob getEvent() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void addEvent(AbstractJob job) {
        queue.add(job);
    }

    @Override
    public void signalChanged(int address, long time) {
        queue.add(new Execute(time, address));
    }

    public class Execute extends AbstractJob {
        private int address;

        public Execute(long currentTime, int address) {
            super(currentTime);
            this.address = address;
        }

        @Override
        public void execute() {
            TimeQueue.this.environment.getSimulator().execute(address, startTime);
        }
    }
}
