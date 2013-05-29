package de.kuei.metafora.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Interface for i18n (internationalization)
 * 
 * @author Kerstin Pfahler
 * @methods returns a string, whose value depends on the requested language
 * 
 *          add "&locale=en" to the end of the URL -> words appear in English
 *          add "&locale=hbr" to URL -> words appear in Hebrew add "&locale=gr"
 *          to URL -> words appear in Greek add "&locale=de" to URL -> words
 *          appear in German i18n default value = English
 */
public interface Languages extends Messages {
	String MenuItemDelete();

	String MenuItemOpen();

	String MenuItemNewMap();

	String MenuItemSaveAs();

	String MenuItemSaveVersion();

	String DiscussThisRightClickCardMenu();

	String DeleteRightClickCardMenu();

	String DoneRightClickCardMenu();

	String OpenRightClickCardMenu();

	String ShareModelRightClickCardMenu();

	String File();

	String IsNeededFor();

	String IsNext();

	String IsLinkedTo();

	String Connectors();

	String PlanningTool();

	String PostToChat();

	String Start();

	String MapOwners();

	String SharedWithGroups();

	String ShareWithGroup();

	String Members();
	
	String TypeFilterAlert();
	String FilterYouTyped();
	String FilterTheCharacters();
	String StringFilterAlert();
	String InvalidNodeReceived();
	String InvalidEdgeReceived();
	String TheNode();
	String WasDeletedAndNoLongerAvailable();
	String MapNotSharedAnyLongerWithGroup();
	String UnshareWithGroup();
	String OK();
	String ErrorCallingServer();
	String CausedBy();
	String ErrorLocatedAtFile();
	String InClass();
	String AtLine();
	String SendMessageToKUEI();
	String TryRightClickAndReloadFrame();
	String Time();
	String Server();
	String Group();
	String Challenge();
	String Plan();
	String DescriptionAlsoChangesSomewhereElse();
	String YourText();
	String OtherText();
	String DecideOnActualText();
	String Commit();
	String ChangedAgain();
	String BrowserDoesNotSupportHTML5Canvas();
	String DoYouReallyWantToDeleteThisPlan();
	String EnterYourAnswer();
	String DeletingPlanFailed();
	String DeletingPlanCanceled();
	String EnterNewPlanName();
	String CreationFailed();
	String PlanAlreadyExists();
	String SaveFailed();
	String SavingPlanWithName();
	String FailedThereIsAlreadyAPlan();
	String DocumentSavedWithId();
	String SavingVersionFailed();
	String InvalidNewAreaSize();
	String HereIsMyModel();
	String ReflectionTool();
	String SelectConnectorType();
	String Select();
	String Choose();
	String Share();
}