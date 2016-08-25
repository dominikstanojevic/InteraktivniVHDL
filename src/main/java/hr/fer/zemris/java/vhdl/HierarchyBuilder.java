package hr.fer.zemris.java.vhdl;

import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.SimulationStatement;
import hr.fer.zemris.java.vhdl.models.Table;
import hr.fer.zemris.java.vhdl.models.mappers.AssociativeMap;
import hr.fer.zemris.java.vhdl.models.mappers.EntityMap;
import hr.fer.zemris.java.vhdl.models.mappers.PositionalMap;
import hr.fer.zemris.java.vhdl.models.values.Value;
import hr.fer.zemris.java.vhdl.parser.DeclarationTable;
import hr.fer.zemris.java.vhdl.parser.Parser;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.expressions.signal.SignalDeclaration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
	private ProgramNode tested;

	public Table getTable() {
		return table;
	}

	public HierarchyBuilder(ProgramNode component) {
		Objects.requireNonNull(component, "Entity cannot be null");

		table = new Table(component.getEntity().getName());
		tested = component;

		ProgramNode testbench = createTestbench(component);
		entities.put(testbench.getEntity().getName(), testbench);

		entities.put(component.getEntity().getName(), component);

		parseMappings(testbench, "");
	}

	public ProgramNode getTested() {
		return tested;
	}

	private ProgramNode createTestbench(ProgramNode component) {
		DeclarationTable table = new DeclarationTable();

		EntityNode entity = new EntityNode("testbench");
		table.setEntryName("testbench");

		Map<String, SignalDeclaration> testbenchSignals =
				createTestbenchSignals(component, table);
		ArchitectureNode arch = new ArchitectureNode("arch", testbenchSignals);
		table.setArchName("arch");

		table.addLabel("uut");
		List<String> map =
				testbenchSignals.entrySet().stream().map(Map.Entry::getKey)
						.collect(Collectors.toList());
		arch.addStatement(new PositionalMap("uut", component.getEntity().getName(), map));

		return new ProgramNode(entity, arch, table);
	}

	private Map<String, SignalDeclaration> createTestbenchSignals(
			ProgramNode component, DeclarationTable table) {
		Map<String, SignalDeclaration> testbenchSignals = new LinkedHashMap<>();

		component.getEntity().getDeclarations().forEach((name, declaration) -> {
			SignalDeclaration changedDeclaration =
					SignalDeclaration.changeType(declaration, SignalDeclaration.Type.INTERNAL);

			testbenchSignals.put(name, changedDeclaration);
			table.addSignal(name, changedDeclaration);
		});

		return testbenchSignals;
	}

	private void parseMappings(ProgramNode component, String label) {		createSignals(component.getArchitecture().getSignals(), label);

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
			table.addComponent(name, entry);

			addStatements(entry, name);

			parseMappings(entry, name);
		}
	}

	private void addStatements(ProgramNode component, String name) {
		component.getArchitecture().getStatements()
				.forEach(s -> table.addStatement(new SimulationStatement(name, s)));
	}

	private void createSignals(Map<String, SignalDeclaration> signals, String label) {
		signals.forEach((s, declaration) -> {
			Value value = declaration.createValue();
			table.addSignal(label + "/" + s, value);
		});
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
					"Invalid mapping of " + mappedEntry.getEntity().getName() + " in " + entry
							.getEntity().getName() + ".");
		}
	}

	private void checkAssociativeMapping(
			AssociativeMap mapping, ProgramNode entry, ProgramNode mappedEntry, String label) {

		Set<Map.Entry<String, SignalDeclaration>> entrySet =
				mappedEntry.getEntity().getDeclarations().entrySet();
		for (Map.Entry<String, SignalDeclaration> signal : entrySet) {
			String name = signal.getKey();
			if (!mapping.isMapped(name)) {
				throw new RuntimeException("Signal " + name + "is not mapped.");
			}

			String mappedTo = mapping.mappedTo(name);
			SignalDeclaration mappedToDeclaration =
					entry.getDeclarationTable().getDeclaration(mappedTo);

			if (!SignalDeclaration.checkMapping(mappedToDeclaration, signal.getValue())) {
				throw new RuntimeException("Signals are not compatibile.");
			}

			String alias = label + "/" + mapping.getLabel() + "/" + name;
			table.addAlias(alias, label + "/" + mappedTo);
		}
	}

	private void checkPositionalMapping(
			PositionalMap mapping, ProgramNode entry, ProgramNode mappedEntry, String label) {

		List<String> signals = mapping.getSignals();
		List<Map.Entry<String, SignalDeclaration>> declared =
				new ArrayList<>(mappedEntry.getEntity().getDeclarations().entrySet());

		for (int i = 0; i < signals.size(); i++) {
			SignalDeclaration declaration =
					entry.getDeclarationTable().getSignal(signals.get(i));

			if (!SignalDeclaration.checkMapping(declaration, declared.get(i).getValue())) {
				throw new RuntimeException("Signals are not compatibile.");
			}

			String alias = label + "/" + mapping.getLabel() + "/" + declared.get(i).getKey();
			table.addAlias(alias, label + "/" + signals.get(i));
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
