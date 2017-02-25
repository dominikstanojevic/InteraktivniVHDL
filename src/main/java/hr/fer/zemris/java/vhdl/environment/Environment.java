package hr.fer.zemris.java.vhdl.environment;

import hr.fer.zemris.java.vhdl.hierarchy.HierarchyBuilder;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.parser.Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Dominik on 24.2.2017..
 */
public class Environment {
    private TimeQueue timeQueue;
    private Model model;
    private Simulator simulator;
    private long startTime;

    public Environment(String program) {
        HierarchyBuilder hb =
                new HierarchyBuilder(new Parser(new Lexer(program)).getProgramNode());
        model = new Model(hb.getMemory(), hb.getTable(), hb.getUut());

        timeQueue = new TimeQueue(this);
        model.addListener(timeQueue);

        startTime = System.currentTimeMillis();
        simulator = new Simulator(this);
    }

    public Model getModel() {
        return model;
    }

    public TimeQueue getTimeQueue() {
        return timeQueue;
    }


    public Simulator getSimulator() {
        return simulator;
    }

    public long getStartTime() {
        return startTime;
    }

    public static void main(String[] args) throws IOException {
        String program =
                new String(Files.readAllBytes(Paths.get("VelikiDek2.vhdl")), StandardCharsets.UTF_8);

        Environment environment = new Environment(program);

        Thread simThread = new Thread(environment.simulator, "Simulator");
        simThread.setDaemon(true);
        simThread.start();

        UI ui = new UI(environment);
        environment.getModel().addListener(ui);
        ui.run();
    }
}
