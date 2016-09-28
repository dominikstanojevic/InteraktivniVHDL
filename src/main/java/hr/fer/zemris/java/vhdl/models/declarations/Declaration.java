package hr.fer.zemris.java.vhdl.models.declarations;

import hr.fer.zemris.java.vhdl.models.values.Value;

/**
 * Created by Dominik on 25.8.2016..
 */
public interface Declaration {
	Value.TypeOf getTypeOf();
	int size();
	Declaration convertToScalar();

	static boolean checkMapping(
			PortDeclaration d1, Declaration d2) {

		if (d1 == null) {
			return false;
		}

		if (d2 == null) {
			return true;
		}

		if (d2 instanceof PortDeclaration) {
			if (d1.getPortType() != ((PortDeclaration) d2).getPortType()) {
				return false;
			}
		}

		if (d1.getTypeOf() != d2.getTypeOf()) {
			return false;
		}

		if (d1.getTypeOf() == Value.TypeOf.STD_LOGIC_VECTOR) {
			if (d1.size() != d2.size()) {
				return false;
			}
		}

		return true;
	}

	Integer getStart();
}
