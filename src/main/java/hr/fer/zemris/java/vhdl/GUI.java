package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.components.Component;
import hr.fer.zemris.java.vhdl.models.components.IModelListener;
import hr.fer.zemris.java.vhdl.models.components.Model;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.plaf.LayerUI;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Optional;

/**
 * Created by Dominik on 28.9.2016..
 */
public class GUI extends JFrame implements IModelListener {
	private Model model;
	private VHDLComponent blackBox;
	private JLayer<JComponent> layer;

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

		panel.add(new JScrollPane(layer), BorderLayout.CENTER);
		cp.add(panel);
		addMouseListener(zoomUI);
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

		layer.addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() == 1) {
					zoomUI.zoomIn();
				} else {
					zoomUI.zoomOut();
				}

				layer.setPreferredSize(new Dimension((int) (zoomUI.zoom * blackBox.getWidth()),
						(int) (zoomUI.zoom * blackBox.getHeight())));
				layer.revalidate();
				layer.repaint();
			}
		});
	}

	@Override
	public void signalChanged(Signal signal, long time) {
		blackBox.repaint();
	}

	private static class ZoomUI extends LayerUI<JComponent> {
		private double zoom = 1;
		public static final double INCREMENT = 0.1;

		public void installUI(JComponent c) {
			super.installUI(c);
			JLayer jlayer = (JLayer) c;
			jlayer.setLayerEventMask(
					AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		}

		@Override
		public void uninstallUI(JComponent c) {
			JLayer jlayer = (JLayer) c;
			jlayer.setLayerEventMask(0);
			super.uninstallUI(c);
		}

		@Override
		public void paint(Graphics g, JComponent c) {
			Graphics2D g2 = (Graphics2D) g.create();

			g2.scale(zoom, zoom);
			super.paint(g2, c);

			g2.dispose();
		}

		@Override
		protected void processMouseWheelEvent(
				MouseWheelEvent e, JLayer<? extends JComponent> l) {
			super.processMouseWheelEvent(e, l);
		}

		public void zoomIn() {
			if (5 - Math.abs(zoom) < 10e-5) {
				return;
			}

			zoom += INCREMENT;
		}

		public void zoomOut() {
			if (Math.abs(zoom) - 0.25 < 10e-5) {
				return;
			}

			zoom -= INCREMENT;
		}

		public double getZoom() {
			return zoom;
		}
	}
}
