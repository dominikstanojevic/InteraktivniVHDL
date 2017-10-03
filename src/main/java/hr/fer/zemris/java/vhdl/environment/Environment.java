package hr.fer.zemris.java.vhdl.environment;

import hr.fer.zemris.java.vhdl.gui.GUI;
import hr.fer.zemris.java.vhdl.gui.PositionParser;
import hr.fer.zemris.java.vhdl.hierarchy.Component;
import hr.fer.zemris.java.vhdl.hierarchy.HierarchyBuilder;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.parser.Parser;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 24.2.2017..
 */
public class Environment {
    private TimeQueue timeQueue;
    private Model model;
    private Simulator simulator;
    private long startTime;
    private GUI gui;
    private int counter;

    public Environment(String program, Map<String, String> signals) {
        HierarchyBuilder hb = new HierarchyBuilder(new Parser(new Lexer(program)).getProgramNode());
        model = new Model(hb.getMemory(), hb.getTable(), hb.getUut());
        startTime = System.currentTimeMillis();
        if (signals != null) {
            setInit(signals);
        }

        timeQueue = new TimeQueue(this);
        model.addListener(timeQueue);

        simulator = new Simulator(this);
    }

    public static void main(String[] args) throws IOException {
        int argsLength = args.length;

        if (argsLength <= 0) {
            JOptionPane.showMessageDialog(null, "Invalid number of arguments: " + argsLength, "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String path;
        Map<String, String> initial = null;
        if (argsLength == 1) {
            path = args[0];
        } else {
            path = args[1];
            initial = initSignals(args[0]);
        }

        String program = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);

        Environment environment = new Environment(program, initial);

        Thread simThread = new Thread(environment.simulator, "Simulator");
        simThread.setDaemon(true);
        simThread.start();

        Set<PositionParser.Definition> def = readPositions(path.substring(0, path
                .lastIndexOf(".")), environment.getModel().getTable());


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
        if (!string.startsWith("-init(") || !string.endsWith(")")) {
            JOptionPane.showMessageDialog(null, "Invalid initialization argument.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        string = string.replace("-init(", "");
        string = string.replace(")", "");

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

    private static Set<PositionParser.Definition> readPositions(String filename, Table table)
            throws
            IOException {
        Path path = Paths.get(filename + ".sim");
        if (!Files.isRegularFile(path)) {
            return null;
        }

        String program = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        PositionParser pp = new PositionParser(program, table);

        return pp.getDefinitions();
    }

    private void setInit(Map<String, String> signals) {
        Component uut = model.getUut();

        String defaultValue = null;

        for (Map.Entry<String, String> entry : signals.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            if (name.equals("*")) {
                defaultValue = value;
                continue;
            }

            Declaration port = uut.getPort(name);
            if (port == null) {
                throw new RuntimeException("Invalid port name. " + name + " does not exist.");
            }

            Integer[] addresses = uut.getAddresses(port);
            if (addresses.length != value.length()) {
                throw new RuntimeException("Invalid number of logical values for port " + name + ".");
            }

            for (int i = 0; i < addresses.length; i++) {
                model.signalChanged(addresses[i], LogicValue.getValue(value.charAt(i)), startTime);
            }
        }

        if(defaultValue != null) {
            Set<Declaration> ports = uut.getPorts();
            Set<Declaration> defaultPorts = ports.stream().filter(d -> !signals.keySet().contains(d.getLabel())).collect(Collectors.toSet());

            for (Declaration port : defaultPorts) {
                Integer[] addresses = uut.getAddresses(port);

                for (int i = 0; i < addresses.length; i++) {
                    model.signalChanged(addresses[i], LogicValue.getValue(defaultValue.charAt(0)), startTime);
                }
            }
        }
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
}
