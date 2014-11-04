package org.eclipse.ease.modules.platform;

import java.util.Scanner;

/**
 * Future object tracking an asynchronous execution result.
 */
public class Future {

	private final Process fProcess;
	private final Exception fException;

	/**
	 * Constructor for a process.
	 * 
	 * @param process
	 *            running process
	 */
	public Future(Process process) {
		fProcess = process;
		fException = null;
	}

	/**
	 * Constructor for exceptions.
	 * 
	 * @param exception
	 *            exception to provide for user
	 */
	public Future(Exception exception) {
		fException = exception;
		fProcess = null;
	}

	/**
	 * Query external process for finished state.
	 * 
	 * @return <code>true</code> when finished
	 */
	public boolean isFinished() {
		if (fException == null) {

			try {
				fProcess.exitValue();
				return true;
			} catch (final IllegalThreadStateException e) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Wait for external process to finish
	 * 
	 * @return <code>true</code> when finished
	 */
	public boolean join() {
		if (!isFinished()) {
			try {
				fProcess.waitFor();
			} catch (final InterruptedException e) {
			}
		}

		return isFinished();
	}

	/**
	 * Get exit code of external process. In case of an exception -1 is returned.
	 * 
	 * @return exit code
	 */
	public int getExitCode() {
		if (fProcess != null)
			return fProcess.exitValue();

		return -1;
	}

	/**
	 * Get the output of the process as string. This method works only once as it consumes a stream.
	 * 
	 * @return process output
	 */
	public String getOutput() {
		if (fProcess != null) {
			final Scanner scanner = new Scanner(fProcess.getInputStream()).useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}

		return "";
	}

	/**
	 * Get the error text of the process as string. This method works only once as it consumes a stream.
	 * 
	 * @return process error text (or exception message)
	 */
	public String getError() {
		if (fProcess != null) {
			final Scanner scanner = new Scanner(fProcess.getErrorStream()).useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		} else if (fException != null) {
			return fException.toString();
		}

		return "";
	}
}
