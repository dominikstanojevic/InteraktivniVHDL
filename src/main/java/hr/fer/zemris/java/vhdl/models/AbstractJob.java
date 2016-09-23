package hr.fer.zemris.java.vhdl.models;

import hr.fer.zemris.java.vhdl.Environment;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dominik on 23.9.2016..
 */
public abstract class AbstractJob implements Delayed {
	protected long startTime;

	public AbstractJob(long currentTime, long delay) {
		startTime = currentTime + delay;
	}

	public AbstractJob(long currentTime) {
		startTime = currentTime;
	}

	public abstract void execute(Environment environment);

	@Override
	public long getDelay(TimeUnit unit) {
		long diff = startTime - System.currentTimeMillis();
		return unit.convert(diff, TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		if(this.startTime < ((AbstractJob) o).startTime) {
			return -1;
		}
		if (this.startTime > ((AbstractJob) o).startTime) {
			return 1;
		}
		return 0;
	}

	public long getStartTime() {
		return startTime;
	}
}
