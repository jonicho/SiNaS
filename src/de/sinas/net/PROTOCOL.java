package de.sinas.net;

public final class PROTOCOL {
	private PROTOCOL() {}

	public static final String IP = "localhost";
	public static final int PORT = 48333;
	public static final String SPLIT = "\u001F"; // U+001F: Unit separator

	public final class SC {
		private SC() {}

		public static final String ERROR = "err", // Base [split] errorcode
				OK = "ok", // Base
				LOGIN_OK = "lgnok", // Base [split] username [split] nickname
				CONVERSATION = "con", // Base [split] conversation id [split] is group conversation [split] users ...
				USER = "user", // Base [split] username [split] nickname
				MSG = "msg"; // Base [split] conversation id [split] message id [split] is file [split] timestamp [split] content
	}

	public final class CS {
		private CS() {}

		public static final String LOGIN = "lgn", // Base [split] name
				GET_CONVERSATIONS = "getconlist", // Base
				GET_USER = "getuser", // Base [split] username
				GET_MESSAGES = "getmsgs", // Base [split] conversation id [split] amount
				MSG = "msg"; // Base [split] conversation id [split] is file [split] content
	}

	public final class ERRORCODES {
		private ERRORCODES() {}

		public static final int UNKNOWN_ERROR = -1,
				LOGIN_FAILED = 0,
				NOT_LOGGED_IN = 1,
				INVALID_MESSAGE = 2,
				USER_DOES_NOT_EXIST = 3;
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