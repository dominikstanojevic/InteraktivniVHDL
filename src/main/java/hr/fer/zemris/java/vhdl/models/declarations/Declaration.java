package hr.fer.zemris.java.vhdl.models.declarations;

import hr.fer.zemris.java.vhdl.models.values.Type;
import hr.fer.zemris.java.vhdl.models.values.VectorData;

/**
 * Created by Dominik on 21.2.2017..
 */
public class Declaration {
    private String label;
    private Type type;
    private PortType portType;
    private VectorData vectorData;

    private static final VectorData SCALAR = new VectorData(0, null, 0);

    private Declaration(String label, Type type, PortType portType, VectorData vectorData) {
        this.label = label;
        this.type = type;
        this.portType = portType;
        this.vectorData = vectorData;
    }

    public Declaration(String label) {
        this(label, Type.STD_LOGIC, null, SCALAR);
    }

    public Declaration(String label, PortType portType) {
        this(label, Type.STD_LOGIC, portType, SCALAR);
    }

    public Declaration(String label, VectorData vectorData) {
        this(label, Type.VECTOR_STD_LOGIC, null, vectorData);
    }

    public Declaration(String label, PortType portType, VectorData vectorData) {
        this(label, Type.VECTOR_STD_LOGIC, portType, vectorData);
    }

    public String getLabel() {
        return label;
    }

    public Type getType() {
        return type;
    }

    public PortType getPortType() {
        return portType;
    }

    public VectorData getVectorData() {
        return vectorData;
    }

    public int size() {
        return vectorData == null ? 1 : vectorData.getSize();
    }


}
