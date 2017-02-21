package hr.fer.zemris.java.vhdl.models.values;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.parser.ParserException;

/**
 * Created by Dominik on 21.2.2017..
 */
public class VectorData {
    private VectorOrder order;
    private int size;
    private int start;

    public VectorData(int first, VectorOrder order, int second) {
        if (!valid(first, order, second)) {
            throw new ParserException("Vector data is not valid.");
        }

        this.order = order;
        this.size = Math.abs(first - second) + 1;
        this.start = first;
    }

    private boolean valid(int first, VectorOrder order, int second) {
        if (order == VectorOrder.TO) {
            return first < second;
        } else {
            return first > second;
        }
    }

    public VectorOrder getOrder() {
        return order;
    }

    public int getSize() {
        return size;
    }

    public int getStart() {
        return start;
    }

    public boolean isValid(Declaration declaration) {
        if (declaration.getType() != Type.VECTOR_STD_LOGIC) {
            throw new ParserException("Invalid type.");
        }

        VectorData other = declaration.getVectorData();
        int end;
        if (order == null) {
            end = this.start;
        } else {
            if (this.order != other.order) {
                return false;
            }
            if (order == VectorOrder.TO) {
                end = start + size - 1;
            } else {
                end = start - size + 1;
            }
        }

        if (other.order == VectorOrder.TO) {
            return this.start >= other.start && end < (other.start + other.size);
        } else {
            return this.start <= other.start && end > (other.start - other.size);
        }
    }

}
