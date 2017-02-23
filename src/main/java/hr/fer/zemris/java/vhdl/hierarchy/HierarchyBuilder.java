package hr.fer.zemris.java.vhdl.hierarchy;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.PortType;
import hr.fer.zemris.java.vhdl.parser.Parser;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
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
import java.util.Objects;
import java.util.Set;

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
            }

            child.setParent(model);
            model.addModel(child);

           build(child);
        }
    }

    private void createPositionalMapping(Model child, List<Mappable> mapped, List<Declaration> mappedTo) {
        long mappedSize = mapped.stream().filter(Objects::nonNull).count();
        long mappedToSize = mappedTo.stream().filter(d -> d.getPortType() == PortType.IN).count();
        if (mappedSize < mappedToSize) {
            throw new RuntimeException("Invalid number of mapped elements.");
        }

        for (int i = 0; i < mappedSize; i++) {
            Declaration origin = mappedTo.get(i);
            Declaration map = ((Expression) mapped.get(i)).getDeclaration();
            checkMapping(origin, map);
            child.addMapped(origin, map);
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
        String program = new String(Files.readAllBytes(Paths.get("VelikiDek2.vhdl")), StandardCharsets.UTF_8);

        Lexer lexer = new Lexer(program);
        Parser parser = new Parser(lexer);
        HierarchyBuilder hb = new HierarchyBuilder(parser.getProgramNode());
        Memory m = hb.memory;
    }
}