package hr.fer.zemris.java.vhdl.models.components;

import hr.fer.zemris.java.vhdl.models.declarable.Signal;

/**
 * Created by Dominik on 23.9.2016..
 */
public interface IModelListener {
	void signalChanged(Signal signal, long time);
}
