package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.DeclarationTable;
import hr.fer.zemris.java.vhdl.parser.Parser;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.Signal;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Dominik on 19.8.2016..
 */
public class Simulator {
	private DeclarationTable table;
	private ProgramNode programNode;

	public Simulator(
			DeclarationTable table, ProgramNode programNode) {
		this.table = table;
		this.programNode = programNode;
	}

	public void calculate(List<Signal> signals, long time) {
		Set<Statement> statements = new HashSet<>();
		signals.forEach(s -> statements
				.addAll(programNode.getArchitecture().getStatementForSignal(s)));

		statements.forEach(Statement::execute);

		List<Signal> changed = new ArrayList<>();
		statements.forEach(s -> {
			Signal signal = s.assign();
			if (signal != null && signal.getSignalType() != Signal.Type.OUT) {
				changed.add(signal);
			}
		});

		if (changed.size() != 0) {
			calculate(changed, time);
		}
	}

	public static void main(String[] args) throws IOException {
		String program =
				new String(Files.readAllBytes(Paths.get("test.txt")), StandardCharsets.UTF_8);

		Parser parser = new Parser(new Lexer(program));
		Simulator simulator = new Simulator(parser.getTable(), parser.getProgramNode());

		Scanner sc = new Scanner(System.in);

		System.out.println("Input signals: ");

		simulator.table.getInputSignals().forEach(s -> System.out.print(s.getId() + " "));
		System.out.println();

		while (true) {
			String line = sc.nextLine().trim();
			List<Signal> signals = parseSignals(simulator, line);
			simulator.calculate(signals, System.currentTimeMillis());

			System.out.println("Current values");
			simulator.table.getSignals().forEach(s -> System.out
					.println("Signal: " + s.getId() + ", value: " + s.getValue()));
		}
	}

	private static List<Signal> parseSignals(Simulator simulator, String line) {
		List<Signal> signalsList = new ArrayList<>();
		String[] signals = line.split(",");

		for (String signal : signals) {
			signal = signal.trim();
			String[] data = signal.split(":");

			Signal s = simulator.table.getSignal(data[0].trim());
			s.setValue(LogicValue.getValue(data[1].trim().charAt(0)));
			signalsList.add(s);
		}

		return signalsList;
	}
}
