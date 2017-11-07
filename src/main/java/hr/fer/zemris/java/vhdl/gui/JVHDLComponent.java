package hr.fer.zemris.java.vhdl.gui;

import hr.fer.zemris.java.vhdl.hierarchy.Component;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.PortType;
import hr.fer.zemris.java.vhdl.models.values.Type;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.models.values.VectorOrder;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 25.2.2017..
 */
public class JVHDLComponent extends VHDLComponent {
    private List<Signal> top = new ArrayList<>();
    private List<Signal> left = new ArrayList<>();
    private List<Signal> right = new ArrayList<>();
    private List<Signal> bottom = new ArrayList<>();

    private int boxHeight;
    private int boxWidth;

    private boolean topExists;
    private boolean leftExists;

    private static final int MIN_BOX_HEIGHT = 200;
    private static final int MIN_BOX_WIDTH = 200;
    private static final int CONNECTOR_SPACING = 10;
    private static final int SPACING = 25;
    private static final int BORDER = 10;
    private static final int WIRE_LENGTH = 50;
    private static final int FONT_SPACING = 10;

    public JVHDLComponent(Model model, Component component, Set<PositionParser.Definition> positions) {
        super(model, component, false);

        initComponent(positions);
    }

    private void initComponent(Set<PositionParser.Definition> positions) {
        int top = 0, left = 0, right = 0, bottom = 0;
        for (PositionParser.Definition def : positions) {
            switch (def.getPosition()) {
                case TOP:
                    top++;
                    continue;
                case LEFT:
                    left++;
                    continue;
                case RIGHT:
                    right++;
                    continue;
                case BOTTOM:
                    bottom++;
                    continue;
            }
        }

        calculateHeight(top, left, right, bottom);
        calculateWidth(top, left, right, bottom);

        List<Declaration> inputs =
                component.getPorts().stream().filter(p -> p.getPortType() == PortType.IN).collect(Collectors.toList());
        List<Declaration> outputs =
                component.getPorts().stream().filter(p -> p.getPortType() == PortType.OUT).collect(Collectors.toList());

        initTop(positions.stream().filter(d -> d.getPosition() == PositionParser.Position.TOP)
                .collect(Collectors.toCollection(LinkedHashSet::new)), inputs, outputs, top, left);
        initLeft(positions.stream().filter(d -> d.getPosition() == PositionParser.Position.LEFT)
                .collect(Collectors.toCollection(LinkedHashSet::new)), inputs, outputs, left, top);

        initRight(positions.stream().filter(d -> d.getPosition() == PositionParser.Position.RIGHT)
                .collect(Collectors.toCollection(LinkedHashSet::new)), inputs, outputs, right);

        initBottom(positions.stream().filter(d -> d.getPosition() == PositionParser.Position.BOTTOM)
                .collect(Collectors.toCollection(LinkedHashSet::new)), inputs, outputs, bottom);

        checkAll(inputs, outputs);
    }

    private void checkAll(List<Declaration> inputs, List<Declaration> outputs) {
        List<Declaration> all = new ArrayList<>();
        all.addAll(inputs);
        all.addAll(outputs);
        for (Declaration d : all) {
            if (d.getVectorData() == null && !added.contains(d.getLabel())) {
                throw new RuntimeException("Signal " + d.getLabel() + "don't have a position.");
            } else {
                if (!added.contains(d.getLabel())) {
                    VectorData vectorData = d.getVectorData();
                    VectorOrder order = vectorData.getOrder();
                    int start = vectorData.getStart();
                    for (int i = 0, n = vectorData.getSize(); i < n; i++) {
                        int pos = order == VectorOrder.TO ? (start + i) : (start - i);
                        if (!added.contains(d.getLabel() + pos)) {
                            throw new RuntimeException("Signal " + d.getLabel() + pos + " don't have a position.");
                        }
                    }
                }
            }
        }
    }

    private void initRight(
            Set<PositionParser.Definition> definitions, List<Declaration> inputs, List<Declaration> outputs,
            int right) {

        int heightPerInput = boxHeight / (right + 1);

        int startHeight = BORDER + heightPerInput;
        if (topExists) {
            startHeight += Signal.WIDTH + WIRE_LENGTH;
        }
        int startWidth = BORDER + boxWidth + WIRE_LENGTH;
        if (leftExists) {
            startWidth += Signal.WIDTH + WIRE_LENGTH;
        }

        for (PositionParser.Definition d : definitions) {
            readSignal(this.right, d, inputs, outputs, startWidth, startHeight, true);
            startHeight += heightPerInput;
        }
    }

    private void initLeft(
            Set<PositionParser.Definition> definitions, List<Declaration> inputs, List<Declaration> outputs, int left,
            int top) {
        int heightPerInput = boxHeight / (left + 1);

        int startHeight = BORDER + heightPerInput;
        startHeight += top == 0 ? 0 : (Signal.WIDTH + WIRE_LENGTH);
        int startWidth = BORDER;

        for (PositionParser.Definition d : definitions) {
            readSignal(this.left, d, inputs, outputs, startWidth, startHeight, true);
            startHeight += heightPerInput;
        }
    }

    private void initBottom(
            Set<PositionParser.Definition> definitions, List<Declaration> inputs, List<Declaration> outputs,
            int bottom) {
        int widthPerInput = boxWidth / (bottom + 1);

        int startWidth = BORDER + widthPerInput;
        if (leftExists) {
            startWidth += Signal.WIDTH + WIRE_LENGTH;
        }

        int startHeight = height - BORDER - Signal.WIDTH;

        for (PositionParser.Definition d : definitions) {
            readSignal(this.bottom, d, inputs, outputs, startWidth, startHeight, false);
            startWidth += widthPerInput;
        }
    }

    private void initTop(
            Set<PositionParser.Definition> definitions, List<Declaration> inputs, List<Declaration> outputs, int top,
            int left) {
        int widthPerInput = boxWidth / (top + 1);

        int startWidth = BORDER + widthPerInput;
        startWidth += left == 0 ? 0 : (Signal.WIDTH + WIRE_LENGTH);
        int startHeight = BORDER;

        for (PositionParser.Definition d : definitions) {
            readSignal(this.top, d, inputs, outputs, startWidth, startHeight, false);
            startWidth += widthPerInput;
        }

    }

    private void readSignal(
            List<Signal> position, PositionParser.Definition d, List<Declaration> inputs, List<Declaration> outputs,
            int width, int height, boolean horizontal) {
        Optional<Declaration> signal = inputs.stream().filter(s -> s.getLabel().equals(d.getSignal())).findFirst();
        if (signal.isPresent()) {
            addSignal(this.inputs, position, horizontal, signal.get(), d, width, height);
            return;
        }

        signal = outputs.stream().filter(s -> s.getLabel().equals(d.getSignal())).findFirst();
        if (signal.isPresent()) {
            addSignal(this.outputs, position, horizontal, signal.get(), d, width, height);
            return;
        }

        throw new RuntimeException("Invalid definition. Signal: " + d.getSignal() + " " + "does not exist.");
    }

    private void addSignal(
            List<Signal> list, List<Signal> position, boolean b, Declaration signal, PositionParser.Definition d,
            int startWidth, int startHeight) {
        if (d.getAccess() == null) {
            int address = component.getAddresses(signal)[0];
            Signal s = new Signal(signal.getLabel(), address, startWidth, startHeight, b);
            list.add(s);
            position.add(s);

            added.add(signal.getLabel());
        } else {
            if (signal.getType() != Type.VECTOR_STD_LOGIC) {
                throw new RuntimeException("Invalid definition for signal: " + signal.getLabel());
            }

            VectorData data = signal.getVectorData();
            int index = data.getOrder() == VectorOrder.TO ?
                    d.getAccess() - data.getStart() :
                    data.getStart() - d.getAccess();
            int address = component.getAddresses(signal)[index];

            String name = signal.getLabel() + d.getAccess();
            Signal s = new Signal(name, address, startWidth, startHeight, b);
            list.add(s);
            position.add(s);

            added.add(name);
        }
    }

    private Set<String> added = new HashSet<>();

    private void calculateWidth(int top, int left, int right, int bottom) {
        int box = getBoxSize(top, bottom);
        boxWidth = box < MIN_BOX_WIDTH ? MIN_BOX_WIDTH : box;

        width = calculateSize(left, right, boxWidth);
        leftExists = left != 0 ? true : false;
    }

    private void calculateHeight(int top, int left, int right, int bottom) {
        int box = getBoxSize(left, right);
        boxHeight = box < MIN_BOX_HEIGHT ? MIN_BOX_HEIGHT : box;

        height = calculateSize(top, bottom, boxHeight);
        topExists = top != 0 ? true : false;
    }

    private int getBoxSize(int a, int b) {
        int max = Math.max(a, b);
        return 2 * SPACING + max * (Signal.WIDTH + CONNECTOR_SPACING) + CONNECTOR_SPACING;
    }

    private int calculateSize(int a, int b, int box) {
        int aExists = a == 0 ? 0 : 1;
        int bExists = b == 0 ? 0 : 1;

        return 2 * BORDER + (aExists + bExists) * (Signal.WIDTH + WIRE_LENGTH) + box;
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setFont(FONT);
        ((Graphics2D) g)
                .setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int boxStartX = BORDER;
        if (leftExists) {
            boxStartX += Signal.WIDTH + WIRE_LENGTH;
        }
        int boxStartY = BORDER;
        if (topExists) {
            boxStartY += Signal.WIDTH + WIRE_LENGTH;
        }
        g.drawRect(boxStartX, boxStartY, boxWidth, boxHeight);

        FontMetrics fm = g.getFontMetrics();

        paintTop(g, fm);
        paintLeft(g, fm);
        paintRight(g, fm);
        paintBottom(g, fm);
    }

    private void paintTop(Graphics g, FontMetrics fm) {
        int fontHeight = fm.getHeight();

        for (Signal t : top) {
            t.paint(g);

            int lineWidth = t.getStartX() + Signal.HEIGHT / 2;
            int lineHeight = t.getStartY() + Signal.WIDTH;
            g.drawLine(lineWidth, lineHeight, lineWidth, lineHeight + WIRE_LENGTH);

            g.drawString(t.toString(), lineWidth - fm.stringWidth(t.toString()) / 2,
                    lineHeight + WIRE_LENGTH + FONT_SPACING + fontHeight / 2);
        }
    }

    private void paintBottom(Graphics g, FontMetrics fm) {
        int fontHeight = fm.getHeight() / 2;

        for (Signal b : bottom) {
            b.paint(g);

            int lineWidth = b.getStartX() + Signal.HEIGHT / 2;
            int lineHeight = b.getStartY();
            g.drawLine(lineWidth, lineHeight, lineWidth, lineHeight - WIRE_LENGTH);

            g.drawString(b.toString(), lineWidth - fm.stringWidth(b.toString()) / 2,
                    lineHeight - WIRE_LENGTH - FONT_SPACING - fontHeight / 2);
        }
    }

    private void paintLeft(Graphics g, FontMetrics fm) {
        int fontHeight = fm.getHeight() / 2;

        int wireStart = BORDER + Signal.WIDTH;
        for (Signal l : left) {
            l.paint(g);
            int y = l.getStartY() + Signal.HEIGHT / 2;
            g.drawLine(wireStart, y, wireStart + WIRE_LENGTH, y);
            g.drawString(l.toString(), wireStart + WIRE_LENGTH + FONT_SPACING, y + fontHeight / 2);
        }
    }

    private void paintRight(Graphics g, FontMetrics fm) {
        int fontHeight = fm.getHeight() / 2;

        int wireStart = BORDER + boxWidth;
        if (leftExists) {
            wireStart += Signal.WIDTH + WIRE_LENGTH;
        }
        for (Signal r : right) {
            r.paint(g);
            int y = r.getStartY() + Signal.HEIGHT / 2;
            g.drawLine(wireStart, y, wireStart + WIRE_LENGTH, y);
            int end = (int) (wireStart - FONT_SPACING - fm.getStringBounds(r.toString(), g).getWidth());
            g.drawString(r.toString(), end, y + fontHeight / 2);
        }
    }
}
