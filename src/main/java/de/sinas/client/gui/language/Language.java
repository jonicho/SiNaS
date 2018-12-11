package de.sinas.client.gui.language;

import java.util.Locale;
import java.util.Map;

public class Language {
	public static Language
			GERMAN = new Language("de"),
			ENGLISH = new Language("en");

	/**
	 * Returns the language for the given locale.<br/>
	 * Defaults to english if the given locale is not supported.
	 *
	 * @param locale
	 * @return
	 */
	public static Language getLanguage(Locale locale) {
		switch (locale.getLanguage()) {
			case "en":
				return ENGLISH;
			case "de":
				return GERMAN;
			default:
				return ENGLISH;
		}
	}

	private Map<String, String> langMap;

	private Language(String langId) {
		loadStrings(langId);
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
	 * Loads the Strings for the given language id
	 *
	 * @param langId
	 * @throws IllegalArgumentException if the given language id is not supported
	 */
	private void loadStrings(String langId) throws IllegalArgumentException {
		// TODO: implement
	}
}
