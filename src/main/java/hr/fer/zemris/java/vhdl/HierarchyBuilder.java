package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.Component;
import hr.fer.zemris.java.vhdl.models.SimulationStatement;
import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.declarable.Port;
import hr.fer.zemris.java.vhdl.models.declarable.Signal;
import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.declarations.PortDeclaration;
import hr.fer.zemris.java.vhdl.models.declarations.SignalDeclaration;
import hr.fer.zemris.java.vhdl.models.mappers.AssociativeMap;
import hr.fer.zemris.java.vhdl.models.mappers.EntityMap;
import hr.fer.zemris.java.vhdl.models.mappers.Mappable;
import hr.fer.zemris.java.vhdl.models.mappers.PositionalMap;
import hr.fer.zemris.java.vhdl.parser.DeclarationTable;
import hr.fer.zemris.java.vhdl.parser.Parser;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalExpression;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Dominik on 22.8.2016..
 */
public class HierarchyBuilder {
	private Map<String, ProgramNode> entities = new HashMap<>();
	private Table table;

	public Table getTable() {
		return table;
	}

	public HierarchyBuilder(ProgramNode component) {
		Objects.requireNonNull(component, "Entity cannot be null");

		table = new Table(component.getEntity().getName());

		ProgramNode testbenchNode = createTestbench(component);
		entities.put(testbenchNode.getEntity().getName(), testbenchNode);

		entities.put(component.getEntity().getName(), component);

		Component testbench = parseMappings(testbenchNode, "");
		table.setComponent(testbench);
	}

	private ProgramNode createTestbench(ProgramNode component) {
		DeclarationTable table = new DeclarationTable();

		EntityNode entity = new EntityNode("testbench");
		table.setEntryName("testbench");

		Set<Signal> testbenchSignals = createTestbenchSignals(component, table);
		ArchitectureNode arch = new ArchitectureNode("arch", testbenchSignals);
		table.setArchName("arch");

		table.addLabel("uut");
		List<Mappable> map = testbenchSignals.stream().map(e -> new SignalExpression(e))
				.collect(Collectors.toList());
		arch.addStatement(new PositionalMap("uut", component.getEntity().getName(), map));

		return new ProgramNode(entity, arch, table);
	}

	private Set<Signal> createTestbenchSignals(
			ProgramNode component, DeclarationTable table) {
		Set<Signal> testbenchSignals = new LinkedHashSet<>();

		component.getEntity().getDeclarations().forEach(declaration -> {
			SignalDeclaration changedDeclaration =
					declaration.getDeclaration().convertToSignalDeclaration();

			Signal signal = new Signal(declaration.getName(), changedDeclaration);
			testbenchSignals.add(signal);
			table.addDeclaration(signal);
		});

		return testbenchSignals;
	}

	private Component parseMappings(ProgramNode component, String label) {
		Set<Signal> signals = createSignals(component.getArchitecture().getSignals(), label);
		List<SimulationStatement> statements = addStatements(component, label);

		List<Component> children = new ArrayList<>();
		for (EntityMap map : component.getArchitecture().getMappedEntities()) {
			ProgramNode entry;
			if (!entities.containsKey(map.getEntity())) {
				entry = new Parser(new Lexer(getEntry(map.getEntity()))).getProgramNode();
				entities.put(entry.getEntity().getName(), entry);
			} else {
				entry = entities.get(map.getEntity());
			}
			String name = label + "/" + map.getLabel();

			checkMapping(map, component, entry, label);

			Component child = parseMappings(entry, name);
			children.add(child);
		}

		return new Component(label, component.getEntity().getDeclarations(), signals,
				statements, children);
	}

	private List<SimulationStatement> addStatements(ProgramNode component, String name) {
		List<SimulationStatement> statements = new ArrayList<>();
		component.getArchitecture().getStatements().forEach(s -> {
			SimulationStatement statement = new SimulationStatement(name, s);
			statements.add(statement);
			table.addStatement(statement);
		});

		return statements;
	}

	private Set<Signal> createSignals(
			Set<Signal> signals, String label) {
		Set<Signal> createdSignals = new HashSet<>();

		signals.forEach(s -> {
			Signal signal = s.createValue(label);
			table.addSignal(signal);
			createdSignals.add(signal);
		});

		return createdSignals;
	}

	private void checkMapping(
			EntityMap mapping, ProgramNode entry, ProgramNode mappedEntry, String label) {
		checkSize(mapping, entry, mappedEntry);

		if (mapping instanceof PositionalMap) {
			checkPositionalMapping((PositionalMap) mapping, entry, mappedEntry, label);
		} else {
			checkAssociativeMapping((AssociativeMap) mapping, entry, mappedEntry, label);
		}
	}

	private void checkSize(EntityMap mapping, ProgramNode entry, ProgramNode mappedEntry) {
		if (mapping.mapSize() != mappedEntry.getEntity().numberOfSignals()) {
			throw new RuntimeException(
					"Invalid mapping of " + mappedEntry.getEntity().getName() + " in " +
					entry.getEntity().getName() + ".");
		}
	}

	private void checkAssociativeMapping(
			AssociativeMap mapping, ProgramNode entry, ProgramNode mappedEntry, String label) {

		Set<Port> ports = mappedEntry.getEntity().getDeclarations();
		for (Port port : ports) {
			//String name = port.getKey().getName();
			if (!mapping.isMapped(port)) {
				throw new RuntimeException("Signal " + port.getName() + "is not mapped.");
			}

			Mappable mappedTo = mapping.mappedTo(port);

			Declaration mappedToDeclaration = mappedTo.getDeclaration();

			if (!(mappedTo instanceof SignalExpression) &&
				port.getDeclaration().getPortType() != PortDeclaration.Type.IN) {
				throw new RuntimeException("Only in ports may be associate with expression.");
			}

			if (!Declaration.checkMapping(port.getDeclaration(), mappedToDeclaration)) {
				throw new RuntimeException("Signals are not compatibile.");
			}

			String alias = label + "/" + mapping.getLabel() + "/" + port.getName();
			table.addAlias(alias, label + "/" + mappedTo.getName());
		}
	}

	private void checkPositionalMapping(
			PositionalMap mapping, ProgramNode entry, ProgramNode mappedEntry, String label) {

		List<Mappable> signals = mapping.getSignals();
		List<Port> declared = new ArrayList<>(mappedEntry.getEntity().getDeclarations());

		for (int i = 0; i < signals.size(); i++) {
			Declaration declaration = signals.get(i).getDeclaration();

			if (!(signals.get(i) instanceof SignalExpression) &&
				declared.get(i).getDeclaration().getPortType() != PortDeclaration.Type.IN) {
				throw new RuntimeException("Only in ports may be associate with expression.");
			}

			if (!Declaration.checkMapping(declared.get(i).getDeclaration(), declaration)) {
				throw new RuntimeException("Signals are not compatibile.");
			}

			String alias = label + "/" + mapping.getLabel() + "/" + declared.get(i).getName();
			table.addAlias(alias, label + "/" + signals.get(i).getName());
		}
	}

	private String getEntry(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(name + ".txt")),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Error reading file: " + name + ".");
		}
	}

	public static void main(String[] args) throws IOException {
		String program =
				new String(Files.readAllBytes(Paths.get("sklop.txt")), StandardCharsets.UTF_8);

		HierarchyBuilder hb =
				new HierarchyBuilder(new Parser(new Lexer(program)).getProgramNode());

		System.out.println("Nice");
	}
}
