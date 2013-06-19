package org.eclipselabs.guita.variableanalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Expression;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipselabs.guita.variableanalyzer.service.TypeFilter;
import org.eclipselabs.guita.variableanalyzer.service.VariableInfo;

public class Visitor extends ASTVisitor {
	private CompilationUnit unit;
	private Map<String, String> imports;
	private CodeBlock currentBlock;
	private TypeFilter filter;

	public Visitor(CompilationUnit unit, TypeFilter filter) {
		this.unit = unit;
		this.filter = filter;

		imports = new HashMap<String, String>();
		currentBlock = new CodeBlock();

		// include all field declarations first
		unit.accept(new ASTVisitor() {
			@Override
			public boolean visit(FieldDeclaration n) {
				for(Object o : n.fragments()) {
					if(o instanceof VariableDeclarationFragment) {
						VariableDeclarationFragment var = (VariableDeclarationFragment) o;
						currentBlock.addVar(var, n.getType().toString());
					}
				}
				return true;
			}
		});
	}

	public VariableInfo resolveType(String varName, int line) {
		return currentBlock.resolveType(varName, line);
	}

	private int line(ASTNode node) {
		return unit.getLineNumber(node.getStartPosition());
	}

	private class Variable {
		final String name;
		final String type;

		public Variable(String name, String type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String toString() {
			return name + " : " + type;
		}
	}

	private class CodeBlock {
		// line number -> ("var1",Label), ("var1",Label), ("var2",Button)
		Map<Integer, List<Variable>> vars;

		CodeBlock parent;
		List<CodeBlock> children;

		CodeBlock() {
			this(null);
		}

		CodeBlock(CodeBlock parent) {
			this.parent = parent;
			vars = new LinkedHashMap<Integer, List<Variable>>();

			children = Collections.emptyList();
			if(parent != null)
				parent.addChild(this);
		}

		boolean isRoot() {
			return parent == null;
		}

		void addChild(CodeBlock b) {
			if(children.isEmpty())
				children = new ArrayList<Visitor.CodeBlock>();
			children.add(b);
		}

		void addVar(SimpleName name) {
			String type =  resolveType(name.getFullyQualifiedName());
			if(type != null)
				addVar(line(name), new Variable(name.getFullyQualifiedName(), type));
		}

		private String resolveType(String name) {
			for(Integer i : vars.keySet()) {
				for(Variable v : vars.get(i)) {
					if(v.name.equals(name))
						return v.type;
				}
			}
			return isRoot() ? null : parent.resolveType(name);
		}



		void addVar(VariableDeclaration var, String type) {
			String name = var.getName().getFullyQualifiedName();
			addVar(line(var), new Variable(name, expand(type)));
		}

		private void addVar(int line, Variable var) {
			if(filter.include(var.type)) {
				if(!vars.containsKey(line))
					vars.put(line, new ArrayList<Variable>());

				vars.get(line).add(var);
			}
		} 

		public VariableInfo resolveType(String varName, int line) {
			if(vars.containsKey(line)) {
				int order = 0;
				for(Variable v : vars.get(line)) {
					if(v.name.equals(varName))
						return new VariableInfo(
								v.type, 
								order, 
								numberOfVariables(line), 
								numberOfVariablesWithType(line, v.type),
								numberOfVariablesWithName(line, v.name));

					order++;
				}
			}

			for(CodeBlock c : children) {
				VariableInfo info = c.resolveType(varName, line);
				if(info != null)
					return info;
			}

			return null;
		}


		public int numberOfVariables(int line) {
			if(!vars.containsKey(line))
				throw new IllegalArgumentException("no key");

			return vars.get(line).size();
		}

		public int numberOfVariablesWithType(int line, String type) {
			if(!vars.containsKey(line))
				throw new IllegalArgumentException("no key");

			int count = 0;
			for(Variable var : vars.get(line)) {
				if(var.type.equals(type))
					count++;
			}
			return count;
		}

		public int numberOfVariablesWithName(int line, String name) {
			if(!vars.containsKey(line))
				throw new IllegalArgumentException("no key");

			int count = 0;
			for(Variable var : vars.get(line)) {
				if(var.name.equals(name))
					count++;
			}
			return count;
		}

		private String tabs(int n) {
			char[] v = new char[n];
			for(int i = 0; i < n; i++)
				v[i] = '\t';
			return new String(v);
		}

		private void print(int level) {
			for(Integer line : vars.keySet()) {
				System.out.print(tabs(level) + line + " -> ");
				for(Variable v : vars.get(line))
					System.out.print(v + ", ");

				System.out.println();
			}

			for(CodeBlock c : children)
				c.print(level+1);
		}
	}

	void printVars() {
		currentBlock.print(0);
	}







	private String expand(String type) {
		if(imports.containsKey(type))
			return imports.get(type);
		else
			return type;
	}

	private void checkArgument(Object o) {
		if(o instanceof SimpleName)
			currentBlock.addVar((SimpleName) o);
		else if(o instanceof MethodInvocation) {
			Expression e = ((MethodInvocation) o).getExpression();
			if(e instanceof SimpleName)
				currentBlock.addVar((SimpleName) e);
		}
	}






	@Override
	public boolean visit(ImportDeclaration n) {
		String name = n.getName().getFullyQualifiedName();
		imports.put(name.substring(name.lastIndexOf('.')+1), name);
		return true;
	}


	@Override
	public boolean visit(Block n) {
		currentBlock = new CodeBlock(currentBlock);
		return true;
	}

	@Override
	public void endVisit(Block node) {
		if(!currentBlock.isRoot())
			currentBlock = currentBlock.parent;
	}


	@Override
	public boolean visit(VariableDeclarationStatement n) {
		//		System.out.println("*>"+ n + " " + line(n));
		for(Object o : n.fragments()) {
			if(o instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment var = (VariableDeclarationFragment) o;
				Expression e = var.getInitializer();
				boolean visit = handleRightHandExpression(e);
				Variable leftVar = new Variable(var.getName().getFullyQualifiedName(), n.getType().toString());
				currentBlock.addVar(line(var), leftVar);
				return visit;
			}
		}

		return true;
	}

	private boolean handleRightHandExpression(Expression e) {
		if(e instanceof MethodInvocation) {
			visit((MethodInvocation) e);
			return false;
		}
		else if(e instanceof ClassInstanceCreation) {
			visit((ClassInstanceCreation) e);
			return false;
		}
		else if(e instanceof SimpleName) {
			currentBlock.addVar((SimpleName) e);
			return false;
		}
		else {
			return true;
		}
	}







	@Override
	public boolean visit(Assignment n) {
		System.out.println("ASS>"+ n + " " + line(n));
		boolean visit = handleRightHandExpression(n.getRightHandSide());
		if(n.getLeftHandSide() instanceof SimpleName) {
			String name = ((SimpleName) n.getLeftHandSide()).getFullyQualifiedName();
			String type = currentBlock.resolveType(name);
			currentBlock.addVar(line(n.getLeftHandSide()), new Variable(name, type));
		}
		return visit;
	}


	@Override
	public boolean visit(ClassInstanceCreation n) {
		for(Object o : n.arguments())
			checkArgument(o);

		return true;
	}

	@Override
	public boolean visit(MethodInvocation n) {
		for(Object o : n.arguments())
			checkArgument(o);

		checkArgument(n.getExpression());
		return true;
	}

	@Override
	public boolean visit(ReturnStatement n) {
		checkArgument(n.getExpression());
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration n) {
		//		System.out.println("->"+ n +  " :: " + n.getName() + " " + n.getType());
		currentBlock.addVar(n, n.getType().toString());
		return true;
	};
}

