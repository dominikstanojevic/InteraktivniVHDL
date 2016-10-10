package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.components.Component;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;
import hr.fer.zemris.java.vhdl.parser.PositionParser;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 10.10.2016..
 */
public class JVHDLComponent extends VHDLComponent {

	private List<VHDLComponent.Input> top = new ArrayList<>();
	private List<VHDLComponent.Input> left = new ArrayList<>();
	private List<VHDLComponent.Input> right = new ArrayList<>();
	private List<VHDLComponent.Input> bottom = new ArrayList<>();

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

	public JVHDLComponent(Component component, Set<PositionParser.Definition> positions) {
		super(component, false);

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

		List<Signal> inputs = component.getInputSignals();
		List<Signal> outputs = component.getOutputSignals();

		initTop(positions.stream().filter(d -> d.getPosition() == PositionParser.Position.TOP)
						.collect(Collectors.toCollection(LinkedHashSet::new)), inputs, outputs, top,
				left);
		initLeft(
				positions.stream().filter(d -> d.getPosition() == PositionParser.Position.LEFT)
						.collect(Collectors.toCollection(LinkedHashSet::new)), inputs, outputs,
				left, top);

		initRight(positions.stream()
				.filter(d -> d.getPosition() == PositionParser.Position.RIGHT)
				.collect(Collectors.toCollection(LinkedHashSet::new)), inputs, outputs, right);

		initBottom(positions.stream()
						.filter(d -> d.getPosition() == PositionParser.Position.BOTTOM)
						.collect(Collectors.toCollection(LinkedHashSet::new)), inputs, outputs,
				bottom);
	}

	private void initRight(
			Set<PositionParser.Definition> definitions, List<Signal> inputs,
			List<Signal> outputs, int right) {

		int heightPerInput = boxHeight / (right + 1);

		int startHeight = BORDER + heightPerInput;
		if (topExists) {
			startHeight += Input.WIDTH + WIRE_LENGTH;
		}
		int startWidth = BORDER + boxWidth + WIRE_LENGTH;
		if (leftExists) {
			startWidth += Input.WIDTH + WIRE_LENGTH;
		}

		for (PositionParser.Definition d : definitions) {
			readSignal(this.right, d, inputs, outputs, startWidth, startHeight, true);
			startHeight += heightPerInput;
		}
	}

	private void initLeft(
			Set<PositionParser.Definition> definitions, List<Signal> inputs,
			List<Signal> outputs, int left, int top) {
		int heightPerInput = boxHeight / (left + 1);

		int startHeight = BORDER + heightPerInput;
		startHeight += top == 0 ? 0 : (Input.WIDTH + WIRE_LENGTH);
		int startWidth = BORDER;

		for (PositionParser.Definition d : definitions) {
			readSignal(this.left, d, inputs, outputs, startWidth, startHeight, true);
			startHeight += heightPerInput;
		}
	}

	private void initBottom(
			Set<PositionParser.Definition> definitions, List<Signal> inputs,
			List<Signal> outputs, int bottom) {
		int widthPerInput = boxWidth / (bottom + 1);

		int startWidth = BORDER + widthPerInput;
		if (leftExists) {
			startWidth += Input.WIDTH + WIRE_LENGTH;
		}

		int startHeight = height - BORDER - Input.WIDTH;

		for (PositionParser.Definition d : definitions) {
			readSignal(this.bottom, d, inputs, outputs, startWidth, startHeight, false);
			startWidth += widthPerInput;
		}
	}

	private void initTop(
			Set<PositionParser.Definition> definitions, List<Signal> inputs,
			List<Signal> outputs, int top, int left) {
		int widthPerInput = boxWidth / (top + 1);

		int startWidth = BORDER + widthPerInput;
		startWidth += left == 0 ? 0 : (Input.WIDTH + WIRE_LENGTH);
		int startHeight = BORDER;

		for (PositionParser.Definition d : definitions) {
			readSignal(this.top, d, inputs, outputs, startWidth, startHeight, false);
			startWidth += widthPerInput;
		}

	}

	private void readSignal(
			List<VHDLComponent.Input> position, PositionParser.Definition d,
			List<Signal> inputs, List<Signal> outputs, int width, int height,
			boolean horizontal) {
		Optional<Signal> signal =
				inputs.stream().filter(s -> s.getName().equals(d.getSignal())).findFirst();
		if (signal.isPresent()) {
			addSignal(this.inputs, position, horizontal, signal.get(), d, width, height);
			return;
		}

		signal = outputs.stream().filter(s -> s.getName().equals(d.getSignal())).findFirst();
		if (signal.isPresent()) {
			addSignal(this.outputs, position, horizontal, signal.get(), d, width, height);
			return;
		}

		throw new RuntimeException(
				"Invalid definition. Signal: " + d.getSignal() + " " + "does not exist.");
	}

	private void addSignal(
			List<VHDLComponent.Input> list, List<VHDLComponent.Input> position, boolean b,
			Signal signal, PositionParser.Definition d, int startWidth, int startHeight) {
		if (d.getAccess() == null) {
			VHDLComponent.Input s =
					new VHDLComponent.Input(signal, null, startWidth, startHeight, b);
			list.add(s);
			position.add(s);
		} else {
			if (signal.getDeclaration().getTypeOf() != Value.TypeOf.STD_LOGIC_VECTOR) {
				throw new RuntimeException(
						"Invalid definition for signal: " + signal.getName());
			}

			Vector value = (Vector) signal.getValue();
			value.checkPosition(d.getAccess());

			VHDLComponent.Input s =
					new VHDLComponent.Input(signal, d.getAccess(), startWidth, startHeight, b);
			list.add(s);
			position.add(s);
		}
	}

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
		return 2 * SPACING + max * (Input.WIDTH + CONNECTOR_SPACING) + CONNECTOR_SPACING;
	}

	private int calculateSize(int a, int b, int box) {
		int aExists = a == 0 ? 0 : 1;
		int bExists = b == 0 ? 0 : 1;

		return 2 * BORDER + (aExists + bExists) * (VHDLComponent.Input.WIDTH + WIRE_LENGTH) +
			   box;
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setFont(VHDLComponent.Input.font);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		int boxStartX = BORDER;
		if (leftExists) {
			boxStartX += VHDLComponent.Input.WIDTH + WIRE_LENGTH;
		}
		int boxStartY = BORDER;
		if (topExists) {
			boxStartY += VHDLComponent.Input.WIDTH + WIRE_LENGTH;
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

		for (VHDLComponent.Input t : top) {
			t.paint(g);

			int lineWidth = t.getStartX() + Input.HEIGHT / 2;
			int lineHeight = t.getStartY() + Input.WIDTH;
			g.drawLine(lineWidth, lineHeight, lineWidth, lineHeight + WIRE_LENGTH);

			g.drawString(t.toString(), lineWidth - fm.stringWidth(t.getSignal().getName()) / 2,
					lineHeight + WIRE_LENGTH + FONT_SPACING + fontHeight / 2);
		}
	}

	private void paintBottom(Graphics g, FontMetrics fm) {
		int fontHeight = fm.getHeight() / 2;

		for (Input b : bottom) {
			b.paint(g);

			int lineWidth = b.getStartX() + Input.HEIGHT / 2;
			int lineHeight = b.getStartY();
			g.drawLine(lineWidth, lineHeight, lineWidth, lineHeight - WIRE_LENGTH);

			g.drawString(b.toString(), lineWidth - fm.stringWidth(b.getSignal().getName()) / 2,
					lineHeight - WIRE_LENGTH - FONT_SPACING - fontHeight / 2);
		}
	}

	private void paintLeft(Graphics g, FontMetrics fm) {
		int fontHeight = fm.getHeight() / 2;

		int wireStart = BORDER + Input.WIDTH;
		for (Input l : left) {
			l.paint(g);
			int y = l.getStartY() + Input.HEIGHT / 2;
			g.drawLine(wireStart, y, wireStart + WIRE_LENGTH, y);
			g.drawString(l.toString(), wireStart + WIRE_LENGTH + FONT_SPACING,
					y + fontHeight / 2);
		}
	}

	private void paintRight(Graphics g, FontMetrics fm) {
		int fontHeight = fm.getHeight() / 2;

		int wireStart = BORDER + boxWidth;
		if (leftExists) {
			wireStart += Input.WIDTH + WIRE_LENGTH;
		}
		for (Input r : right) {
			r.paint(g);
			int y = r.getStartY() + Input.HEIGHT / 2;
			g.drawLine(wireStart, y, wireStart + WIRE_LENGTH, y);
			int end = (int) (wireStart - FONT_SPACING -
							 fm.getStringBounds(r.toString(), g).getWidth());
			g.drawString(r.toString(), end, y + fontHeight / 2);
		}
	}
}
