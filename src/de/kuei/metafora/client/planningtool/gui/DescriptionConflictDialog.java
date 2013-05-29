package de.kuei.metafora.client.planningtool.gui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.planningtool.gui.graph.DnDNode;

public class DescriptionConflictDialog extends DialogBox implements ClickHandler{
	
	// i18n
	final static Languages language = GWT.create(Languages.class);
	private DnDNode node;
	private HTML ownText;
	private HTML otherText;
	private TextArea newText;
	
	
	/**
	 * This dialog is shown if a node description was changed locally and remotely
	 * 
	 * @param node
	 * 		local node reference
	 * @param text
	 * 		remote changed description
	 */
	public DescriptionConflictDialog(DnDNode node, String text){
		
		
		this.node = node;
		
		VerticalPanel dialogPanel = new VerticalPanel();
		
		//TODO: language
		HTML title = new HTML("<p><b>"+language.DescriptionAlsoChangesSomewhereElse()+"</b></p>");
		dialogPanel.add(title);
		
		HTML your = new HTML("<b>"+language.YourText()+"</b>");
		dialogPanel.add(your);
		
		ownText = new HTML("<p>"+node.getDescriptionFieldContent()+"</p>");
		dialogPanel.add(ownText);
		
		HTML other = new HTML("<b>"+language.OtherText()+"</b>");
		dialogPanel.add(other);
		
		otherText = new HTML("<p>"+text+"</p>");
		dialogPanel.add(otherText);

		
		dialogPanel.add(new HTML("<b>"+language.DecideOnActualText()+"</b>"));
		
		newText = new TextArea();
		newText.setSize("30em", "10ex");
		newText.setText(text);
		dialogPanel.add(newText);
		
		Button submit = new Button(language.Commit());
		submit.addClickHandler(this);
		dialogPanel.add(submit);
		
		add(dialogPanel);
		
	}
	
	public void setOwnText(String text){
		ownText.setHTML("<p>"+text+"</p>");
	}
	
	public void setOtherText(String text){
		if(this.isShowing()){
			otherText.setHTML("<p><b>"+language.ChangedAgain()+"</b><br /><span style='border-width:1px; border-style:solid; border-color:red;'>"+text+"</span></p>");
		}else{
			otherText.setHTML("<p>"+text+"</p>");
			newText.setText(text);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		node.sendNewDescription(newText.getText());
		this.hide();
	}

}
