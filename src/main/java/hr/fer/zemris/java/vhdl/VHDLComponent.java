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
	private Component component;

	private List<Input> inputs = new ArrayList<>();
	private List<Input> outputs = new ArrayList<>();

	private static final int WIDTH = 600;
	private int height;

	private static final int CONNECTOR_SPACING = 10;
	private static final int SPACING = 25;

	private static final int WIRE_LENGTH = 50;

	public VHDLComponent(Component component) {
		this.component = component;

		initComponent();
	}

	private void initComponent() {
		int numberOfInputs = component.numberOfInputs();
		int numberOfOutputs = component.numberOfOutputs();

		int max = Math.max(numberOfInputs, numberOfOutputs);
		height = max * (Input.HEIGHT + CONNECTOR_SPACING) + CONNECTOR_SPACING + 6 * SPACING;

		initInputs(numberOfInputs);
		initOutputs(numberOfOutputs);
	}

	private void initOutputs(int numberOfOutputs) {
		List<Signal> outputs = component.getOutputSignals();

		int boxYStart = getInsets().top + SPACING;
		int boxHeight = (getHeight() - getInsets().bottom - SPACING) - boxYStart;
		int heightPerInput = boxHeight / (numberOfOutputs + 1);

		int startHeight = getInsets().top + SPACING + heightPerInput;
		for (Signal output : outputs) {
			if (output.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR) {
				Map<Integer, LogicValue> values =
						((Vector) output.getValue()).getMappedValues();
				for(Map.Entry<Integer, LogicValue> value : values.entrySet()) {
					this.outputs.add(new Input(output, value.getKey(), 450, startHeight));
					startHeight += heightPerInput;
				}
			} else {
				this.outputs.add(new Input(output, null, 450, startHeight));
				startHeight += heightPerInput;
			}
		}
	}

	private void initInputs(int numberOfInputs) {
		List<Signal> inputs = component.getInputSignals();

		int boxYStart = getInsets().top + SPACING;
		int boxHeight = (getHeight() - getInsets().bottom - SPACING) - boxYStart;
		int heightPerInput = boxHeight / (numberOfInputs + 1);

		int startHeight = getInsets().top + SPACING + heightPerInput;
		for (Signal input : inputs) {
			if (input.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR) {
				Map<Integer, LogicValue> values =
						((Vector) input.getValue()).getMappedValues();
				for(Map.Entry<Integer, LogicValue> value : values.entrySet()) {
					this.inputs.add(new Input(input, value.getKey(), 50, startHeight));
					startHeight += heightPerInput;
				}
			} else {
				this.inputs.add(new Input(input, null, 50, startHeight));
				startHeight += heightPerInput;
			}
		}

	}



	@Override
	public Dimension preferredSize() {
		return new Dimension(WIDTH, height);
	}

	@Override
	public int getWidth() {
		return WIDTH + getInsets().left + getInsets().right;
	}

	@Override
	public int getHeight() {
		return height + getInsets().top + getInsets().right;
	}

	@Override
	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		int startY = getInsets().top + SPACING;
		int height = getHeight() - getInsets().bottom - SPACING - startY;

		g.drawRect(200, startY, 200, height);

		FontMetrics fm = g.getFontMetrics();
		int fontHeight = fm.getHeight() / 2;


		for(Input input : inputs) {
			input.paint(g);
			int y = input.startY + Input.HEIGHT / 2;
			g.drawLine(150, y, 200, y);
			g.drawString(input.toString(), 210, y + fontHeight);
		}

		for(Input output: outputs) {
			output.paint(g);
			int y = output.startY + Input.HEIGHT / 2;
			String s = output.toString();
			int width = fm.stringWidth(s);
			g.drawLine(400, y, 450, y);
			g.drawString(s, 375 - width, y + fontHeight);
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

		public static final int HEIGHT = 50;
		public static final int WIDTH = 100;

		private static final Font font = new Font("SansSerif", Font.PLAIN, 20);

		public Input(
				Signal signal, Integer position, int startX, int centerY) {
			this.signal = signal;
			this.position = position;
			this.startX = startX;
			this.startY = centerY - HEIGHT / 2;
		}

		public boolean isInside(double x, double y) {
			if (x < startX) {
				return false;
			}
			if (y < startY) {
				return false;
			}
			if (x > startX + WIDTH) {
				return false;
			}
			if (y > startY + HEIGHT) {
				return false;
			}

			return true;
		}

		private LogicValue getValue() {
			if (signal.getDeclaration().getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR) {
				return ((Vector) signal.getValue()).getLogicValue(position);
			} else {
				return (LogicValue) signal.getValue();
			}
		}

		public void paint(Graphics g) {
			g.setColor(Color.lightGray);
			g.fillRect(startX, startY, WIDTH, HEIGHT);
			g.setColor(Color.black);

			g.setFont(font);
			FontMetrics fm = g.getFontMetrics();
			int fontWidth = fm.stringWidth(getValue().toString());
			int fontHeight = fm.getHeight() / 2;
			int startHeight = startY + (HEIGHT + fontHeight) / 2;
			int startWidth = startX + (WIDTH - fontWidth) / 2;

			g.drawString(getValue().toString(), startWidth, startHeight);
		}

		@Override
		public String toString() {
			return signal.getName() + (position == null ? "" : position);
		}

		public Signal getSignal() {
			return signal;
		}

		public Integer getPosition() {
			return position;
		}
	}

	static class Output {

	}
}
