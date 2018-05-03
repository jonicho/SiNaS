package de.sinas.net;

public final class PROTOCOL {
	private PROTOCOL() {}

	public static final String IP = "localhost";
	public static final int PORT = 54699;
	public static final String SPLIT = "\u001F"; // U+001F: Unit separator

	public final class SC {
		private SC() {}

		public static final String ERROR = "err", // Base [split] errorcode
				OK = "ok"; // Base
	}

	public final class CS {
		private CS() {}

		public static final String LOGIN = "lgn"; // Base [split] name
	}

	public final class ERRORCODES {
		private ERRORCODES() {}

		public static final int UNKNOWN_ERROR = -1;
	}

	/**
	 * Builds a massage by connecting all arguments into one String, separated by
	 * SPLIT.<br>
	 * First it calls String.valueOf(x) on each given object.
	 * 
	 * @param msgParts
	 *            The message parts to connect
	 * @return The built String
	 */
	public static String buildMessage(Object... msgParts) {
		String result = String.valueOf(msgParts[0]);
		for (int i = 1; i < msgParts.length; i++) {
			result += SPLIT + String.valueOf(msgParts[i]);
		}
		return result;
	}

	/**
	 * Returns an error message based on the following pattern:</br>
	 * {@code SC.ERROR + SPLIT + errorcode}
	 * 
	 * @param errorcode
	 *            The error code
	 * @return The error message
	 */
	public static String getErrorMessage(int errorcode) {
		return buildMessage(SC.ERROR, String.valueOf(errorcode));
	}
}