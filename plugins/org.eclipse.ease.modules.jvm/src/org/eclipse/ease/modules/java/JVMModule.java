package org.eclipse.ease.modules.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.eclipse.ease.lang.jvm.compiled.JVMCompiledScriptEngine;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This module provides access to class files compiled in the current workspace. Typically these files will be derived from .java files, but this module accepts
 * any source that compiles to .class files.
 */
public class JVMModule extends AbstractScriptModule {

	/**
	 * Create an instance of a given JRE resource. The default constructor is used to create the class instance.
	 *
	 * @param location
	 *            file location to look for
	 * @return class instance
	 * @throws ClassNotFoundException
	 *             when class cannot be found
	 * @throws JavaModelException
	 *             when the workspace project model is invalid
	 * @throws MalformedURLException
	 *             when references in the project are invalid
	 * @throws IllegalAccessException
	 *             when class access is not granted
	 * @throws InstantiationException
	 *             when class cannot be instantiated
	 */
	@WrapToScript
	public Object createInstance(final String location) throws ClassNotFoundException, JavaModelException, MalformedURLException, InstantiationException,
	IllegalAccessException {
		Class<?> clazz = compile(location);
		return (clazz != null) ? clazz.newInstance() : null;
	}

	/**
	 * Invoke a static method on a given JRE resource.
	 *
	 * @param location
	 *            file location to look for
	 * @param methodName
	 *            method to invoke
	 * @return invoked method result
	 * @throws ClassNotFoundException
	 *             when class cannot be found
	 * @throws JavaModelException
	 *             when the workspace project model is invalid
	 * @throws MalformedURLException
	 *             when references in the project are invalid
	 * @throws SecurityException
	 *             If a security manager, s, is present and the caller's class loader is not the same as or an ancestor of the class loader for the current
	 *             class and invocation of s.checkPackageAccess() denies access to the package of this class.
	 * @throws NoSuchMethodException
	 *             if a matching method is not found or if the name is "<init>"or "<clinit>".
	 * @throws InvocationTargetException
	 *             if the underlying method throws an exception.
	 * @throws IllegalArgumentException
	 *             if the method is an instance method and the specified object argument is not an instance of the class or interface declaring the underlying
	 *             method (or of a subclass or implementor thereof); if the number of actual and formal parameters differ; if an unwrapping conversion for
	 *             primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal parameter type
	 *             by a method invocation conversion.
	 * @throws IllegalAccessException
	 *             if this Method object is enforcing Java language access control and the underlying method is inaccessible.
	 */
	@WrapToScript
	public Object invokeStatic(final String location, final String methodName) throws ClassNotFoundException, JavaModelException, MalformedURLException,
	NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Class<?> clazz = compile(location);

		Method method = clazz.getMethod(methodName);
		return method.invoke(null);
	}

	/**
	 * Retrieve class for a given JRE resource.
	 *
	 * @param location
	 *            file location to look for
	 * @return class instance
	 * @throws ClassNotFoundException
	 *             when class cannot be found
	 * @throws JavaModelException
	 *             when the workspace project model is invalid
	 * @throws MalformedURLException
	 *             when references in the project are invalid
	 */
	@WrapToScript
	public Class<?> compile(final Object location) throws ClassNotFoundException, JavaModelException, MalformedURLException {
		return JVMCompiledScriptEngine.loadClass(location);
	}
}
