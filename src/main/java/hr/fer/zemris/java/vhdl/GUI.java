package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.components.Component;
import hr.fer.zemris.java.vhdl.models.components.IModelListener;
import hr.fer.zemris.java.vhdl.models.components.Model;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
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
import java.util.Optional;

/**
 * Created by Dominik on 28.9.2016..
 */
public class GUI extends JFrame implements IModelListener {
	private Model model;
	private VHDLComponent blackBox;
	private JLayer<JComponent> layer;
	private JScrollPane scrollPane;

	public GUI(Model model) {
		this.model = model;
		model.addListener(this);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		initGUI(model.getTable().getTestedComponent());
		pack();
	}

	private void initGUI(Component component) {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		blackBox = new VHDLComponent(component);

		JPanel panel = new JPanel(new BorderLayout());
		ZoomUI zoomUI = new ZoomUI();
		layer = new JLayer<>(blackBox, zoomUI);
		scrollPane = new JScrollPane(layer);

		panel.add(scrollPane, BorderLayout.CENTER);
		cp.add(panel);
		addMouseListener(zoomUI);
		addKeyBindings(zoomUI);
	}

	private void addMouseListener(ZoomUI zoomUI) {
		layer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = layer.getMousePosition();
				double zoom = zoomUI.zoom;
				System.out.println(zoom * p.getX() + ", " + zoom * p.getY());

				Optional<VHDLComponent.Input> input =
						blackBox.getInputForPoint(p.getX() / zoom, p.getY() / zoom);

				if (input.isPresent()) {
					VHDLComponent.Input i = input.get();
					LogicValue value;
					if (i.getSignal().getDeclaration().getTypeOf() ==
						Value.TypeOf.STD_LOGIC_VECTOR) {
						value = ((Vector) i.getSignal().getValue())
								.getLogicValue(i.getPosition());
					} else {
						value = (LogicValue) i.getSignal().getValue();
					}

					if (value != LogicValue.ZERO) {
						model.signalChange(i.getSignal(), LogicValue.ZERO, i.getPosition(),
								System.currentTimeMillis());
					} else {
						model.signalChange(i.getSignal(), LogicValue.ONE, i.getPosition(),
								System.currentTimeMillis());
					}
				}

				layer.repaint();
			}
		});
	}

	private void addKeyBindings(ZoomUI zoomUI) {
		layer.getInputMap()
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_MASK), "zoomIn");
		layer.getInputMap()
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_MASK), "zoomIn");
		layer.getActionMap().put("zoomIn", zoomUI.zoomIn);

		layer.getInputMap()
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK),
						"zoomOut");
		layer.getInputMap()
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK),
						"zoomOut");
		layer.getActionMap().put("zoomOut", zoomUI.zoomOut);
	}

	@Override
	public void signalChanged(Signal signal, long time) {
		blackBox.repaint();
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
			layer.setPreferredSize(new Dimension((int) (zoom * blackBox.getWidth()),
					(int) (zoom * blackBox.getHeight())));

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

				int newX = (int)(point.x*(0.9f - 1f) + 0.9f*pos.x);
				int newY = (int)(point.y*(0.9f - 1f) + 0.9f*pos.y);
				scrollPane.getViewport().setViewPosition(new Point(newX, newY));

				reDraw();
			}
		};
	}
}
