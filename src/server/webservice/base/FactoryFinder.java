package server.webservice.base;

/*
 * Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import javax.xml.ws.WebServiceException;

public class FactoryFinder {

	/**
	 * Creates an instance of the specified class using the specified 
	 * <code>ClassLoader</code> object.
	 *
	 * @exception WebServiceException if the given class could not be found
	 *            or could not be instantiated
	 */
	private static Object newInstance(String className, ClassLoader classLoader) {
		try {
			Class spiClass = safeLoadClass(className, classLoader);
			return spiClass.newInstance();
		} catch (ClassNotFoundException x) {
			throw new WebServiceException("Provider " + className
					+ " not found", x);
		} catch (Exception x) {
			throw new WebServiceException("Provider " + className
					+ " could not be instantiated: " + x, x);
		}
	}

	/**
	 * Finds the implementation <code>Class</code> object for the given
	 * factory name, or if that fails, finds the <code>Class</code> object
	 * for the given fallback class name. The arguments supplied MUST be
	 * used in order. If using the first argument is successful, the second
	 * one will not be used.
	 * <P>
	 * This method is package private so that this code can be shared.
	 *
	 * @return the <code>Class</code> object of the specified message factory;
	 *         may not be <code>null</code>
	 *
	 * @param factoryId             the name of the factory to find, which is
	 *                              a system property
	 * @param fallbackClassName     the implementation class name, which is
	 *                              to be used only if nothing else
	 *                              is found; <code>null</code> to indicate that
	 *                              there is no fallback class name
	 * @exception WebServiceException if there is an error
	 */
	public static Object find(String factoryId, String fallbackClassName) {
		if (fallbackClassName == null) {
			throw new WebServiceException("Provider for " + factoryId
					+ " cannot be found", null);
		}
		ClassLoader classLoader;
		try {
			classLoader = Thread.currentThread().getContextClassLoader();
		} catch (Exception x) {
			throw new WebServiceException(x.toString(), x);
		}
		return newInstance(fallbackClassName, classLoader);
	}

	private static final String PLATFORM_DEFAULT_FACTORY_CLASS = "com.sun.xml.internal.ws.spi.ProviderImpl";

	/**
	 * Loads the class, provided that the calling thread has an access to the class being loaded.
	 */
	private static Class safeLoadClass(String className, ClassLoader classLoader)
			throws ClassNotFoundException {
		try {
			// make sure that the current thread has an access to the package of the given name.
			SecurityManager s = System.getSecurityManager();
			if (s != null) {
				int i = className.lastIndexOf('.');
				if (i != -1) {
					s.checkPackageAccess(className.substring(0, i));
				}
			}

			if (classLoader == null)
				return Class.forName(className);
			else
				return classLoader.loadClass(className);
		} catch (SecurityException se) {
			// anyone can access the platform default factory class without permission
			if (PLATFORM_DEFAULT_FACTORY_CLASS.equals(className))
				return Class.forName(className);
			throw se;
		}
	}

}
