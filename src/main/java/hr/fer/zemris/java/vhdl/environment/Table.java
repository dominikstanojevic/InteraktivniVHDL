package hr.fer.zemris.java.vhdl.environment;

import hr.fer.zemris.java.vhdl.hierarchy.Component;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.AddressStatement;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dominik on 24.2.2017..
 */
public class Table {
    private Component uut;
    private Map<Integer, Set<AddressStatement>> statements = new HashMap<>();

    public Component getUut() {
        return uut;
    }

    public void setUut(Component uut) {
        this.uut = uut;
    }

    public Map<Integer, Set<AddressStatement>> getStatements() {
        return statements;
    }

    public void addStatement(int address, AddressStatement statement) {
        Set<AddressStatement> statementsForAddress = statements.computeIfAbsent(address, k -> new LinkedHashSet<>());
        statementsForAddress.add(statement);
    }

    public Set<AddressStatement> getStatementsForAddress(int address) {
        Set<AddressStatement> statements = this.statements.get(address);
        return statements == null ? Collections.EMPTY_SET : statements;
    }
}
