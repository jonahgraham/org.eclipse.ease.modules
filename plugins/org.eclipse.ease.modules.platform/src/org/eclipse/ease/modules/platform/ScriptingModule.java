package org.eclipse.ease.modules.platform;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptable;
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

	public static final String MODULE_ID = "/System/Scripting";

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
		final Collection<EngineDescription> engines = scriptService.getEngines();
		for (final EngineDescription description : engines) {
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
		final List<String> result = new ArrayList<String>();

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		for (final EngineDescription description : scriptService.getEngines())
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
			final String location = ResourceTools.toAbsoluteLocation(resource, getScriptEngine().getExecutedFile());
			final ScriptType scriptType = scriptService.getScriptType(location);
			if (scriptType != null) {
				final List<EngineDescription> engines = scriptType.getEngines();
				if (!engines.isEmpty())
					engineID = engines.get(0).getID();
			}
		}

		if (engineID != null) {
			// engine found
			final IScriptEngine engine = scriptService.getEngineByID(engineID).createEngine();

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
				} catch (final IllegalArgumentException e) {
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
			final long stopWaitingTime = System.currentTimeMillis() + timeout;
			try {
				while (((Job) engine).getState() != Job.NONE) {
					final long now = System.currentTimeMillis();
					if (timeout == 0)
						Thread.sleep(1000);

					else if (stopWaitingTime > now)
						Thread.sleep(Math.min(stopWaitingTime - now, 1000));

					else
						// timeout depleted
						return false;
				}
			} catch (final InterruptedException e) {
				// we got interrupted - ev the current engine is shutting down?
				return false;
			}

			// job terminated
			return true;
		}

		// cannot evaluate engine state
		throw new RuntimeException("Cannot evaluate engine state");
	}

	/**
	 * Run a code fragment in a synchronized block. Executes <i>code</i> within a synchronized block on the <i>monitor</i> object. The code object might be a
	 * {@link String}, {@link File}, {@link IFile} or any other object that can be adapted to {@link IScriptable}.
	 *
	 * @param monitor
	 *            monitor to synchronize on
	 * @param code
	 *            code to run.
	 * @return
	 */
	@WrapToScript
	public Object executeSync(final Object monitor, final Object code) {
		synchronized (monitor) {
			return getScriptEngine().inject(code);
		}
	}

	/**
	 * Causes the current thread to wait until either another thread invokes the {@link java.lang.Object#notify()} method or the
	 * {@link java.lang.Object#notifyAll()} method for this object, or a specified amount of time has elapsed. Calls the java method monitor.wait(timeout).
	 *
	 * @param monitor
	 *            monitor to wait for
	 * @param timeout
	 *            max timeout (0 does not time out)
	 * @throws InterruptedException
	 *             when wait gets interrupted
	 */
	@WrapToScript
	public void wait(final Object monitor, @ScriptParameter(defaultValue = "0") final long timeout) throws InterruptedException {
		synchronized (monitor) {
			monitor.wait(timeout);
		}
	}

	/**
	 * Wakes up a single thread that is waiting on the monitor. Calls the java method monitor.notify().
	 *
	 * @param monitor
	 *            monitor to notify
	 */
	@WrapToScript
	public void notify(final Object monitor) {
		synchronized (monitor) {
			monitor.notify();
		}
	}

	/**
	 * Wakes up all threads that are waiting on the monitor. Calls the java method monitor.notifyAll().
	 *
	 * @param monitor
	 *            monitor to notify
	 */
	@WrapToScript
	public void notifyAll(final Object monitor) {
		synchronized (monitor) {
			monitor.notifyAll();
		}
	}
}
