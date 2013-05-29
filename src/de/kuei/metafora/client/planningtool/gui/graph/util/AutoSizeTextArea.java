package de.kuei.metafora.client.planningtool.gui.graph.util;

import com.google.gwt.user.client.ui.TextArea;

public class AutoSizeTextArea extends TextArea {

	private static int MAXLINELENGTH = 30;
	private static int MINLINELENGTH = 10;
	private static int MAXLINECOUNT = 5;
	private static int MINLINECOUNT = 2;

	public AutoSizeTextArea() {
		super();
		setCharacterWidth(MINLINELENGTH);
		setVisibleLines(MINLINECOUNT);
		getElement().addClassName("cardtextarea");
	}

	private void fitSize() {

		String text = getText();

		int maxlinelenght = 0;

		int linecount = 1;
		int linechars = 0;
		int lastblank = -1;
		boolean breakLine = false;

		text = text.replace((char) 10, '\n');

		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				if (linechars > maxlinelenght)
					maxlinelenght = linechars;
				linechars = 0;
				linecount++;
				lastblank = -1;
			} else {
				if (text.charAt(i) == ' ') {
					if (breakLine) {
						String start = text.substring(0, i);
						String end = text.substring(i + 1, text.length());
						text = start + "\n" + end;
						lastblank = -1;
						linecount++;

						if (linechars > maxlinelenght)
							maxlinelenght = linechars;

						linechars = 0;
					} else {
						lastblank = i;
					}
				}

				linechars++;

				if (linechars >= MAXLINELENGTH) {
					if (lastblank != -1) {
						String start = text.substring(0, lastblank);
						String end = text.substring(lastblank + 1,
								text.length());
						text = start + "\n" + end;
						i = lastblank;
						linecount++;
						if (linechars > maxlinelenght)
							maxlinelenght = linechars;
						linechars = 0;
					} else {
						breakLine = true;
					}
				}
			}
		}

		if (linechars > maxlinelenght)
			maxlinelenght = linechars;

		if (linecount < MINLINECOUNT) {
			linecount = MINLINECOUNT;
		} else if (linecount > MAXLINECOUNT) {
			linecount = MAXLINECOUNT;
		}

		if (maxlinelenght > AutoSizeTextArea.MAXLINELENGTH) {
			maxlinelenght = AutoSizeTextArea.MAXLINELENGTH;
		} else if (maxlinelenght < AutoSizeTextArea.MINLINELENGTH) {
			maxlinelenght = AutoSizeTextArea.MINLINELENGTH;
		}

		setCharacterWidth(maxlinelenght);
		setVisibleLines(linecount);
		super.setText(text);
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		fitSize();
	}
}
