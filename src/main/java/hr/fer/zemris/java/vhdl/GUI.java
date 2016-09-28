package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.models.components.Component;
import hr.fer.zemris.java.vhdl.models.components.IModelListener;
import hr.fer.zemris.java.vhdl.models.components.Model;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.values.LogicValue;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.models.values.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Created by Dominik on 28.9.2016..
 */
public class GUI extends JFrame implements IModelListener {
	private Model model;
	private VHDLComponent blackBox;

	public GUI(Model model) {
		this.model = model;
		model.addListener(this);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		initGUI(model.getTable().getTestedComponent());
		pack();
	}

	private void initGUI(Component component) {
		Container cp = getContentPane();
		blackBox = new VHDLComponent(component);
		cp.add(new JScrollPane(blackBox));
		blackBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = blackBox.getMousePosition();
				System.out.println(p.getX() + ", " + p.getY());

				Optional<VHDLComponent.Input> input =
						blackBox.getInputForPoint(p.getX(), p.getY());

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

				blackBox.repaint();
			}
		});
	}

	@Override
	public void signalChanged(Signal signal, long time) {
		blackBox.repaint();
	}
}
