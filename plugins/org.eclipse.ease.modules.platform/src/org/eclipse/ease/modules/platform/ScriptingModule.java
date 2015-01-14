package org.eclipse.ease.modules.platform;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.PlatformUI;

/**
 * Commands to launch additional script engines.
 */
public class ScriptingModule extends AbstractScriptModule {

	/**
	 * Create a new script engine instance.
	 *
	 * @param identifier
	 *            engine ID, literal engine name or accepted file extension
	 * @return script engine instance (not started) or <code>null</code>
	 */
	@WrapToScript
	public static IScriptEngine createScriptEngine(final String identifier) {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);

		// by ID
		EngineDescription engine = scriptService.getEngineByID(identifier);
		if (engine != null)
			return engine.createEngine();

		// by literal name
		Collection<EngineDescription> engines = scriptService.getEngines();
		for (EngineDescription description : engines) {
			if (description.getName().equals(identifier))
				return description.createEngine();
		}

		// by script type
		engine = scriptService.getEngine(identifier);
		if (engine != null)
			return engine.createEngine();

		// giving up
		return null;
	}

	/**
	 * Retrieve a list of available script engines.
	 *
	 * @return array of engine IDs
	 */
	@WrapToScript
	public static String[] listScriptEngines() {
		List<String> result = new ArrayList<String>();

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		for (EngineDescription description : scriptService.getEngines())
			result.add(description.getID());

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Fork a new script engine and execute provided resource.
	 *
	 * @param resource
	 *            resource to execute (path, URI or file instance)
	 * @param arguments
	 *            optional script arguments
	 * @param engineID
	 *            engine ID to be used
	 * @return script engine instance or <code>null</code> in case of error
	 */
	@WrapToScript
	public IScriptEngine fork(final Object resource, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String arguments,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) String engineID) {

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);

		if (engineID == null) {
			// try to find engine for script type
			String location = ResourceTools.toAbsoluteLocation(resource, getScriptEngine().getExecutedFile());
			ScriptType scriptType = scriptService.getScriptType(location);
			if (scriptType != null) {
				List<EngineDescription> engines = scriptType.getEngines();
				if (!engines.isEmpty())
					engineID = engines.get(0).getID();
			}
		}

		if (engineID != null) {
			// engine found
			IScriptEngine engine = scriptService.getEngineByID(engineID).createEngine();

			// connect streams
			engine.setOutputStream(getScriptEngine().getOutputStream());
			engine.setErrorStream(getScriptEngine().getErrorStream());
			engine.setInputStream(getScriptEngine().getInputStream());
			engine.setCloseStreamsOnTerminate(false);

			// set input parameters
			engine.setVariable("argv", AbstractScriptEngine.extractArguments(arguments));

			Object scriptObject = ResourceTools.resolveFile(resource, getScriptEngine().getExecutedFile(), true);
			if (scriptObject == null) {
				try {
					// no file available, try to resolve URI
					scriptObject = URI.create(resource.toString());
				} catch (IllegalArgumentException e) {
					// could not resolve URI, giving up
					return null;
				}
			}

			engine.executeAsync(scriptObject);
			engine.schedule();
			return engine;
		}

		return null;
	}

	/**
	 * Wait for a script engine to shut down. If <i>timeout</i> is set to 0 this method will wait endlessly.
	 *
	 * @param engine
	 *            script engine to wait for
	 * @param timeout
	 *            time to wait for shutdown [ms]
	 * @return <code>true</code> when engine is shut down
	 */
	@WrapToScript
	public static boolean join(final IScriptEngine engine, @ScriptParameter(defaultValue = "0") final long timeout) {
		if (engine instanceof Job) {
			long stopWaitingTime = System.currentTimeMillis() + timeout;
			try {
				while (((Job) engine).getState() != Job.NONE) {
					long now = System.currentTimeMillis();
					if (timeout == 0)
						Thread.sleep(1000);

					else if (stopWaitingTime > now)
						Thread.sleep(Math.min(stopWaitingTime - now, 1000));

					else
						// timeout depleted
						return false;
				}
			} catch (InterruptedException e) {
				// we got interrupted - ev the current engine is shutting down?
				return false;
			}

			// job terminated
			return true;
		}

		// cannot evaluate engine state
		throw new RuntimeException("Cannot evaluate engine state");
	}
}
