package hr.fer.zemris.java.vhdl.models.declarable;

import hr.fer.zemris.java.vhdl.models.declarations.PortDeclaration;

import java.util.Objects;

/**
 * Created by Dominik on 25.8.2016..
 */
public class Port implements Declarable<PortDeclaration> {
	private String name;
	private PortDeclaration declaration;

	public Port(String name, PortDeclaration declaration) {
		Objects.requireNonNull(name, "Port name cannot be null.");
		Objects.requireNonNull(declaration, "Port declaration cannot be null");

		this.name = name;
		this.declaration = declaration;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Port port = (Port) o;

		return name.equals(port.name);

	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name.toString();
	}

	@Override
	public DeclarationType getDeclarationType() {
		return DeclarationType.PORT;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public PortDeclaration getDeclaration() {
		return declaration;
	}
}
