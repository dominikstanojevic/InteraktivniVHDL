package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.Component;
import hr.fer.zemris.java.vhdl.models.SimulationStatement;
import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.declarations.PortDeclaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 19.8.2016..
 */
public class Simulator {
	private Table table;
	private Component tested;

	public Simulator(Table table) {
		this.table = table;
		this.tested = table.getTestedComponent();
	}

	public static void main(String[] args) throws IOException {
		String program =
				new String(Files.readAllBytes(Paths.get("test2.txt")), StandardCharsets.UTF_8);

		HierarchyBuilder hb =
				new HierarchyBuilder(new Parser(new Lexer(program)).getProgramNode());
		Simulator simulator = new Simulator(hb.getTable());
		simulator.table.getStatements().forEach(s -> {
			s.execute(simulator.table);
			s.assign(simulator.table);
		});

		Scanner sc = new Scanner(System.in);

		System.out.println("Input signals: ");

		simulator.tested.getPorts().stream()
				.filter(e -> e.getDeclaration().getPortType() == PortDeclaration.Type.IN)
				.forEach(e -> System.out
						.println(e.getName() + ": type " + e.getDeclaration().getTypeOf()));

		while (true) {
			String line = sc.nextLine().trim();
			List<String> signals = parseSignals(simulator, line);
			simulator.calculate(signals, System.currentTimeMillis());

			System.out.println("Current values");
			simulator.table.getSignals().forEach(
					(s, value) -> System.out.println(value + ": " + value.getValue()));
		}
	}

	public void calculate(List<String> signals, long time) {
		List<SimulationStatement> statements = table.getStatements().stream()
				.filter(s -> s.sensitiveForSignals(table, signals))
				.collect(Collectors.toList());

		List<String> changed = new ArrayList<>();
		statements.forEach(s -> {
			if (s.execute(table)) {
				changed.add(s.getSignal(table).getName());
				s.assign(table);
			}
		});


		if (changed.size() != 0) {
			calculate(changed, time);
		}
	}

	private static List<String> parseSignals(Simulator simulator, String line) {
		List<String> signalsList = new ArrayList<>();
		String[] signals = line.split(",");

		for (String signal : signals) {
			signal = signal.trim();
			String[] data = signal.split(":");

			String s = data[0].trim();
			Signal sig = simulator.table.getSignal("", s);
			if (sig.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC) {
				sig.setValue(LogicValue.getValue(data[1].trim().charAt(0)));
			} else {
				sig.setValue(Vector.createVector(data[1].trim().toCharArray()));
			}
			signalsList.add("/" + s);
		}

		return signalsList;
	}

}
