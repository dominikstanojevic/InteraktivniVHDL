package hr.fer.zemris.java.vhdl.models;

import hr.fer.zemris.java.vhdl.parser.nodes.IExpressionElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Dominik on 27.7.2016..
 */
public class Architecture {
	private String name;
	private Entity entity;
	private Map<String, Queue<IExpressionElement>> expressions;

	public Architecture(String name, Entity entity) {
		this.name = name;
		this.entity = entity;
	}

	public String getName() {
		return name;
	}

	public void putExpression(String variable, Queue<IExpressionElement> expression) {
		if(expressions == null) {
			expressions = new HashMap<>();
		}

		expressions.put(variable, expression);
	}

	public Map<String, Queue<IExpressionElement>> getExpressions() {
		return expressions;
	}
}
