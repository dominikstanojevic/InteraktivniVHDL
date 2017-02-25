package hr.fer.zemris.java.vhdl.environment;

/**
 * Created by Dominik on 24.2.2017..
 */
public interface IModelListener {
    void signalChanged(int address, long time);
}
