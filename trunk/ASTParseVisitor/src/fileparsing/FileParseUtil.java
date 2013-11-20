package fileparsing;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
 
public class FileParseUtil {
 
	private final CompilationUnit unit;
	
	public FileParseUtil(String filePath, String className) throws IOException {
		String source = readFileToString(filePath);
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		parser.setEnvironment(new String[0], new String[0], null, true);
		parser.setUnitName(className);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		unit = (CompilationUnit) parser.createAST(null);
	}
	
	public CompilationUnit getCompilationUnit() {
		return unit;
	}
	
	public void parse(ASTVisitor visitor) {
		unit.accept(visitor);
	}
 
	public static String readFileToString(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return  fileData.toString();	
	}
}