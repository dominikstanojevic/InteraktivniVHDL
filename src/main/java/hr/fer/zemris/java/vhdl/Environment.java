package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.components.Model;
import hr.fer.zemris.java.vhdl.parser.Parser;

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
		ui = new UI(this);

		model.addListener(queue);
		model.addListener(ui);
	}

	public static void main(String[] args) throws IOException {
		String program = new String(Files.readAllBytes(Paths.get("testovi/sklopI.txt")),
				StandardCharsets.UTF_8);
		Environment environment = new Environment(program);
		new Thread(environment.simulator, "Simulator").start();
		new Thread(environment.ui, "UI").start();
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
