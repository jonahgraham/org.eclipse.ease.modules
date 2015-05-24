/*******************************************************************************
 * Copyright (c) 2015 CNES and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JF Rolland (Atos) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.modules.modeling.ui.matchers.IMatcher;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MatcherRegistry {
	static final String EXT_ID = "matcher";
	static List<IMatcher> MATCHERS = doGetMatchers();

	public static List<IMatcher> getMatchers() {
		return MATCHERS;
	}

	private static List<IMatcher> doGetMatchers() {
		IConfigurationElement[] extensions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(Activator.PLUGIN_ID, EXT_ID);
		return Lists.newArrayList(Iterables.transform(
				Arrays.asList(extensions),
				new Function<IConfigurationElement, IMatcher>() {
					@Override
					public IMatcher apply(IConfigurationElement arg0) {
						try {
							return (IMatcher) arg0
									.createExecutableExtension("instance");
						} catch (CoreException e) {
							e.printStackTrace();
						}
						return new IMatcher() {

							@Override
							public String getText() {
								return "error";
							}

							@Override
							public String getHelp() {
								return "";
							}

							@Override
							public List<EObject> getElements(String string,
									IEditingDomainProvider currentEditor) {
								return Lists.newArrayList();
							}
						};
					}
				}));
	}
}
