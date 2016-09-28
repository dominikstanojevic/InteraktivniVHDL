package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.AbstractJob;
import hr.fer.zemris.java.vhdl.models.SimulationStatement;
import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.util.List;

/**
 * Created by Dominik on 19.8.2016..
 */
public class Simulator implements Runnable {
	private Environment environment;

	public Simulator(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void run() {
		while (true) {
			environment.getQueue().getEvent().execute(environment);
		}
	}

	public void execute(Signal signal, long time) {
		Table table = environment.getModel().getTable();
		List<SimulationStatement> statements = table.getStatementsForSignal(signal);

		statements.forEach(s -> {
			Signal sig = s.getSignal(table);
			if (sig == null) {
				return;
			}

			Value value = s.execute(table);
			Integer position = s.getPosition(table);
			long delay = s.getDelay();


			environment.getQueue().addEvent(new Assign(sig, value, position, time, delay));

		});
	}

	private boolean checkEquality(Signal signal, Value value, Integer position) {
		if (position == null) {
			return signal.getValue().equals(value);
		}

		return ((Vector) signal.getValue()).getLogicValue(position).equals(value);
	}

	public class Assign extends AbstractJob {
		private Signal signal;
		private Value value;
		private Integer position;

		public Assign(
				Signal signal, Value value, Integer position, long currentTime, long delay) {
			super(currentTime, delay);
			this.signal = signal;
			this.position = position;
			this.value = value;
		}

		@Override
		public void execute(Environment environment) {
			if (!checkEquality(signal, value, position)) {
				environment.getModel().signalChange(signal, value, position, startTime);
			}
		}
	}
}
