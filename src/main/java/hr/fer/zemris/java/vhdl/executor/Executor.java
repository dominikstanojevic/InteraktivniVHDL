package hr.fer.zemris.java.vhdl.executor;

import hr.fer.zemris.java.vhdl.executor.operators.ANDOperator;
import hr.fer.zemris.java.vhdl.executor.operators.BinaryOperator;
import hr.fer.zemris.java.vhdl.executor.operators.IOperator;
import hr.fer.zemris.java.vhdl.executor.operators.NANDOperator;
import hr.fer.zemris.java.vhdl.executor.operators.NOROperator;
import hr.fer.zemris.java.vhdl.executor.operators.NOTOperator;
import hr.fer.zemris.java.vhdl.executor.operators.OROperator;
import hr.fer.zemris.java.vhdl.executor.operators.UnaryOperator;
import hr.fer.zemris.java.vhdl.executor.operators.XOROperator;
import hr.fer.zemris.java.vhdl.lexer.Lexer;
import hr.fer.zemris.java.vhdl.models.Architecture;
import hr.fer.zemris.java.vhdl.models.Entity;
import hr.fer.zemris.java.vhdl.parser.Parser;
import hr.fer.zemris.java.vhdl.parser.nodes.ArchitectureNode;
import hr.fer.zemris.java.vhdl.parser.nodes.Constant;
import hr.fer.zemris.java.vhdl.parser.nodes.DeclarationNode;
import hr.fer.zemris.java.vhdl.parser.nodes.EntityNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ExpressionNode;
import hr.fer.zemris.java.vhdl.parser.nodes.IExpressionElement;
import hr.fer.zemris.java.vhdl.parser.nodes.INode;
import hr.fer.zemris.java.vhdl.parser.nodes.InputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.InvalidConstantException;
import hr.fer.zemris.java.vhdl.parser.nodes.Operator;
import hr.fer.zemris.java.vhdl.parser.nodes.OutputNode;
import hr.fer.zemris.java.vhdl.parser.nodes.ProgramNode;
import hr.fer.zemris.java.vhdl.parser.nodes.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * Created by Dominik on 27.7.2016..
 */
public class Executor {
	private static Map<String, IOperator> operators = new HashMap<>();

	static {
		operators.put("not", new NOTOperator());
		operators.put("and", new ANDOperator());
		operators.put("or", new OROperator());
		operators.put("xor", new XOROperator());
		operators.put("nand", new NANDOperator());
		operators.put("nor", new NOROperator());
	}

	private Entity entity;
	private Architecture architecture;

	private ProgramNode programNode;

	public Executor(ProgramNode programNode) {
		this.programNode = programNode;
		new Visitor().visitProgramNode(programNode);
	}

	public class Visitor implements IVisitor {

		@Override
		public void visitProgramNode(ProgramNode node) {
			for (INode child : node.getChildren()) {
				child.accept(this);
			}
		}

		@Override
		public void visitEntityNode(EntityNode node) {
			if (!node.getName().equals(node.getEnd())) {
				throw new ExecutorException("Different name of entity on the start and on "
											+ "the end of declaration.");
			}

			entity = new Entity(node.getName());

			for (DeclarationNode declaration : node.getDeclarations()) {
				declaration.accept(this);
			}
		}

		@Override
		public void visitInputNode(InputNode node) {
			for (Variable var : node.getVariables()) {
				try {
					entity.addInput(var.getName());
				} catch (PreviousDeclarationException e) {
					throw new ExecutorException(e.getMessage());
				}
			}
		}

		@Override
		public void visitOutputNode(OutputNode node) {
			for (Variable var : node.getVariables()) {
				try {
					entity.addOutput(var.getName());
				} catch (PreviousDeclarationException e) {
					throw new ExecutorException(e.getMessage());
				}
			}
		}

		@Override
		public void visitArchNode(ArchitectureNode node) {
			if (!node.getName().equals(node.getEnd())) {
				throw new ExecutorException(
						"Different name of architecture on the start and " + "on "
						+ "the end of declaration.");
			}

			if (!node.getEntity().equals(entity.getName())) {
				throw new ExecutorException(
						"Entity name and model name for architecture " + node.getName()
						+ "does not match.");
			}

			architecture = new Architecture(node.getName(), entity);

			for (ExpressionNode expression : node.getExpressions()) {
				expression.accept(this);
			}
		}

		@Override
		public void visitExpressionNode(ExpressionNode node) {
			String variable = node.getVariable().getName();

			if (entity.containsInput(variable)) {
				throw new ExecutorException("Cannot assign variable " + variable);
			}

			if (!entity.containsVariable(variable)) {
				throw new ExecutorException(
						"Variable " + node.getVariable().getName() + " " + "is not declared.");
			}

			node.getExpression().forEach(e -> {
				if (e instanceof Variable) {
					if (!entity.containsVariable(((Variable) e).getName())) {
						throw new ExecutorException(
								"Variable " + ((Variable) e).getName() + " is not declared.");
					}
				}
			});

			architecture.putExpression(variable, node.getExpression());
		}
	}

	public static void main(String[] args) {
		String program = "entity majority IS port ( A, B, C: in std_logic;\n\t\tY, Z:out "
						 + "std_logic\n);end majority;\n\nARCHITECTURE " + "concurrent of "
						 + "majority is\n\nbegin Y<= not (A or 'U') or (B or not A) and (C or "
						 + "A);\nZ<=A nand B or C;" + "\nend concurrent;";

		Lexer lexer = new Lexer(program);
		Parser parser = new Parser(lexer);
		Executor executor = new Executor(parser.getProgramNode());
		Map<String, Boolean> variables = executor.compute(getInputs());

		variables.forEach((k, v) -> System.out
				.println("Variable: " + k + ", value: " + (v == null ? "Unassigned" : v)));
	}

	private static Map<String, Boolean> getInputs() {
		Map<String, Boolean> inputs = new HashMap<>();
		inputs.put("A", true);
		inputs.put("B", true);
		inputs.put("C", false);

		return inputs;
	}

	public Map<String, Boolean> compute(Map<String, Boolean> inputs) {
		Map<String, Boolean> variables = addVariables(inputs, entity.getOutputs());

		Set<Map.Entry<String, Queue<IExpressionElement>>> expressions =
				architecture.getExpressions().entrySet();

		for (Map.Entry<String, Queue<IExpressionElement>> expression : expressions) {
			Boolean evaluation = evaluateExpression(variables, expression.getValue());
			variables.put(expression.getKey(), evaluation);
		}

		return variables;
	}

	private Boolean evaluateExpression(
			Map<String, Boolean> variables, Queue<IExpressionElement> expression) {
		Stack<Boolean> stack = new Stack<>();

		for (IExpressionElement element : expression) {
			if (element instanceof Variable) {
				stack.push(variables.get(((Variable) element).getName()));
				continue;
			}

			if (element instanceof Constant) {
				try {
					stack.push(((Constant) element).getConstant());
				} catch (InvalidConstantException e) {
					throw new ExecutorException(e.getMessage());
				}
				continue;
			}

			Operator operator = (Operator) element;
			if (operator.getName().equals("not")) {
				UnaryOperator unaryOperator = (UnaryOperator) operators.get("not");
				stack.push(unaryOperator.compute(stack.pop()));
				continue;
			}

			BinaryOperator binaryOperator = (BinaryOperator) operators.get(operator.getName());
			stack.push(binaryOperator.compute(stack.pop(), stack.pop()));
		}

		if (stack.size() != 1) {
			throw new ExecutorException("Illegal number of elements on stack expression.");
		}

		return stack.pop();
	}

	private Map<String, Boolean> addVariables(
			Map<String, Boolean> inputs, Set<String> outputs) {
		Map<String, Boolean> variables = new HashMap<>(inputs);

		for (String var : outputs) {
			variables.put(var, null);
		}

		return variables;
	}
}
