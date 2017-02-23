package hr.fer.zemris.java.vhdl.hierarchy;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.PortType;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.models.values.VectorOrder;
import hr.fer.zemris.java.vhdl.parser.Parser;
import hr.fer.zemris.java.vhdl.parser.ParserException;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Constant;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.Expression;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.AddressStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.SetStatement;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping.Mappable;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping.Mapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 21.2.2017..
 */
public class HierarchyBuilder {
    private Map<String, ProgramNode> entities;

    private Memory memory = new Memory();

    private Model testbench;

    public HierarchyBuilder(ProgramNode program) {
        entities = new HashMap<>();
        entities.put(program.getEntity().getName(), program);

        testbench = createTestbench(program);
        testbench.getModels().forEach(this::build);
    }

    private void build(Model model) {
        ProgramNode program = entities.get(model.getName());
        createSignals(model, program);
        createStatements(model, program);
        createChildren(model, program);
    }

    private void createChildren(Model model, ProgramNode program) {
        Set<Mapping> mappings = program.getArchitecture().getMappedEntities();
        for (Mapping mapping : mappings) {
            ProgramNode childNode = entities.get(mapping.getEntity());
            if (childNode == null) {
                childNode = parseEntity(mapping.getEntity());
            }

            Model child = new Model(mapping.getEntity(), model.getLabel() + "/" + mapping.getLabel());
            List<Declaration> mappedTo = childNode.getEntity().getDeclarations();
            if (mapping instanceof Mapping.Positional) {
                createPositionalMapping(child, ((Mapping.Positional) mapping).getMapped(), mappedTo);
            } else {
                createAssociativeMapping(child, ((Mapping.Associative) mapping).getMapped(), mappedTo);
            }

            child.setParent(model);
            model.addModel(child);

            build(child);
        }
    }

    private void createAssociativeMapping(Model child, Map<Declaration, Mappable> mapped, List<Declaration> mappedTo) {
        Set<Declaration> m = mapped.keySet();
        for (Declaration origin : mappedTo) {
            VectorData originData = origin.getVectorData();
            for (int i = 0, size = originData.getSize(), start = originData.getStart(); i < size; i++) {
                int index = originData.getOrder() == VectorOrder.TO ? start + i : start - i;
                VectorData newData = new VectorData(index, null, index);
                List<Declaration> valid = m.stream()
                        .filter(d -> d.getLabel().equals(origin.getLabel()) && newData.isValid(d.getVectorData()))
                        .collect(Collectors.toList());
                PortType portType = origin.getPortType();
                if ((portType == PortType.IN && valid.size() != 1) || (portType == PortType.OUT && valid.size() > 1)) {
                    throw new ParserException("Invalid number of mappings for " + origin.getLabel() + ".");
                }

                if (valid.size() == 1 && mapped.get(valid.get(0)) != null) {
                    Declaration dest = valid.get(0);
                    Declaration map = ((Expression) mapped.get(dest)).getDeclaration();
                    if (map.getPortType() != null && map.getPortType() != origin.getPortType()) {
                        throw new RuntimeException(
                                "Port " + origin.getLabel() + "must be of mode " + origin.getPortType().name());
                    }
                    if (mapped.get(dest) instanceof Constant) {
                        if (origin.getPortType() == PortType.OUT) {
                            throw new ParserException("Cannot assign constant to port of mode OUT.");
                        }
                        int define = memory.define(((Constant) mapped.get(dest)).getValues());
                        child.addSignal(origin, define);
                    }
                    child.addMapped(dest, map);
                } else {
                    if (portType != PortType.OUT) {
                        throw new RuntimeException();
                    }

                    child.addMapped(new Declaration(origin.getLabel(), PortType.OUT, newData), null);
                }
            }
        }
    }

    private void createPositionalMapping(Model child, List<Mappable> mapped, List<Declaration> mappedTo) {
        long mappedSize = mapped.size();
        long mappedToSize = mappedTo.stream().filter(d -> d.getPortType() == PortType.IN).count();
        if (mappedSize < mappedToSize) {
            throw new RuntimeException("Invalid number of mapped elements.");
        }

        for (int i = 0, size = mappedTo.size(); i < size; i++) {
            Declaration origin = mappedTo.get(i);
            if (i < mappedSize && mapped.get(i) != null) {
                Declaration map = ((Expression) mapped.get(i)).getDeclaration();
                checkMapping(origin, map);
                if (mapped.get(i) instanceof Constant) {
                    if (origin.getPortType() == PortType.OUT) {
                        throw new ParserException("Cannot assign constant to port of mode OUT.");
                    }
                    int start = memory.define(((Constant) mapped.get(i)).getValues());
                    child.addSignal(origin, start);
                } else {
                    child.addMapped(origin, map);
                }
            } else {
                if (origin.getPortType() != PortType.OUT) {
                    throw new ParserException("Cannot have open port of mode IN.");
                }
                child.addMapped(origin, null);
            }
        }
    }

    private void checkMapping(Declaration origin, Declaration map) {
        if (map.getPortType() != null && map.getPortType() != origin.getPortType()) {
            throw new RuntimeException("Port " + origin.getLabel() + "must be of mode " + origin.getPortType().name());
        }
        if (map.size() != origin.size()) {
            throw new RuntimeException("Port " + origin.getLabel() + "should be of size " + origin.size());
        }
    }

    private ProgramNode parseEntity(String entityName) {
        try {
            String program = new String(Files.readAllBytes(Paths.get(entityName + ".vhdl")), StandardCharsets.UTF_8);
            Parser parser = new Parser(new Lexer(program));
            ProgramNode prog = parser.getProgramNode();
            entities.put(entityName, prog);
            return prog;
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file: " + entityName + ".vhdl");
        }
    }

    private void createStatements(Model model, ProgramNode program) {
        List<SetStatement> statements = program.getArchitecture().getStatements();
        for (SetStatement statement : statements) {
            AddressStatement s = statement.prepareStatement(model);
            model.addStatement(s);
        }
    }

    private void createSignals(Model model, ProgramNode program) {
        List<Declaration> signals = program.getArchitecture().getSignals();
        for (Declaration signal : signals) {
            int size = signal.size();
            int address = memory.define(size);
            model.addSignal(signal, address);
        }
    }

    private Model createTestbench(ProgramNode program) {
        Model testbench = new Model("testbench", "");

        List<Declaration> signals = program.getEntity().getDeclarations();
        for (Declaration signal : signals) {
            int size = signal.size();
            int address = memory.define(size);
            testbench.addSignal(signal, address);
        }

        Model uut = new Model(program.getEntity().getName(), "/uut");
        uut.setParent(testbench);
        testbench.addModel(uut);
        for (Declaration signal : signals) {
            uut.addMapped(signal, signal);
        }

        return testbench;
    }

    public static void main(String[] args) throws IOException {
        String program = new String(Files.readAllBytes(Paths.get("Adder_4_bit.vhdl")), StandardCharsets.UTF_8);

        Lexer lexer = new Lexer(program);
        Parser parser = new Parser(lexer);
        HierarchyBuilder hb = new HierarchyBuilder(parser.getProgramNode());
        Memory m = hb.memory;
    }
}