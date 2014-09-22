package it.jrc.inject;

import it.jrc.auth.RoleManager;
import it.jrc.form.editor.BaseEditor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;

import org.vaadin.addons.guice.uiscope.UIScoped;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.servlet.SessionScoped;
import com.vaadin.navigator.View;

public abstract class AbstractViewModule extends AbstractModule {

    protected MapBinder<String, View> mapbinder;
    
    private Map<Class<?>, String> classUrlMapping = new HashMap<Class<?>, String>();
    
    
    public static final String HOME = "Home";
    
    /**
     * Provides a mapping between classes and their URLs, which allows lookup
     * via class to URL, useful when finding the URL for a specific entity.
     * 
     * @return
     */
    @Singleton
    @Provides
    Map<Class<?>, String> provideMap() {
        return classUrlMapping;
    }

    /**
     * Provides a mapping between menu groupings and full nav fragments.
     * 
     * @param adminComponents
     * @param roleManager
     * @return
     */
    @SessionScoped
    @Provides
    Multimap<String, String> provideMenuTreeMap(Map<String, Provider<View>> adminComponents, RoleManager roleManager) {
        
    	Set<String> urls = adminComponents.keySet();
    	TreeMultimap<String, String> menuTreeMap = TreeMultimap.create();
    
    	for (String url : urls) {
    	    
    	    if (!url.contains("/")) {
    	        continue;
    	    }
    
    		if (roleManager.canView(url)) {
    
    			String[] headTail = url.split("/");
    			String head = headTail[0];
    			String tail = headTail[1];
    
    			if (head == null || tail == null) {
    				throw new RuntimeException("Invalid url fragment: " + url);
    			}
    			menuTreeMap.put(head, url);
    		}
    	}
    	
    	return menuTreeMap;
    }

    /**
     * Utility method to bind a {@link BaseEditor} subclass and associated
     * {@link Entity}
     * 
     * @param uriFragment
     * @param clazz
     * @param entityClazz
     */
    protected void addBinding(String uriFragment, Class<? extends View> clazz, Class<?> entityClazz) {
        mapbinder.addBinding(uriFragment).to(clazz).in(UIScoped.class);
        classUrlMapping.put(entityClazz, uriFragment);
    }

}