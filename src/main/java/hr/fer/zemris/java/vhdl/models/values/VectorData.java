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
        if (order != null && !valid(first, order, second)) {
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
        //TODO: FIX (MAYBE)
        /* if (declaration.getType() != Type.VECTOR_STD_LOGIC) {
            throw new ParserException("Invalid type.");
        }*/

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

    public Offset calculateOffset(VectorData other) {
        int start = Math.abs(this.start - other.start);
        int end = Math.abs(this.size - other.size) - start;
        return new Offset(start, end);
    }

    public Offset calculateOffset(int pos) {
        int start = Math.abs(this.start - pos);
        int end = Math.abs(this.size - 1) - start;
        return new Offset(start, end);
    }

    public VectorData getFromOffset(Offset offset) {
        VectorOrder newOrder = order;
        int newStart;
        int newEnd;
        if (order == VectorOrder.TO) {
            newStart = start + offset.start;
            newEnd = (start + size - 1) - offset.end;
        } else if (order == VectorOrder.DOWNTO) {
            newStart = start - offset.start;
            newEnd = (start - size + 1) + offset.end;
        } else {
            newStart = start;
            newEnd = start;
        }

        if (newStart == newEnd) {
            newOrder = null;
        }
        return new VectorData(newStart, newOrder, newEnd);
    }

    public VectorData getAddress(VectorData other, int start) {
        Offset offset = calculateOffset(other);
        VectorData address = getFromOffset(offset);
        address.start = start + offset.start;
        return address;
    }

    public static class Offset {
        private int start;
        private int end;

        public Offset(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
