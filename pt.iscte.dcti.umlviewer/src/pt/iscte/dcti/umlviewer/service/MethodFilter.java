package pt.iscte.dcti.umlviewer.service;

import java.lang.reflect.Method;

public interface MethodFilter {

	boolean accept(Method m);
}
