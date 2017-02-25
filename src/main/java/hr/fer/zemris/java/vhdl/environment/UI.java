package hr.fer.zemris.java.vhdl.environment;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.PortType;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.models.values.VectorOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Dominik on 24.2.2017..
 */
public class UI implements Runnable, IModelListener {
    private Environment environment;
    private long startTime;

    private Map<Integer, String> signals = new HashMap<>();

    public UI(Environment environment) {
        this.environment = environment;
        startTime = environment.getStartTime();

        Set<Declaration> declarations = environment.getModel().getUut().getPorts();
        for (Declaration declaration : declarations) {
            VectorData vectorData = declaration.getVectorData();
            Integer[] addresses = environment.getModel().getUut().getAddresses(declaration);
            for (int i = 0, start = vectorData.getStart(); i < vectorData.getSize(); i++) {
                int index = vectorData.getOrder() == VectorOrder.TO ? start + i : start - i;
                signals.put(addresses[i], declaration.getLabel() + index);
            }
        }
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Signal signals: ");

        environment.getModel().getUut().getPorts().stream().filter(p -> p.getPortType() == PortType.IN)
                .forEach(e -> System.out.println(e.getLabel() + ": type " + e.getType()));

        while (true) {
            String line = sc.nextLine().trim();
            parseSignals(line);
        }
    }

    private void parseSignals(String line) {
        String[] data = line.split(":");

        String s = data[0].trim();
        Declaration sig =
                environment.getModel().getUut().getPorts().stream().filter(p -> p.getLabel().equals(s)).findFirst()
                        .get();
        Integer[] addresses = environment.getModel().getUut().getAddresses(sig);
        long currTime = System.currentTimeMillis();
        for (int i = 0; i < addresses.length; i++) {
            LogicValue value = LogicValue.getValue(data[1].trim().charAt(i));
            environment.getModel().signalChanged(addresses[i], value, currTime);
        }
    }

    @Override
    public void signalChanged(int address, long time) {
        String name = signals.get(address);
        if (name != null) {
            System.out.println("Time: " + (time - startTime) + ", signal: " + signals.get(address) + ", value: " +
                               environment.getModel().getValue(address));
        }
    }
}
