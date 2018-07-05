package hr.fer.zemris.java.vhdl.environment;

import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.jobs.AbstractJob;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.AddressStatement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dominik on 24.2.2017..
 */
public class Simulator implements Runnable {
    private Environment environment;

    public Simulator(Environment environment) {
        this.environment = environment;

        Table table = environment.getModel().getTable();
        Set<AddressStatement> statements = new HashSet<>();
        table.getStatements().values().forEach(statements::addAll);

        statements.forEach(s -> assign(s.getAddress(), s.getExpression().evaluate(environment.getModel()), environment.getStartTime()));
    }

    @Override
    public void run() {
        TimeQueue queue = environment.getTimeQueue();
        while (true) {
            queue.getEvent().execute();
        }
    }

    public void execute(int address, long time) {
        Table table = environment.getModel().getTable();

        Set<AddressStatement> statements = table.getStatementsForAddress(address);

        TimeQueue queue = environment.getTimeQueue();
        statements.forEach(s -> queue.addEvent(new Assign(time + 1, s.getDelay(), s.getAddress(),
                s.getExpression().evaluate(environment.getModel()))));
    }

    public void assign(Integer[] addresses, LogicValue[] values, long time) {
        if (values.length != addresses.length) {
            throw new RuntimeException("Invalid number of assigned elements.");
        }

        Model model = environment.getModel();

        for (int i = 0; i < addresses.length; i++) {
            if (addresses[i] == null) {
                continue;
            }

            if (model.getValue(addresses[i]) != values[i]) {
                model.signalChanged(addresses[i], values[i], time);
            }
        }
    }

    public class Assign extends AbstractJob {
        private Integer[] addresses;
        private LogicValue[] values;

        public Assign(long currentTime, long delay, Integer[] addresses, LogicValue[] values) {
            super(currentTime, delay);
            this.addresses = addresses;
            this.values = values;
        }

        @Override
        public void execute() {
            assign(addresses, values, startTime);
        }
    }
}
