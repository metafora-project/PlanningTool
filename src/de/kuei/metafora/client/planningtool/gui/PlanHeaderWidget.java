package de.kuei.metafora.client.planningtool.gui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

import de.kuei.metafora.client.Languages;

public class PlanHeaderWidget extends LayoutPanel {

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);
	
	private Label plan;
	private Label planName;
	private Label users;
	private Label usersText;

	public PlanHeaderWidget(String name, String user) {
		plan = new Label(language.File()+":");
		users = new Label(language.Members()+":");

		planName = new Label(name);
		planName.setTitle(name);
		usersText = new Label(user);
		users.setTitle(user);

		add(plan);
		add(users);
		add(planName);
		add(usersText);

		setWidgetLeftWidth(plan, 2, Unit.PX, 35, Unit.PX);
		setWidgetTopHeight(plan, 2, Unit.PX, 20, Unit.PX);

		setWidgetLeftRight(planName, 40, Unit.PX, 0, Unit.PX);
		setWidgetTopHeight(planName, 2, Unit.PX, 20, Unit.PX);

		setWidgetLeftWidth(users, 2, Unit.PX, 65, Unit.PX);
		setWidgetTopHeight(users, 25, Unit.PX, 20, Unit.PX);

		setWidgetLeftRight(usersText, 70, Unit.PX, 0, Unit.PX);
		setWidgetTopHeight(usersText, 25, Unit.PX, 20, Unit.PX);
	}

	public void setPlanName(String name) {
		planName.setText(name);
		planName.setTitle(name);

	}

	public void setUserText(String user) {
		usersText.setText(user);
		usersText.setTitle(user);
	}
}
