package de.kuei.metafora.client.planningtool.gui;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.PlanningTool;
import de.kuei.metafora.client.planningtool.PlanningToolWidget;

public class ClientErrorHandler {
	// i18n
	final static Languages language = GWT.create(Languages.class);

	public static void showErrorMessage(Throwable caught) {
		String out = language.ErrorCallingServer() + " \n";

		String message = caught.getMessage();
		out += message + " \n";

		Throwable cause = caught.getCause();
		if (cause != null) {
			out += language.CausedBy() + " " + cause.getMessage() + " \n";
		}

		StackTraceElement[] stacktrace = caught.getStackTrace();
		if (stacktrace != null && stacktrace.length > 0) {
			out += language.ErrorLocatedAtFile() + " "
					+ stacktrace[0].getFileName() + " ";
			out += language.InClass() + " " + stacktrace[0].getClassName()
					+ " ";
			out += language.AtLine() + " " + stacktrace[0].getLineNumber()
					+ ". \n";

		}

		out += language.SendMessageToKUEI() + " \n";
		out += language.TryRightClickAndReloadFrame() + " \n";

		Date date = new Date();
		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy/MM/dd HH:mm:ss");
		out += language.Time() + " " + dtf.format(date).toString() + ", ";
		out += language.Server() + " " + PlanningTool.getTomcatServer() + ", ";
		out += language.Group() + " " + PlanningTool.getGroup() + ", ";
		out += language.Challenge() + " " + PlanningTool.getChallengeName()
				+ " (";
		out += PlanningTool.getChallengeId() + ") ";
		out += language.Plan() + " " + PlanningToolWidget.map;

		Window.alert(out);
	}

}
