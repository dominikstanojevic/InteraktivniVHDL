package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class EntityNode {
    private String name;
    private List<Declaration> declarations;

    public EntityNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addSignals(List<Declaration> declarations) {
        if (this.declarations == null) {
            this.declarations = new ArrayList<>();
        }

        this.declarations.addAll(declarations);
    }

    public int numberOfSignals() {
        return declarations.size();
    }

    public List<Declaration> getDeclarations() {
        return declarations != null ? declarations : Collections.EMPTY_LIST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityNode that = (EntityNode) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
