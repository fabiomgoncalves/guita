package org.eclipselabs.guita.ipreviews.view;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

public class ClassLoading {

	private HashMap<String, Class<?>> classes_loaded;
	private static ClassLoading class_singleton;

	private ClassLoading(){
		classes_loaded = new HashMap<String, Class<?>>();
		loadClasses();
	}

	public static ClassLoading getInstance() {
		if(class_singleton == null)
			class_singleton = new ClassLoading();
		return class_singleton;
	}

	private void loadClasses() {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			loadClasses(workspace.getRoot().getLocation().toFile());
		}
		catch (IllegalStateException e){
			loadLocalClasses();
		}
	}


	private void loadClasses(File workspaceDirectory) {
		String sep = System.getProperty("file.separator");

		ArrayList<File> project_folders = new ArrayList<File>();
		for(File f : workspaceDirectory.listFiles()){
			if(!f.getName().startsWith(".") && f.isDirectory())
				project_folders.add(f);
		}

		HashMap<String, ArrayList<String>> classes_packages_names = new HashMap<String, ArrayList<String>>();
		for(File folder : project_folders){
			File f = new File(folder.getAbsolutePath() + sep + "bin");
			ArrayList<String> package_names = new ArrayList<String>();
			for(File fbin : f.listFiles()){
				getPackagesDirectories(package_names, fbin, "");
			}
			classes_packages_names.put(folder.getName(), package_names);
		}
		System.out.println("BADAAAAAAAAAAAAAM " + classes_packages_names);

		for(Entry<String, ArrayList<String>> entry: classes_packages_names.entrySet()){
			for(String packageName : entry.getValue()){
				File dir = new File(workspaceDirectory.getAbsolutePath() + sep + entry.getKey() + sep + "bin" + sep + packageName);
				FilenameFilter filter = new FilenameFilter() {
					@Override
					public boolean accept(File f, String name) {
						return name.endsWith(".class") && !name.contains("$");
					}
				};
				System.out.println(Arrays.toString(dir.list(filter)));
				for(String file : dir.list(filter)) {
					String className = file.split("\\.")[0];
					Class<?> aux = null;
					try {
						MyClassLoader loader = new MyClassLoader(ClassLoading.class.getClassLoader(), new File(dir.getAbsolutePath() + sep + className + ".class"));
						aux = loader.loadClass(packageName + sep + className);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException("Erro inesperado pois não foi criada uma classe existente em bin. String: " + packageName + "." + className);
					}
					if(aux != null)
						classes_loaded.put(aux.getCanonicalName(), aux);
				}
			}
		}
	}

	private void loadLocalClasses(){
		ArrayList<String> package_names = new ArrayList<String>();
		String sep = System.getProperty("file.separator");
		File src_dir = new File(System.getProperty("user.dir") + sep + "bin");
		for(File f : src_dir.listFiles()){
			getPackagesDirectories(package_names, f, "");
		}

		for(String packageName : package_names){
			File dir = new File(
					System.getProperty("user.dir") + sep + "\\bin\\" +
							packageName.replaceAll("\\.", sep));

			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File f, String name) {
					return name.endsWith(".class");
				}
			};
			for(String file : dir.list(filter)) {
				String className = file.split("\\.")[0];
				Class<?> aux = null;
				try {
					aux = Class.forName(packageName + "." + className);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Erro inesperado pois não foi criada uma classe existente em bin. String: " + packageName + "." + className);
				}
				classes_loaded.put(aux.getSimpleName(), aux);
			}
		}
	}

	public Class<?> getClass(String name){
		return classes_loaded.get(name);
	}

	public boolean containsClass(String name){
		return classes_loaded.containsKey(name);
	}

	private void getPackagesDirectories(ArrayList<String> package_names, File f, String nome) {
		if(f.isFile()){
			return;
		}
		ArrayList<File> aux = hasDirectory(f);
		if(f.isDirectory() && aux.size() != 0){
			for(File file_aux : aux){
				getPackagesDirectories(package_names, file_aux, nome + f.getName() + "\\");
			}
		}
		else {
			package_names.add(nome+f.getName());
		}
	}

	private ArrayList<File> hasDirectory(File f) {
		ArrayList<File> files = new ArrayList<File>();
		for(File f1 : f.listFiles()){
			if(f1.isDirectory())
				files.add(f1);
		}
		return files;
	}

	public List<Method> getMethods(Class<?> c) {
		List<Method> methods = new ArrayList<Method>();
		for (Method method : c.getMethods()) {
			methods.add(method);
		}
		return Collections.unmodifiableList(methods);
	}

	public void addClass(Class<?> c) {
		classes_loaded.put(c.getSimpleName(), c);
	}
}