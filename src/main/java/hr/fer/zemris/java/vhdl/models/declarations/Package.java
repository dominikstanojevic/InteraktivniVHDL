package hr.fer.zemris.java.vhdl.models.declarations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Package {
    private String name;
    private String library;
    private Map<String, Boolean> types;

    public Package(String name, String library, Set<String> types) {
        this.name = name;
        this.library = library;

        this.types = new HashMap<>();
        types.forEach(t -> this.types.put(t, false));
    }

    public String getName() {
        return name;
    }

    public String getLibrary() {
        return library;
    }

    public Set<String> getTypes() {
        return types.keySet();
    }

    public boolean isTypeUsed(String type) {
        return types.get(type);
    }

    public void setUsed(String type) {
        if (type.equals("all")) {
            for(String t : types.keySet()) {
                types.put(t, true);
            }
        } else if(!types.keySet().contains(type)) {
            throw new RuntimeException("Invalid type for given package.");
        }

        types.put(type, true);
    }

    public static Package getStdLogicPackage() {
        String libraryName = "ieee";
        Set<String> types = new HashSet<>();
        types.add("std_logic");
        types.add("std_logic_vector");

        return new Package("std_logic_1164", libraryName, types);
    }
}
