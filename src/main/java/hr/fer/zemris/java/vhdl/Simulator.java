package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.SimulationStatement;
import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.Parser;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;

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
	private ProgramNode tested;

	public Simulator(Table table, ProgramNode testedUnit) {
		this.table = table;
		this.tested = testedUnit;
	}

	public static void main(String[] args) throws IOException {
		String program =
				new String(Files.readAllBytes(Paths.get("sklop.txt")), StandardCharsets.UTF_8);

		HierarchyBuilder hb =
				new HierarchyBuilder(new Parser(new Lexer(program)).getProgramNode());
		Simulator simulator = new Simulator(hb.getTable(), hb.getTested());

		Scanner sc = new Scanner(System.in);

		System.out.println("Input signals: ");

		simulator.tested.getDeclarationTable().getInputSignals()
				.forEach(s -> System.out.print(s + " "));
		System.out.println();

		while (true) {
			String line = sc.nextLine().trim();
			List<String> signals = parseSignals(simulator, line);
			simulator.calculate(signals, System.currentTimeMillis());

			System.out.println("Current values");
			simulator.table.getSignals()
					.forEach((s, value) -> System.out.println(s + ": " + value));
		}
	}

	public void calculate(List<String> signals, long time) {
		List<SimulationStatement> statements = table.getStatements().stream().filter(s -> s
				.sensitiveForSignals(table, signals)).collect(Collectors.toList());

		List<String> changed = new ArrayList<>();
		statements.forEach(s -> {
			if(s.execute(table)) {
				changed.add(s.getSignal(table));
			}
		});
		statements.forEach(s -> s.assign(table));

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
			simulator.table.setValueForSignal("", s, LogicValue.getValue(data[1].trim().charAt(0)));
			signalsList.add("/" + s);
		}

		return signalsList;
	}

}
