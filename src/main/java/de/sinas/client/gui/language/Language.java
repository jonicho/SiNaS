package de.sinas.client.gui.language;

import java.util.Locale;
import java.util.Map;

public class Language {
	public static Language
			ENGLISH = new Language(Locale.ENGLISH);

	private Map<String, String> langMap;

	private Language(Locale locale) {
		loadString(locale);
	}

	/**
	 * Returns the internationalized String with the given identifier
	 *
	 * @param identifier
	 * @return
	 */
	public String getString(String identifier) {
		String string = langMap.get(identifier);
		return string == null ? "" : string;
	}

	/**
	 * Loads the Strings for the given locale
	 *
	 * @param locale
	 * @throws IllegalArgumentException if the given locale is not supported
	 */
	private void loadString(Locale locale) throws IllegalArgumentException {
		// TODO: implement
	}
}
