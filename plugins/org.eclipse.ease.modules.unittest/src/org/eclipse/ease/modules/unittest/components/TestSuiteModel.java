/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.unittest.components;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public class TestSuiteModel implements IResourceChangeListener {

	public static final String CODE_LOCATION_TESTSUITE_SETUP = "TestSuite Setup";
	public static final String CODE_LOCATION_TESTSUITE_TEARDOWN = "TestSuite Teardown";
	public static final String CODE_LOCATION_TESTFILE_SETUP = "TestFile Setup";
	public static final String CODE_LOCATION_TESTFILE_TEARDOWN = "TestFile Teardown";
	public static final String CODE_LOCATION_TEST_SETUP = "Test Setup";
	public static final String CODE_LOCATION_TEST_TEARDOWN = "Test Teardown";

	public static final String FLAG_MAX_THREADS = "max threads";
	public static final String FLAG_STOP_SUITE_ON_FAILURE = "stop suite on failure";
	public static final String FLAG_PROMOTE_ERRORS_TO_FAILURES = "promote errors to failures";
	public static final String FLAG_EXECUTE_TEARDOWN_ON_FAILURE = "execute teardown on failure";

	private static final String XML_NODE_ROOT = "testsuite";
	private static final String XML_NODE_TESTFILES = "testfiles";
	private static final String XML_NODE_TESTFILE = "testfile";
	private static final String XML_NODE_VARIABLES = "variables";
	private static final String XML_NODE_VARIABLE = "variable";
	private static final String XML_NODE_CODE_FRAGMENTS = "codeFragments";
	private static final String XML_NODE_CODE_FRAGMENT = "codeFragment";
	private static final String XML_NODE_FLAGS = "flags";
	private static final String XML_NODE_FLAG = "flag";
	private static final String XML_NODE_DESCRIPTION = "description";
	private static final String XML_ATTRIBUTE_NAME = "name";
	private static final String XML_ATTRIBUTE_DESCRIPTION = "description";

	public class Variable {

		private String fDescription;
		private String fContent;
		private String fName;

		public Variable(final String identifier, final String content, final String description) {
			fName = identifier;
			fContent = content;
			fDescription = (description != null) ? description : "";
		}

		public String getContent() {
			return fContent;
		}

		public String getDescription() {
			return fDescription;
		}

		public String getName() {
			return fName;
		}

		public void setName(final String name) {
			fName = name;
		}

		public void setDescription(final String description) {
			fDescription = description;
		}

		public void setContent(final String content) {
			fContent = content;
		}
	}

	private final Collection<String> fTestFiles = new HashSet<String>();
	private final Map<String, String> fFlags = new HashMap<String, String>();
	private final List<Variable> fVariables = new ArrayList<Variable>();
	private final Map<String, String> fCodeFragments = new HashMap<String, String>();

	private final IFile fFile;

	private String fDescription = null;
	private boolean fDirty;

	public TestSuiteModel(final IFile file) throws IOException, CoreException {
		fFile = file;

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		fDirty = true;
		reload();
	}

	public TestSuiteModel() {
		fFile = null;

		fDirty = false;

		// set default flags
		setFlag(FLAG_MAX_THREADS, 1);
		setFlag(FLAG_PROMOTE_ERRORS_TO_FAILURES, false);
		setFlag(FLAG_STOP_SUITE_ON_FAILURE, false);
		setFlag(FLAG_EXECUTE_TEARDOWN_ON_FAILURE, true);
	}

	public void close() {
		// remove listener to allow garbage collection of this model
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public boolean getFlag(final String flagID, final boolean defaultValue) {
		final String value = fFlags.get(flagID);
		return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
	}

	public int getFlag(final String flagID, final int defaultValue) {
		final String value = fFlags.get(flagID);
		try {
			return (value != null) ? Integer.parseInt(value) : defaultValue;
		} catch (final NumberFormatException e) {
			return defaultValue;
		}
	}

	public void setFlag(final String flagID, final String content) {
		fFlags.put(flagID, content);
	}

	public void setFlag(final String flagID, final boolean content) {
		setFlag(flagID, Boolean.toString(content));
	}

	public void setFlag(final String flagID, final int content) {
		setFlag(flagID, Integer.toString(content));
	}

	public Collection<String> getTestFiles() {
		return fTestFiles;
	}

	public void load(final String text) {
		fTestFiles.clear();
		fVariables.clear();
		fCodeFragments.clear();
		fFlags.clear();

		try {
			final XMLMemento memento = XMLMemento.createReadRoot(new StringReader(text));

			// load testFiles
			final IMemento testsNode = memento.getChild(XML_NODE_TESTFILES);
			if (testsNode != null) {
				// load test files
				for (final IMemento node : testsNode.getChildren(XML_NODE_TESTFILE))
					fTestFiles.add(node.getTextData());
			}

			// load variables
			final IMemento variablesNode = memento.getChild(XML_NODE_VARIABLES);
			if (variablesNode != null) {
				for (final IMemento node : variablesNode.getChildren(XML_NODE_VARIABLE))
					fVariables.add(new Variable(node.getString(XML_ATTRIBUTE_NAME), node.getTextData(), node.getString(XML_ATTRIBUTE_DESCRIPTION)));
			}

			// load code fragments
			final IMemento codeFragmentsNode = memento.getChild(XML_NODE_CODE_FRAGMENTS);
			if (codeFragmentsNode != null) {
				for (final IMemento node : codeFragmentsNode.getChildren(XML_NODE_CODE_FRAGMENT))
					fCodeFragments.put(node.getString(XML_ATTRIBUTE_NAME), node.getTextData());
			}

			// load flags
			final IMemento flagsNode = memento.getChild(XML_NODE_FLAGS);
			if (flagsNode != null) {
				for (final IMemento node : flagsNode.getChildren(XML_NODE_FLAG))
					setFlag(node.getString(XML_ATTRIBUTE_NAME), node.getTextData());
			}

			// load description
			final IMemento descriptionNode = memento.getChild(XML_NODE_DESCRIPTION);
			fDescription = (descriptionNode != null) ? descriptionNode.getTextData() : "";

			fDirty = false;

		} catch (final WorkbenchException e) {
		}
	}

	public XMLMemento toMemento() {
		final XMLMemento memento = XMLMemento.createWriteRoot(XML_NODE_ROOT);

		// store test files
		final IMemento testsNode = memento.createChild(XML_NODE_TESTFILES);
		for (final String fileLocation : getTestFiles())
			testsNode.createChild(XML_NODE_TESTFILE).putTextData(fileLocation);
		;

		// store variables
		final IMemento variablesNode = memento.createChild(XML_NODE_VARIABLES);
		for (final Variable variable : getVariables()) {
			final IMemento node = variablesNode.createChild(XML_NODE_VARIABLE);
			node.putString(XML_ATTRIBUTE_NAME, variable.getName());
			node.putString(XML_ATTRIBUTE_DESCRIPTION, variable.getDescription());
			node.putTextData(variable.getContent());
		}

		// store code fragments
		final IMemento codesNode = memento.createChild(XML_NODE_CODE_FRAGMENTS);
		for (final Entry<String, String> entry : fCodeFragments.entrySet()) {
			final IMemento node = codesNode.createChild(XML_NODE_CODE_FRAGMENT);
			node.putString(XML_ATTRIBUTE_NAME, entry.getKey());
			node.putTextData(entry.getValue());
		}

		// store flags
		final IMemento flagsNode = memento.createChild(XML_NODE_FLAGS);
		for (final Entry<String, String> entry : fFlags.entrySet()) {
			final IMemento node = flagsNode.createChild(XML_NODE_FLAG);
			node.putString(XML_ATTRIBUTE_NAME, entry.getKey());
			node.putTextData(entry.getValue());
		}

		// store description
		memento.createChild(XML_NODE_DESCRIPTION).putTextData(getDescription());

		return memento;
	}

	public void addTestFile(final String fileLocation) {
		fTestFiles.add(fileLocation);
	}

	public IFile getFile() {
		return fFile;
	}

	public List<Variable> getVariables() {
		return fVariables;
	}

	public void addVariable(final String identifier, final String content, final String description) {
		fVariables.add(new Variable(identifier, content, description));
	}

	public String getCodeFragment(final String identifier) {
		return (fCodeFragments.containsKey(identifier)) ? fCodeFragments.get(identifier) : "";
	}

	public void setCodeFragment(final String identifier, final String code) {
		if ((code == null) || (code.isEmpty()))
			fCodeFragments.remove(identifier);
		else
			fCodeFragments.put(identifier, code);
	}

	public void setDescription(final String description) {
		fDescription = description;
	}

	public String getDescription() {
		return (fDescription != null) ? fDescription : "";
	}

	public void removeVariable(final Variable variable) {
		fVariables.remove(variable);
	}

	public void reload() throws IOException, CoreException {
		if (isDirty())
			load(ResourceTools.toString(fFile.getContents()));
	}

	private boolean isDirty() {
		return fDirty;
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {

				@Override
				public boolean visit(final IResourceDelta delta) throws CoreException {
					final IResource resource = delta.getResource();
					if (resource instanceof IContainer) {
						// only follow if it is a parent of the file
						return fFile.getFullPath().toString().startsWith(resource.getFullPath().toString());
					}

					if (resource.equals(fFile))
						fDirty = true;

					return false;
				}
			});
		} catch (final CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}
	}

	public Map<String, String> getCodeFragments() {
		return fCodeFragments;
	}
}
