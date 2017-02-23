package hr.fer.zemris.java.vhdl.hierarchy;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.models.values.VectorData;
import hr.fer.zemris.java.vhdl.models.values.VectorOrder;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.AddressStatement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Dominik on 22.2.2017..
 */
public class Model {
    private String name;
    private String label;
    private Map<Declaration, Integer> addresses = new HashMap<>();
    private Map<Declaration, Declaration> mapped;
    private Set<AddressStatement> statements;
    private Model parent;
    private Set<Model> models;

    public Model(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public Map<Declaration, Integer> getAddresses() {
        return addresses;
    }

    public Set<AddressStatement> getStatements() {
        return statements;
    }

    public Set<Model> getModels() {
        return models;
    }

    public void addSignal(Declaration declaration, Integer id) {
        if (addresses == null) {
            addresses = new HashMap<>();
        }

        addresses.put(declaration, id);
    }

    public void addModel(Model model) {
        if (models == null) {
            models = new HashSet<>();
        }

        models.add(model);
    }

    public Integer getSignalData(String name) {
        String fullName = label + "/" + name;
        return addresses.get(fullName);
    }

    public String getLabel() {
        return label;
    }

    public void setParent(Model parent) {
        this.parent = parent;
    }

    public void addMapped(Declaration port, Declaration mappedTo) {
        if (mapped == null) {
            mapped = new HashMap<>();
        }

        mapped.put(port, mappedTo);
    }

    public void addStatement(AddressStatement statement) {
        if (statements == null) {
            statements = new HashSet<>();
        }

        statements.add(statement);
    }

    public int[] getAddresses(Declaration declaration) {
        VectorData data = declaration.getVectorData();
        int[] addresses = new int[data.getSize()];
        for (int i = 0, size = data.getSize(), start = data.getStart(); i < size; i++) {
            int index = data.getOrder() == VectorOrder.TO ? start + i : start - i;
            addresses[i] = findSignal(this, declaration.getLabel(), new VectorData(index, null, index),
                    new VectorData.Offset(0, 0));
        }
        return addresses;
    }

   /* private int getAddress(String label, VectorData data) {
        Optional<Declaration> origin = addresses.keySet().stream().filter(d -> d.getLabel().equals(label)).findAny();
        if (origin.isPresent()) {
            return origin.get().getVectorData().getAddress(data, addresses.get(origin.get())).getStart();
        } else {
            origin = mapped.keySet().stream()
                    .filter(d -> d.getLabel().equals((label)) && data.isValid(d.getVectorData())).findAny();
            if (!origin.isPresent()) {
                throw new RuntimeException("Cannot find declaration.");
            }

            VectorData.Offset offset = origin.get().getVectorData().calculateOffset(data);
            return findSignal(this.parent, mapped.get(origin.get()), offset);
        }
    }
*/
    private int findSignal(Model model, String label, VectorData data, VectorData.Offset offset) {
        VectorData newData = data.getFromOffset(offset);

        Optional<Declaration> origin =
                model.addresses.keySet().stream().filter(d -> d.getLabel().equals(label)).findAny();
        if (origin.isPresent()) {
            return origin.get().getVectorData().getAddress(newData, model.addresses.get(origin.get())).getStart();
        } else {
            origin = model.mapped.keySet().stream()
                    .filter(d -> d.getLabel().equals((label)) && newData.isValid(d.getVectorData())).findAny();
            if (!origin.isPresent()) {
                throw new RuntimeException("Cannot find declaration.");
            }

            offset = origin.get().getVectorData().calculateOffset(newData);
            return findSignal(model.parent, model.mapped.get(origin.get()).getLabel(),
                    model.mapped.get(origin.get()).getVectorData(), offset);
        }
    }
}
