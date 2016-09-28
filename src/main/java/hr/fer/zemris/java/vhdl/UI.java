package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.IUserInterface;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.declarations.PortDeclaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import java.util.Scanner;

/**
 * Created by Dominik on 23.9.2016..
 */
public class UI implements IUserInterface, Runnable {
	private Environment environment;
	private long startTime;

	public UI(Environment environment) {
		this.environment = environment;
		startTime = System.currentTimeMillis();
	}

	@Override
	public void signalChanged(Signal signal, long time) {
		System.out.println(
				"Time: " + (time - startTime) + ", signal: " + signal.getName() + ", value: " +
				signal.getValue());
	}

	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);

		System.out.println("Input signals: ");

		environment.getModel().getComponent().getPorts().stream()
				.filter(e -> e.getDeclaration().getPortType() == PortDeclaration.Type.IN)
				.forEach(e -> System.out
						.println(e.getName() + ": type " + e.getDeclaration().getTypeOf()));

		while (true) {
			String line = sc.nextLine().trim();
			parseSignals(line);
		}
	}

	private void parseSignals(String line) {
		String[] data = line.split(":");

		String s = data[0].trim();
		Signal sig = environment.getModel().getTable().getSignal("", s);
		Value value;
		if (sig.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC) {
			value = LogicValue.getValue(data[1].trim().charAt(0));
		} else {
			value = Vector.createVector(data[1].trim().toCharArray());
		}

		environment.getModel().signalChange(s, value, null, System.currentTimeMillis());
	}
}
