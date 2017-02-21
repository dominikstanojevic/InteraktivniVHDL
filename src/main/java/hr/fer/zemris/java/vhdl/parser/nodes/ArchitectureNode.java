package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ArchitectureNode {
    private String name;
    private List<Declaration> signals;
    private List<Statement> statements = new ArrayList<>();
    //TODO: FIX
    //private List<EntityMap> mappedEntities = new ArrayList<>();

    public ArchitectureNode(String name) {
        this.name = name;
    }

    public ArchitectureNode(
            String name, List<Declaration> internalSignals) {
        this.name = name;
        this.signals = internalSignals;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void addSignals(List<Declaration> signals) {
        if (this.signals == null) {
            this.signals = new ArrayList<>();
        }

        this.signals.addAll(signals);
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public List<Declaration> getSignals() {
        return signals == null ? Collections.EMPTY_LIST : signals;
    }

    /*public List<EntityMap> getMappedEntities() {
        return mappedEntities;
    }*/

    public String getName() {
        return name;
    }
}
