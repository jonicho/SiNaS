package de.sinas.net;

public final class PROTOCOL {
	private PROTOCOL() {}

	public static final String IP = "localhost";
	public static final int PORT = 48333;
	public static final String SPLIT = "\u001F"; // U+001F: Unit separator

	public final class SC {
		private SC() {}

		public static final String ERROR = "err", //{ENRYPTED:MAINAES} Base [split] errorcode
				OK = "ok", //{ENRYPTED:MAINAES} Base
				LOGIN_OK = "lgnok", //{ENRYPTED:MAINAES} Base
				CONVERSATION = "con", //{ENRYPTED:MAINAES} Base [split] conversation name [split] conversation id [split] conversation AES KEY [split] users ...
				SEC_CONNECTION_ACCEPTED = "secacc", //{ENCRYPTED:RSA} Base [split] Main AES Key
				USER = "user", //{ENRYPTED:MAINAES} Base [split] username
				MESSAGES = "msgs", //conversation id[split]{ENCRYPTED:CONVAES} Base [split] conversation id {[split] message id [split] is file [split] timestamp [split] sender [split] content}*
				USER_SEARCH_RESULT = "usersearchres"; //{ENRYPTED:MAINAES} Base [split] query {[split] result}*
	}

	public final class CS {
		private CS() {}

		public static final String LOGIN = "lgn", //{ENRYPTED:MAINAES} Base [split] name [split] PBBKDF2 Hash of the password
				CREATE_SEC_CONNECTION = "crypcon", // Base [split] RSA Public Key
				REGISTER = "reg", //{ENRYPTED:MAINAES} Base [split] name [split] PBBKDF2 Hash of the password
				CREATE_CONVERSATION = "crtcon", //{ENRYPTED:MAINAES} Base [split] name [split] users ...
				GET_CONVERSATIONS = "getcons", //{ENRYPTED:MAINAES} Base
				GET_USER = "getuser", //{ENRYPTED:MAINAES} Base [split] username
				GET_MESSAGES = "getmsgs", //{ENRYPTED:MAINAES} Base [split] conversation id [split] amount
				CONVERSATION_ADD = "conadd", //{ENRYPTED:MAINAES} Base [Split] id [Split] name
				CONVERSATION_REM = "conrem", //{ENRYPTED:MAINAES} Base [Split] id [Split] name
				CONVERSATION_RENAME = "conren", //{ENRYPTED:MAINAES} Base [Split] id [Split] new names
				MESSAGE = "msg", //conversation id[split]{ENCRYPTED:CONVAES} Base [split] conversation id [split] is file [split] content
				USER_SEARCH = "usersearch"; //{ENRYPTED:MAINAES} Base [split] query
	}

	public final class ERRORCODES {
		private ERRORCODES() {}

		public static final int UNKNOWN_ERROR = -1,
				LOGIN_FAILED = 0,
				NOT_LOGGED_IN = 1,
				INVALID_MESSAGE = 2,
				USER_DOES_NOT_EXIST = 3,
				REQUEST_NOT_ALLOWED = 4,
				UNKNOWN_MESSAGE_BASE = 5,
				EMPTY_MESSAGE = 6,
				ALREADY_REGISTERED = 7,
				NOT_SEC_CONNECTED = 8;

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