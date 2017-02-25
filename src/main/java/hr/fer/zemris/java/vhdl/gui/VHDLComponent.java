package hr.fer.zemris.java.vhdl.gui;

import hr.fer.zemris.java.vhdl.hierarchy.Component;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.PortType;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Type;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.models.values.VectorOrder;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 25.2.2017..
 */
public class VHDLComponent extends JComponent {
    protected Model model;
    protected Component component;

    protected List<Signal> inputs = new ArrayList<>();
    protected List<Signal> outputs = new ArrayList<>();

    protected int width;
    protected int height;

    public static final int CONNECTOR_SPACING = 10;
    public static final int SPACING = 25;
    public static final Font FONT = new Font("SansSerif", Font.PLAIN, 20);

    public VHDLComponent(Model model, Component component) {
        this(model, component, true);
    }

    protected VHDLComponent(Model model, Component component, boolean init) {
        this.model = model;
        this.component = component;

        if (init) {
            width = 600;
            initComponent();
        }
    }

    private void initComponent() {
        Set<Declaration> ports = component.getPorts();

        int numberOfInputs = 0;
        int numberOfOutputs = 0;
        for (Declaration d : ports) {
            if (d.getPortType() == PortType.IN) {
                numberOfInputs += d.size();
            } else {
                numberOfOutputs += d.size();
            }
        }

        int max = Math.max(numberOfInputs, numberOfOutputs);
        height = max * (Signal.HEIGHT + CONNECTOR_SPACING) + CONNECTOR_SPACING + 6 * SPACING;

        initInputs(ports.stream().filter(d -> d.getPortType() == PortType.IN).collect(Collectors.toList()),
                numberOfInputs);
        initOutputs(ports.stream().filter(d -> d.getPortType() == PortType.OUT).collect(Collectors.toList()),
                numberOfOutputs);
    }

    private void initOutputs(List<Declaration> outputs, int numberOfOutputs) {
        int boxHeight = height - 2 * SPACING;
        int heightPerInput = boxHeight / (numberOfOutputs + 1);

        int startHeight = getInsets().top + SPACING + heightPerInput;
        for (Declaration output : outputs) {
            List<Signal> o = createSignals(output, 450, startHeight, true, heightPerInput);
            this.outputs.addAll(o);
            startHeight += o.size() * heightPerInput;
        }
    }

    private void initInputs(List<Declaration> inputs, int numberOfInputs) {
        int boxHeight = height - 2 * SPACING;
        int heightPerInput = boxHeight / (numberOfInputs + 1);

        int startHeight = getInsets().top + heightPerInput + SPACING;
        for (Declaration input : inputs) {
            List<Signal> i = createSignals(input, 50, startHeight, true, heightPerInput);
            this.inputs.addAll(i);
            startHeight += i.size() * heightPerInput;
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    private static final int BORDER = 50;
    private static final int BOX_WIDTH = 200;
    private static final int WIRE_LENGTH = 50;
    private static final int FONT_SPACING = 10;

    @Override
    public void paintComponent(Graphics g) {
        g.setFont(FONT);
        ((Graphics2D) g)
                .setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int startY = SPACING;
        int height = this.height - 2 * SPACING;

        int boxStartX = BORDER + Signal.WIDTH + WIRE_LENGTH;
        g.drawRect(boxStartX, startY, BOX_WIDTH, height);

        FontMetrics fm = g.getFontMetrics();
        int fontHeight = fm.getHeight() / 2;

        int wireStart = BORDER + Signal.WIDTH;
        for (Signal signal : inputs) {
            signal.paint(g);
            int y = signal.startY + Signal.HEIGHT / 2;
            g.drawLine(wireStart, y, boxStartX, y);
            g.drawString(signal.toString(), boxStartX + FONT_SPACING, y + fontHeight / 2);
        }

        int boxEndX = boxStartX + BOX_WIDTH;
        for (Signal output : outputs) {
            output.paint(g);
            int y = output.startY + Signal.HEIGHT / 2;
            String s = output.toString();
            int width = fm.stringWidth(s);
            g.drawLine(boxEndX, y, boxEndX + WIRE_LENGTH, y);
            int end = (int) (boxEndX - FONT_SPACING - fm.getStringBounds(s, g).getWidth());
            g.drawString(s, end, y + fontHeight / 2);
        }
    }

    public Optional<Signal> getInputForPoint(double x, double y) {
        return inputs.stream().filter(i -> i.isInside(x, y)).findFirst();
    }

    public List<Signal> createSignals(
            Declaration declaration, int width, int height, boolean horizontal, int heightPerInput) {
        List<Signal> signals = new ArrayList<>();
        Integer[] addresses = component.getAddresses(declaration);
        VectorData data = declaration.getVectorData();
        for (int i = 0, start = data.getStart(); i < addresses.length; i++) {
            int index = data.getOrder() == VectorOrder.TO ? start + i : start - i;
            signals.add(
                    new Signal(declaration.getLabel() + (declaration.getType() == Type.VECTOR_STD_LOGIC ? index : ""),
                            addresses[i], width, height, horizontal));
            height += heightPerInput;
        }
        return signals;
    }

    public class Signal {
        private String name;
        private int address;
        private int startX;
        private int startY;
        private boolean horizontal;

        public static final int HEIGHT = 50;
        public static final int WIDTH = 100;

        public Signal(String name, int address, int startX, int startY, boolean horizontal) {
            this.name = name;
            this.address = address;
            this.startX = startX;
            this.startY = startY;
            this.horizontal = horizontal;

            if (horizontal) {
                this.startY -= HEIGHT / 2;
            } else {
                this.startX -= HEIGHT / 2;
            }
        }

        public boolean isInside(double x, double y) {
            if (x < startX) {
                return false;
            }
            if (y < startY) {
                return false;
            }
            if (x > startX + (horizontal ? WIDTH : HEIGHT)) {
                return false;
            }
            if (y > startY + (horizontal ? HEIGHT : WIDTH)) {
                return false;
            }

            return true;
        }

        public LogicValue getValue() {
            return model.getValue(address);
        }

        public void paint(Graphics g) {
            g.setColor(Color.lightGray);
            if (horizontal) {
                g.fillRect(startX, startY, WIDTH, HEIGHT);
            } else {
                g.fillRect(startX, startY, HEIGHT, WIDTH);
            }
            g.setColor(Color.black);

            FontMetrics fm = g.getFontMetrics();
            int fontWidth = fm.stringWidth(getValue().toString());
            int fontHeight = fm.getHeight() / 2;
            int startHeight = startY + ((horizontal ? HEIGHT : WIDTH) + fontHeight) / 2;
            int startWidth = startX + ((horizontal ? WIDTH : HEIGHT) - fontWidth) / 2;

            g.drawString(getValue().toString(), startWidth, startHeight);
        }

        @Override
        public String toString() {
            return name;
        }

        public int getStartX() {
            return startX;
        }

        public int getStartY() {
            return startY;
        }

        public int getAddress() {
            return address;
        }
    }

    public List<Signal> getSignals() {
        List<Signal> signals = new ArrayList<>();
        signals.addAll(inputs);
        signals.addAll(outputs);

        return signals;
    }
}
