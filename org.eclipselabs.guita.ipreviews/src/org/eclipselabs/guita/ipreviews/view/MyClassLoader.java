package org.eclipselabs.guita.ipreviews.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.Bundle;

public class MyClassLoader extends ClassLoader{
        private File file;
		String sep = System.getProperty("file.separator");
        public MyClassLoader(ClassLoader parent, File file) {
            super(parent);
            this.file = file;
        }
       
        public Class<?> loadClass(String name) throws ClassNotFoundException {
//        	if(ClassLoading.getInstance().containsClass(name.replaceAll("\\"+sep, "."))){
//        		return ClassLoading.getInstance().getClass(name.replaceAll("\\"+sep, "."));
//        	}
        	
            if(name.startsWith("java."))
                return getParent().loadClass(name);
            
            name = name.replaceAll("\\"+sep, ".");
           
            byte[] classData = null;
            try {
//                classData = getBytesFromFile(file);
            	Path path = Paths.get(file.getPath());
            	classData = Files.readAllBytes(path);
            } catch (IOException e) {                   
                e.printStackTrace();
            }
            Class<?> clazz = null;
            System.out.println(file.toString() + " --- " + name);
            try {
            	clazz = defineClass(name, classData, 0, classData.length);
            }
            catch(NoClassDefFoundError e){
            	try{
            		clazz = Class.forName(name);
            	}
            	catch(ClassNotFoundException f){
            		return null;
            	}
            }
            return clazz;
        }
    }