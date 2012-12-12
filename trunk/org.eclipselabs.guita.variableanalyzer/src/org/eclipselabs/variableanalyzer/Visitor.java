package org.eclipselabs.variableanalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class Visitor extends ASTVisitor {
	private CompilationUnit unit;
	private Map<String, String> imports;
	private CodeBlock currentBlock;

	public Visitor(CompilationUnit unit) {
		this.unit = unit;
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

	public String resolveType(String varName, int line) {
		return currentBlock.resolveType(varName, line);
	}

	public List<String> resolveTypes(int line) {
		return currentBlock.resolveTypes(line);
	}
	
	private int line(ASTNode node) {
		return unit.getLineNumber(node.getStartPosition());
	}

	private class CodeBlock {
		Map<String, Map<Integer, String>> varTypes;

		CodeBlock parent;
		List<CodeBlock> children;

		CodeBlock() {
			this(null);
		}

		CodeBlock(CodeBlock parent) {
			this.parent = parent;
			varTypes = new LinkedHashMap<String, Map<Integer, String>>();
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
				addVar(name.getFullyQualifiedName(), line(name), type);
		}

		private String resolveType(String name) {
			if(varTypes.containsKey(name))
				return varTypes.get(name).values().iterator().next();
			else if(!isRoot())
				return parent.resolveType(name);
			else
				return null;
		}

		void addVar(VariableDeclaration var, String type) {
			String key = var.getName().getFullyQualifiedName();
			addVar(key, line(var), type);
		}

		private void addVar(String name, int line, String type) {
			if(!varTypes.containsKey(name))
				varTypes.put(name, new HashMap<Integer, String>());

			varTypes.get(name).put(line, expand(type));
		}

		public String resolveType(String varName, int line) {
			if(varTypes.containsKey(varName) && varTypes.get(varName).containsKey(line))
				return varTypes.get(varName).get(line);

			for(CodeBlock c : children) {
				String t = c.resolveType(varName, line);
				if(t != null)
					return t;
			}

			return null;
		}

		public List<String> resolveTypes(int line) {
			List<String> list = new ArrayList<String>();
			resolveTypesAux(line, list);
			return list;
		}

		private void resolveTypesAux(int line, List<String> list) {
			for(Entry<String, Map<Integer, String>> entry : varTypes.entrySet())
				if(entry.getValue().containsKey(line))
					list.add(entry.getValue().get(line));

			for(CodeBlock c : children)
				c.resolveTypesAux(line, list);
		}

		void print() {
			print(0);
		}

		private String tabs(int n) {
			char[] v = new char[n];
			for(int i = 0; i < n; i++)
				v[i] = '\t';
			return new String(v);
		}

		private void print(int level) {
			for(String var : varTypes.keySet())
				System.out.println(tabs(level) + var + " : " + varTypes.get(var));

			for(CodeBlock c : children)
				c.print(level+1);
		}
	}

	void printVars() {
		currentBlock.print();
	}







	private String expand(String type) {
		if(imports.containsKey(type))
			return imports.get(type);
		else
			return type;
	}
	
	private void checkSimpleNameVar(Object o) {
		if(o instanceof SimpleName)
			currentBlock.addVar((SimpleName) o);
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
				currentBlock.addVar(var, n.getType().toString());
			}
		}

		return true;
	}

	
	@Override
	public boolean visit(Assignment n) {
		checkSimpleNameVar(n.getLeftHandSide());
		checkSimpleNameVar(n.getRightHandSide());
		return true;
	}

	
	@Override
	public boolean visit(ClassInstanceCreation n) {
		for(Object o : n.arguments())
			checkSimpleNameVar(o);
	
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation n) {
		checkSimpleNameVar(n.getExpression());
		for(Object o : n.arguments())
			checkSimpleNameVar(o);

		return true;
	}

	@Override
	public boolean visit(ReturnStatement n) {
		checkSimpleNameVar(n.getExpression());
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration n) {
		//		System.out.println("->"+ n +  " :: " + n.getName() + " " + n.getType());
		currentBlock.addVar(n, n.getType().toString());
		return true;
	};
}

