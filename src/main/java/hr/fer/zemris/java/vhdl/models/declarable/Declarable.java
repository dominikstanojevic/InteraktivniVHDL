package hr.fer.zemris.java.vhdl.models.declarable;

import hr.fer.zemris.java.vhdl.models.declarations.Declaration;

/**
 * Created by Dominik on 25.8.2016..
 */
public interface Declarable<T extends Declaration> {
	enum DeclarationType {
		PORT, SIGNAL
	}

	DeclarationType getDeclarationType();
	String getName();
	T getDeclaration();
}
