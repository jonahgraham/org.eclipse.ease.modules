package org.eclipse.ease.modules.platform;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.Platform;
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
		Map<String, String> commandParameters = (parameters != null) ? parameters : new HashMap<String, String>();
		ICommandService commandService = (ICommandService) getService(ICommandService.class);
		IEvaluationService evaluationService = (IEvaluationService) getService(IEvaluationService.class);

		Command command = commandService.getCommand(commandId);
		command.executeWithChecks(new ExecutionEvent(command, commandParameters, null, evaluationService.getCurrentState()));
	}
}
