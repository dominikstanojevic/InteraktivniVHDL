package hr.fer.zemris.java.vhdl.parser.nodes.statements.mapping;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;
import hr.fer.zemris.java.vhdl.parser.nodes.statements.Statement;

import java.util.List;
import java.util.Map;

/**
 * Created by Dominik on 21.2.2017..
 */
public abstract class Mapping extends Statement {
    private String entity;

    protected Mapping(String label, String entity) {
        super(label);
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }

    public static class Positional extends Mapping {
        private List<Mappable> mapped;

        private Positional(String label, String entity) {
            super(label, entity);
        }

        public Positional(String label, String entity, List<Mappable> mapped) {
            this(label, entity);
            this.mapped = mapped;
        }

        public List<Mappable> getMapped() {
            return mapped;
        }
    }

    public static class Associative extends Mapping {
        private Map<Declaration, Mappable> mapped;

        private Associative(String label, String entity) {
            super(label, entity);
        }

        public Associative(String label, String entity, Map<Declaration, Mappable> mapped) {
            this(label, entity);
            this.mapped = mapped;
        }

        public Map<Declaration, Mappable> getMapped() {
            return mapped;
        }
    }
}
