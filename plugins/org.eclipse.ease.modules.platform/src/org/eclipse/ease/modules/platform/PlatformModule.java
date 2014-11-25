package org.eclipse.ease.modules.platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;

public class PlatformModule {

	/**
	 * Adapt object to target type. Try to get an adapter for an object.
	 *
	 * @param source
	 *            object to adapt
	 * @param target
	 *            target class to adapt to
	 * @return adapted object or <code>null</code>
	 */
	@WrapToScript
	public Object adapt(final Object source, final Class<?> target) {
		return Platform.getAdapterManager().getAdapter(source, target);
	}

	/**
	 * Get a platform service.
	 *
	 * @param type
	 *            service type
	 * @return service instance or <code>null</code>
	 */
	@WrapToScript
	public Object getService(final Class<?> type) {
		return PlatformUI.getWorkbench().getService(type);
	}

	/**
	 * Execute a command from the command framework. As we have no UI available, we do not pass a control to the command. Hence HandlerUtil.getActive...
	 * commands will very likely fail.
	 *
	 * @param commandId
	 *            full id of the command to execute
	 * @param parameters
	 *            command parameters
	 * @throws ExecutionException
	 * @throws NotDefinedException
	 * @throws NotEnabledException
	 * @throws NotHandledException
	 */
	@WrapToScript
	public void executeCommand(final String commandId, @ScriptParameter(defaultValue = ScriptParameter.NULL) final Map<String, String> parameters)
			throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
		final Map<String, String> commandParameters = (parameters != null) ? parameters : new HashMap<String, String>();
		final ICommandService commandService = (ICommandService) getService(ICommandService.class);
		final IEvaluationService evaluationService = (IEvaluationService) getService(IEvaluationService.class);

		final Command command = commandService.getCommand(commandId);
		command.executeWithChecks(new ExecutionEvent(command, commandParameters, null, evaluationService.getCurrentState()));
	}

	/**
	 * Get a system property value
	 *
	 * @param key
	 *            key to query
	 * @return system property for <i>key</i>
	 */
	@WrapToScript
	public String getSystemProperty(final String key) {
		return System.getProperty(key);
	}

	/**
	 * Run an external process. The process is started in the background and a {@link Future} object is returned. Query the result for finished state, output
	 * and error streams of the executed process.
	 *
	 * @param name
	 *            program to run (with full path if necessary)
	 * @param args
	 *            program arguments
	 * @return {@link Future} object tracking the program
	 */
	@WrapToScript
	public Future runProcess(final String name, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String[] args) {
		final List<String> arguments = new ArrayList<String>();
		arguments.add(name);
		if (args != null) {
			for (final String arg : args)
				arguments.add(arg);
		}

		final ProcessBuilder builder = new ProcessBuilder(arguments);
		try {
			return new Future(builder.start());
		} catch (final IOException e) {
			return new Future(e);
		}
	}

	/**
	 * Read a preferences value. The <i>defaultValue</i> is optional, but contains type information if used. Provide instances of Boolean, Integer, Double,
	 * Float, Long, byte[], or String to get the appropriate return value of same type.
	 *
	 * @param node
	 *            node to read from
	 * @param key
	 *            key name to read from
	 * @param defaultValue
	 *            default value to use, if value is not set
	 * @return
	 */
	@WrapToScript
	public Object readPreferences(final String node, final String key, @ScriptParameter(defaultValue = "") final Object defaultValue) {

		IEclipsePreferences root = InstanceScope.INSTANCE.getNode(node);
		if (root != null) {
			if (defaultValue instanceof Boolean)
				return root.getBoolean(key, (Boolean) defaultValue);

			else if (defaultValue instanceof Integer)
				return root.getInt(key, (Integer) defaultValue);

			else if (defaultValue instanceof Double)
				return root.getDouble(key, (Double) defaultValue);

			else if (defaultValue instanceof Float)
				return root.getFloat(key, (Float) defaultValue);

			else if (defaultValue instanceof Long)
				return root.getLong(key, (Long) defaultValue);

			else if (defaultValue instanceof byte[])
				return root.getByteArray(key, (byte[]) defaultValue);

			else
				return root.get(key, (defaultValue != null) ? defaultValue.toString() : "");
		}

		return null;
	}

	/**
	 * Set a preferences value. Valid types for <i>value</i> are: Boolean, Integer, Double, Float, Long, byte[], and String.
	 *
	 * @param node
	 *            node to write to
	 * @param key
	 *            key to store to
	 * @param value
	 *            value to store
	 */
	@WrapToScript
	public void writePreferences(final String node, final String key, final Object value) {

		IEclipsePreferences root = InstanceScope.INSTANCE.getNode(node);
		if (root != null) {
			if (value instanceof Boolean)
				root.putBoolean(key, (Boolean) value);

			else if (value instanceof Integer)
				root.putInt(key, (Integer) value);

			else if (value instanceof Double)
				root.putDouble(key, (Double) value);

			else if (value instanceof Float)
				root.putFloat(key, (Float) value);

			else if (value instanceof Long)
				root.putLong(key, (Long) value);

			else if (value instanceof byte[])
				root.putByteArray(key, (byte[]) value);

			else
				root.put(key, (value != null) ? value.toString() : "");
		}
	}
}
