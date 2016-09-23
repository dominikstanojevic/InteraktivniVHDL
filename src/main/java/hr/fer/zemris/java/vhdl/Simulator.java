package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.AbstractJob;
import hr.fer.zemris.java.vhdl.models.SimulationStatement;
import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;

import java.util.List;

/**
 * Created by Dominik on 19.8.2016..
 */
public class Simulator implements Runnable{
	private Environment environment;

	public Simulator(Environment environment) {
		this.environment = environment;
	}


	@Override
	public void run() {
		while(true) {
			environment.getQueue().getEvent().execute(environment);
		}
	}

	public void execute(Signal signal, long time) {
		Table table = environment.getModel().getTable();
		List<SimulationStatement> statements = table.getStatementsForSignal(signal);

		statements.forEach(s -> {
			if(s.execute(table)) {
				environment.getQueue().addEvent(new Assign(s, time, s.getDelay()));
			}
		});
	}

	public void assign(SimulationStatement statement, long time) {
		statement.assign(environment.getModel().getTable());
		Signal signal = statement.getSignal(environment.getModel().getTable());
		environment.getModel().signalChange(signal, time);
	}

	public class Assign extends AbstractJob {
		private SimulationStatement statement;

		public Assign(SimulationStatement statement, long currentTime, long delay) {
			super(currentTime, delay);
			this.statement = statement;
		}

		@Override
		public void execute(Environment environment) {
			environment.getSimulator().assign(statement, startTime);
		}
	}
}
