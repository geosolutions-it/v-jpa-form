package it.jrc.inject;

import java.util.Map;
import java.util.Set;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

/**
 * The guiced-up version of the Vaadin view
 * 
 * TODO 
 * 
 * @author Will Temperley
 */
public class GuicedViewProvider implements ViewProvider {
    
    public static final String HOME = "Home";

	private Map<String, Provider<View>> adminComponents;
	private Set<String> keys;
	
	@Inject
	public GuicedViewProvider(Map<String, Provider<View>> adminComponents) {
		this.adminComponents = adminComponents;
		this.keys = adminComponents.keySet();
	}

	@Override
	public String getViewName(String viewAndParameters) {
		
		for (String key : keys) {
			if (viewAndParameters.startsWith(key)) {
				return key;
			}
		}
		return HOME;
	}

	@Override
	public View getView(String viewName) {
		
		Provider<View> provider = adminComponents.get(viewName);
		if (provider != null) {
			return provider.get();
		}

		provider = adminComponents.get(HOME);
		return provider.get();
		
	}

}
