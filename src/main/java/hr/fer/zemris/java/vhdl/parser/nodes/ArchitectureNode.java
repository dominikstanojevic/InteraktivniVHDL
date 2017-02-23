package hr.fer.zemris.java.vhdl.parser.nodes;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping.Mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dominik on 25.7.2016..
 */
public class ArchitectureNode {
    private String name;
    private List<Declaration> signals;
    private List<SetStatement> statements = new ArrayList<>();
    private Set<Mapping> mappedEntities = new HashSet<>();

    public ArchitectureNode(String name) {
        this.name = name;
    }

    public ArchitectureNode(
            String name, List<Declaration> internalSignals) {
        this.name = name;
        this.signals = internalSignals;
    }

    public List<SetStatement> getStatements() {
        return statements;
    }

    public void addSignals(List<Declaration> signals) {
        if (this.signals == null) {
            this.signals = new ArrayList<>();
        }

        this.signals.addAll(signals);
    }

    public void addStatement(Statement statement) {
        if(statement instanceof SetStatement) {
            statements.add((SetStatement) statement);
        } else {
            mappedEntities.add((Mapping) statement);
        }
    }

    public List<Declaration> getSignals() {
        return signals == null ? Collections.EMPTY_LIST : signals;
    }

    public Set<Mapping> getMappedEntities() {
        return mappedEntities;
    }

    public String getName() {
        return name;
    }
}
