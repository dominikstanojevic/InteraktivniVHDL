package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.components.Model;
import hr.fer.zemris.java.vhdl.parser.Parser;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Dominik on 23.9.2016..
 */
public class Environment {
	private Simulator simulator;
	private TimeQueue queue;
	private Model model;
	private UI ui;

	public Environment(String program) {
		HierarchyBuilder hb =
				new HierarchyBuilder(new Parser(new Lexer(program)).getProgramNode());
		simulator = new Simulator(this);
		queue = new TimeQueue();
		model = new Model(hb.getTable(), hb.getTable().getTestedComponent());

		model.addListener(queue);
	}

	public static void main(String[] args) throws IOException {
		String program = new String(Files.readAllBytes(Paths.get("testovi/Adder_4_bit.txt")),
				StandardCharsets.UTF_8);
		Environment environment = new Environment(program);
		Thread simThread = new Thread(environment.simulator, "Simulator");
		simThread.setDaemon(true);
		simThread.start();
		SwingUtilities.invokeLater(() -> new GUI(environment.model).setVisible(true));
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

	public UI getUi() {
		return ui;
	}
}
