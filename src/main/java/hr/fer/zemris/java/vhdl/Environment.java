package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.components.Model;
import hr.fer.zemris.java.vhdl.parser.Parser;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
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
	private GUI gui;

	private long startTime;
	private int counter;

	public Environment(String program) {
		HierarchyBuilder hb =
				new HierarchyBuilder(new Parser(new Lexer(program)).getProgramNode());
		model = new Model(hb.getTable(), hb.getTable().getTestedComponent());

		queue = new TimeQueue();
		model.addListener(queue);

		startTime = System.currentTimeMillis();
		simulator = new Simulator(this);

	}

	public static void main(String[] args) throws IOException {
		String program = new String(Files.readAllBytes(Paths.get("testovi/Adder_4_bit.txt")),
				StandardCharsets.UTF_8);
		Environment environment = new Environment(program);
		Thread simThread = new Thread(environment.simulator, "Simulator");
		simThread.setDaemon(true);
		simThread.start();

		SwingUtilities.invokeLater(
				() -> {
					GUI gui = new GUI(environment.model, environment.startTime);
					gui.setVisible(true);
					environment.gui = gui;
				});


		Timer timer = new Timer(100, l -> {
			long time = System.currentTimeMillis();
			environment.gui.updateGraphs(time);
			environment.counter++;

			if(environment.counter > 60000) {
				environment.gui.clearGraphs();
				environment.counter = 0;
			}
		});
		timer.start();
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
