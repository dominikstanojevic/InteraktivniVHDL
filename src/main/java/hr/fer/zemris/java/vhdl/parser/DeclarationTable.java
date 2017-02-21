package hr.fer.zemris.java.vhdl.parser;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dominik on 30.7.2016..
 */
public class DeclarationTable {
    private String entryName;
    private Map<String, Declaration> declarations;
    private String archName;
    private Set<String> labels = new HashSet<>();

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public void addDeclaration(Declaration declarable) {
        if (declarations == null) {
            declarations = new HashMap<>();
        }

        if (declarations.containsKey(declarable.getLabel())) {
            throw new ParserException("Declaration " + declarable.getLabel() + " already exists.");
        }

        declarations.put(declarable.getLabel(), declarable);
    }

    public String getArchName() {
        return archName;
    }

    public void setArchName(String archName) {
        this.archName = archName;
    }

    public boolean addLabel(String label) {
        return label == null || labels.add(label);
    }

    public boolean isDeclared(String name) {
        return declarations.containsKey(name);
    }

    public Declaration getDeclaration(String name) {
        return declarations.get(name);
    }
}
