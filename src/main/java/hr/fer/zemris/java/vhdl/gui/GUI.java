package hr.fer.zemris.java.vhdl.gui;

import hr.fer.zemris.java.vhdl.environment.IModelListener;
import hr.fer.zemris.java.vhdl.hierarchy.Component;
import hr.fer.zemris.java.vhdl.hierarchy.Model;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import org.jfree.chart.ChartPanel;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.plaf.LayerUI;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GUI extends JFrame implements IModelListener {
    private Model model;
    private VHDLComponent blackBox;
    private JLayer<JComponent> layer;
    private JScrollPane scrollPane;
    private List<Graph> graphs;

    public GUI(Model model, long startTime, Set<PositionParser.Definition> positions) {
        this.model = model;
        model.addListener(this);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("InteraktivniVHDL (Autor: Dominik Stanojević)");

        initGUI(model.getTable().getUut(), startTime, positions);
        pack();
    }

    private void initGUI(
            Component component, long startTime, Set<PositionParser.Definition> positions) {
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        if (positions != null) {
            blackBox = new JVHDLComponent(model, component, positions);
        } else {
            blackBox = new VHDLComponent(model, component);
        }

        JPanel panel = new JPanel(new BorderLayout());
        ZoomUI zoomUI = new ZoomUI();
        layer = new JLayer<>(blackBox, zoomUI);
        scrollPane = new JScrollPane(layer);

        addMouseListener(zoomUI);
        addKeyBindings(zoomUI);

        graphs = createGraphs(startTime);
        JPanel chartsPanel = new JPanel(new WrapLayout());
        graphs.forEach(g -> {
            ChartPanel chartPanel = new ChartPanel(g.getChart());
            chartPanel.setPreferredSize(new Dimension(400, 200));
            chartsPanel.add(chartPanel);
        });

        JFrame frame = new JFrame();
        frame.getContentPane().add(new JScrollPane(chartsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        frame.setSize(600, 600);
        frame.setTitle("Graphs");

        JButton statistics = new JButton("Graphs");
        statistics.addActionListener(l -> frame.setVisible(true));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(statistics, BorderLayout.PAGE_END);
        cp.add(panel);
    }

    private List<Graph> createGraphs(long startTime) {
        List<Graph> graphs = new ArrayList<>();
        List<VHDLComponent.Signal> signals = blackBox.getSignals();

        graphs.addAll(signals.stream().map(signal -> new Graph(signal, startTime)).collect(Collectors.toList()));

        return graphs;
    }

    private void addMouseListener(ZoomUI zoomUI) {
        layer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = layer.getMousePosition();
                double zoom = zoomUI.zoom;

                Optional<VHDLComponent.Signal> input = blackBox.getInputForPoint(p.getX() / zoom, p.getY() / zoom);

                if (input.isPresent()) {
                    VHDLComponent.Signal i = input.get();
                    LogicValue value = i.getValue();

                    if (value != LogicValue.ZERO) {
                        model.signalChanged(i.getAddress(), LogicValue.ZERO, System.currentTimeMillis());
                    } else {
                        model.signalChanged(i.getAddress(), LogicValue.ONE, System.currentTimeMillis());
                    }
                }

                layer.repaint();
            }
        });
    }

    private void addKeyBindings(ZoomUI zoomUI) {
        layer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_MASK), "zoomIn");
        layer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_MASK), "zoomIn");
        layer.getActionMap().put("zoomIn", zoomUI.zoomIn);

        layer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK), "zoomOut");
        layer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK), "zoomOut");
        layer.getActionMap().put("zoomOut", zoomUI.zoomOut);
    }

    public void updateGraphs(long time) {
        double t = (double) time / 1000;
        graphs.forEach(g -> SwingUtilities.invokeLater(() -> g.updateSignal(t)));
    }

    public void clearGraphs() {
        graphs.forEach(g -> g.clearData());
    }

    @Override
    public void signalChanged(int address, long time) {
        blackBox.repaint();

        double t = (double) time / 1000;
        graphs.stream().filter(g -> g.getSignal().getAddress() == address)
                .forEach(g -> SwingUtilities.invokeLater(() -> g.updateSignal(t)));
    }

    private class ZoomUI extends LayerUI<JComponent> {
        private double zoom = 1;
        public static final double INCREMENT = 0.1;

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.scale(zoom, zoom);
            super.paint(g2, c);
            g2.dispose();
        }

        private void reDraw() {
            layer.setPreferredSize(
                    new Dimension((int) (zoom * blackBox.getWidth()), (int) (zoom * blackBox.getHeight())));

            layer.revalidate();
            layer.repaint();
        }

        private AbstractAction zoomIn = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (5 - Math.abs(zoom) < 10e-5) {
                    return;
                }

                zoom *= 1.1;

                Point point = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(point, scrollPane);

                Point pos = scrollPane.getViewport().getViewPosition();

                int newX = (int) (point.x * (1.1f - 1f) + 1.1f * pos.x);
                int newY = (int) (point.y * (1.1f - 1f) + 1.1f * pos.y);
                scrollPane.getViewport().setViewPosition(new Point(newX, newY));

                reDraw();
            }
        };

        private AbstractAction zoomOut = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Math.abs(zoom) - 0.25 < 10e-5) {
                    return;
                }

                zoom *= 0.9;

                Point point = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(point, scrollPane);

                Point pos = scrollPane.getViewport().getViewPosition();

                int newX = (int) (point.x * (0.9f - 1f) + 0.9f * pos.x);
                int newY = (int) (point.y * (0.9f - 1f) + 0.9f * pos.y);
                scrollPane.getViewport().setViewPosition(new Point(newX, newY));

                reDraw();
            }
        };
    }
}