package de.kuei.metafora.client.planningtool.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBoxBase;

import de.kuei.metafora.client.Languages;

public class InputFilter implements KeyPressHandler {

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	private static char[] reservedCharacters = new char[] { '\'', '\"', '<',
			'>', '&' };

	public static String filterString(String text) {
		boolean invalidCharacters = false;

		String original = text;

		for (int i = 0; i < reservedCharacters.length; i++) {
			if (text.contains(reservedCharacters[i] + "")) {
				text = text.replaceAll(reservedCharacters[i] + "", "");
				invalidCharacters = true;
			}
		}

		if (invalidCharacters) {
			String message = language.FilterYouTyped() + original + ". "
					+ language.FilterTheCharacters();
			for (int i = 0; i < reservedCharacters.length; i++) {
				message += reservedCharacters[i] + ", ";
			}
			message = message.substring(0, message.length() - 2);
			message += language.StringFilterAlert() + text;
			Window.alert(message);

		}

		return text;
	}

	private TextBoxBase area;
	private String message;

	public InputFilter(TextBoxBase area) {
		this.area = area;

		message = language.FilterTheCharacters();
		for (int i = 0; i < reservedCharacters.length; i++) {
			message += reservedCharacters[i] + ", ";
		}
		message = message.substring(0, message.length() - 2);
		message += language.TypeFilterAlert();
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		char c = event.getCharCode();
		for (int i = 0; i < reservedCharacters.length; i++) {
			if (reservedCharacters[i] == c) {
				area.cancelKey();
				Window.alert(language.FilterYouTyped() + c + ". " + message);
				break;
			}
		}
	}

}
