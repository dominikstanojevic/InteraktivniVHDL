package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.components.Model;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.Parser;
import hr.fer.zemris.java.vhdl.parser.PositionParser;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Dominik on 23.9.2016..
 */
public class Environment {
	private Simulator simulator;
	private TimeQueue queue;
	private Model model;
	private GUI gui;

	private long startTime;
	private int counter;

	public Environment(String program, Map<String, String> initial) {
		HierarchyBuilder hb =
				new HierarchyBuilder(new Parser(new Lexer(program)).getProgramNode());
		model = new Model(hb.getTable(), hb.getTable().getTestedComponent());

		queue = new TimeQueue();
		model.addListener(queue);

		startTime = System.currentTimeMillis();
		simulator = new Simulator(this);
		if (initial != null) {
			initializeSignals(initial);
		}

	}

	private void initializeSignals(Map<String, String> initial) {
		List<Signal> inputs = model.getTable().getTestedComponent().getInputSignals();

		for (Map.Entry<String, String> entry : initial.entrySet()) {
			Optional<Signal> optional = inputs.stream()
					.filter(signal -> signal.getName().equals(entry.getKey()))
					.findFirst();

			if (!optional.isPresent()) {
				inputNotExist(entry.getKey());
			}

			Signal s = optional.get();
			String value = entry.getValue();

			if (s.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC) {
				if (value.length() != 1) {
					invalidValueForSignal(s, value);
					System.exit(-1);
				} else {
					model.signalChange(s, LogicValue.getValue(value.charAt(0)), null,
							startTime);
				}
			} else {
				int size = s.getDeclaration().size();
				if (size != value.length()) {
					invalidValueForSignal(s, value);
				} else {
					model.signalChange(s, Vector.createVector(value.toCharArray()), null,
							startTime);
				}
			}

		}
	}

	private void inputNotExist(String key) {
		JOptionPane.showMessageDialog(null, "Signal: " + key + " doesn't exist.", "Error",
				JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}

	private void invalidValueForSignal(Signal s, String value) {
		JOptionPane
				.showMessageDialog(null, "Invalid value " + value + " for signal: " + s + " .",
						"Error", JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}

	private static Set<PositionParser.Definition> readPositions(String filename, Table table)
			throws
			IOException {
		Path path = Paths.get(filename + ".sim");
		if(!Files.isRegularFile(path)) {
			return null;
		}

		String program = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		PositionParser pp = new PositionParser(program, table);

		return pp.getDefinitions();
	}

	public static void main(String[] args) throws IOException {
		int argsLength = args.length;

		if (argsLength <= 0) {
			JOptionPane.showMessageDialog(null, "Invalid number of arguments: " + argsLength,
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String path;
		Map<String, String> inital = null;
		if (argsLength == 1) {
			path = args[0];
		} else {
			path = args[1];
			inital = initSignals(args[0]);
		}

		String program =
				new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

		Environment environment = new Environment(program, inital);

		Set<PositionParser.Definition> def = readPositions(path.substring(0, path
				.lastIndexOf(".")), environment.getModel().getTable());


		Thread simThread = new Thread(environment.simulator, "Simulator");
		simThread.setDaemon(true);
		simThread.start();



		SwingUtilities.invokeLater(() -> {
			GUI gui = new GUI(environment.model, environment.startTime, def);
			gui.setVisible(true);
			environment.gui = gui;
		});

		Timer timer = new Timer(100, l -> {
			long time = System.currentTimeMillis();
			environment.gui.updateGraphs(time);
			environment.counter++;

			if (environment.counter > 60000) {
				environment.gui.clearGraphs();
				environment.counter = 0;
			}
		});
		timer.start();
	}

	private static Map<String, String> initSignals(String string) {
		if (!string.startsWith("-init{") || !string.endsWith("}")) {
			JOptionPane.showMessageDialog(null, "Invalid initialization argument.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		string = string.replace("-init{", "");
		string = string.replace("}", "");

		Map<String, String> values = new HashMap<>();
		String[] signals = string.split(",");

		for (String sig : signals) {
			String[] data = sig.split("=");

			String signal = data[0].trim();
			String value = data[1].trim();

			values.put(signal, value);
		}

		return values;
	}

	private static String processArgs(String[] args) {
		return null;
	}

	public Simulator getSimulator() {
		return simulator;
	}

	public TimeQueue getQueue() {
		return queue;
	}

	public Model getModel() {
		return model;
	}

	public long getStartTime() {
		return startTime;
	}
}
