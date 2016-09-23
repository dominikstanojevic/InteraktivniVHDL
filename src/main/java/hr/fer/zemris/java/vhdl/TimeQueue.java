package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.AbstractJob;
import hr.fer.zemris.java.vhdl.models.components.IModelListener;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;

import java.util.concurrent.DelayQueue;

/**
 * Created by Dominik on 23.9.2016..
 */
public class TimeQueue implements IModelListener{
	private DelayQueue<AbstractJob> queue = new DelayQueue<>();

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
	public void signalChanged(Signal signal, long time) {
		queue.add(new Execute(signal, time));
	}

	public static class Execute extends AbstractJob {
		private Signal signal;

		public Execute(Signal signal, long currentTime) {
			super(currentTime);
			this.signal = signal;
		}

		@Override
		public void execute(Environment environment) {
			environment.getSimulator().execute(signal, startTime);
		}
	}
}
