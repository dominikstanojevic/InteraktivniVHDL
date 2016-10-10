package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.components.Component;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

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
import java.util.Map;
import java.util.Optional;

/**
 * Created by Dominik on 26.9.2016..
 */
public class VHDLComponent extends JComponent {
	protected Component component;

	protected List<Input> inputs = new ArrayList<>();
	protected List<Input> outputs = new ArrayList<>();

	protected int width = 600;
	protected int height;

	private int connectorSpacing = 10;
	private int spacing = 25;


	public VHDLComponent(Component component) {
		this(component, true);
	}

	protected VHDLComponent(Component component, boolean init) {
		this.component = component;

		if(init) {
			initComponent();
		}
	}

	private void initComponent() {
		int numberOfInputs = component.numberOfInputs();
		int numberOfOutputs = component.numberOfOutputs();

		int max = Math.max(numberOfInputs, numberOfOutputs);
		height = max * (Input.HEIGHT + connectorSpacing) + connectorSpacing + 6 * spacing;

		initInputs(numberOfInputs);
		initOutputs(numberOfOutputs);
	}

	private void initOutputs(int numberOfOutputs) {
		List<Signal> outputs = component.getOutputSignals();

		int boxHeight = height - 2 * spacing;
		int heightPerInput = boxHeight / (numberOfOutputs + 1);

		int startHeight = getInsets().top + spacing + heightPerInput;
		for (Signal output : outputs) {
			if (output.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR) {
				Map<Integer, LogicValue> values =
						((Vector) output.getValue()).getMappedValues();
				for(Map.Entry<Integer, LogicValue> value : values.entrySet()) {
					this.outputs.add(new Input(output, value.getKey(), 450, startHeight,
							true));
					startHeight += heightPerInput;
				}
			} else {
				this.outputs.add(new Input(output, null, 450, startHeight, true));
				startHeight += heightPerInput;
			}
		}
	}

	private void initInputs(int numberOfInputs) {
		List<Signal> inputs = component.getInputSignals();

		int boxHeight = height - 2 * spacing;
		int heightPerInput = boxHeight / (numberOfInputs + 1);

		int startHeight = getInsets().top + heightPerInput + spacing;
		for (Signal input : inputs) {
			if (input.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR) {
				Map<Integer, LogicValue> values =
						((Vector) input.getValue()).getMappedValues();
				for(Map.Entry<Integer, LogicValue> value : values.entrySet()) {
					this.inputs.add(new Input(input, value.getKey(), 50, startHeight, true));
					startHeight += heightPerInput;
				}
			} else {
				this.inputs.add(new Input(input, null, 50, startHeight, true));
				startHeight += heightPerInput;
			}
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
		g.setFont(Input.font);
		((Graphics2D) g).setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		int startY = spacing;
		int height = this.height - 2 * spacing;

		int boxStartX = BORDER + Input.WIDTH + WIRE_LENGTH;
		g.drawRect(boxStartX, startY, BOX_WIDTH, height);

		FontMetrics fm = g.getFontMetrics();
		int fontHeight = fm.getHeight() / 2;

		int wireStart = BORDER + Input.WIDTH;
		for(Input input : inputs) {
			input.paint(g);
			int y = input.startY + Input.HEIGHT / 2;
			g.drawLine(wireStart, y, boxStartX, y);
			g.drawString(input.toString(), boxStartX + FONT_SPACING, y + fontHeight / 2);
		}

		int boxEndX = boxStartX + BOX_WIDTH;
		for(Input output: outputs) {
			output.paint(g);
			int y = output.startY + Input.HEIGHT / 2;
			String s = output.toString();
			int width = fm.stringWidth(s);
			g.drawLine(boxEndX, y, boxEndX + WIRE_LENGTH, y);
			int end = (int) (boxEndX - FONT_SPACING - fm.getStringBounds(s, g).getWidth());
			g.drawString(s, end, y + fontHeight / 2);
		}
	}

	public Optional<Input> getInputForPoint(double x, double y) {
		return inputs.stream().filter(i -> i.isInside(x, y)).findFirst();
	}

	public static class Input {
		private Signal signal;
		private Integer position;
		private int startX;
		private int startY;
		private boolean horizontal;

		public static final int HEIGHT = 50;
		public static final int WIDTH = 100;

		public static final Font font = new Font("SansSerif", Font.PLAIN, 20);

		public Input(
				Signal signal, Integer position, int startX, int startY, boolean horizontal) {
			this.signal = signal;
			this.position = position;
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
			if (signal.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR) {
				return ((Vector) signal.getValue()).getLogicValue(position);
			} else {
				return (LogicValue) signal.getValue();
			}
		}

		public void paint(Graphics g) {
			g.setColor(Color.lightGray);
			if(horizontal) {
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
			return signal.toString() + (position == null ? "" : position);
		}

		public Signal getSignal() {
			return signal;
		}

		public Integer getPosition() {
			return position;
		}

		public int getStartX() {
			return startX;
		}

		public int getStartY() {
			return startY;
		}
	}

	public List<Input> getSignals() {
		List<Input> signals = new ArrayList<>();
		signals.addAll(inputs);
		signals.addAll(outputs);

		return signals;
	}
}
