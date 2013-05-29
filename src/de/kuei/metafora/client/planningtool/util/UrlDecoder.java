package de.kuei.metafora.client.planningtool.util;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

public class UrlDecoder {

	public static String getParameter(String name){
		String parameter = Window.Location.getParameter(name);
		if(parameter != null){
			parameter = URL.decode(parameter);
		}
		return parameter;		
	}
}
