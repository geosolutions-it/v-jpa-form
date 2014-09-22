package it.jrc.inject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

public abstract class AbstractGuiceServletModule extends ServletModule {

    private Properties props;

    /**
     * The most basic ini possible, just to keep Shiro happy. Question is, is
     * Shiro really necessary? It does cause a lot of cruft.
     * 
     * @return
     */
    protected Map<String, String> getIni() {
        Map<String, String> config = new HashMap<String, String>();
        config.put("config", "[roles]\nadmin = *\n");
        return config;
    }

    /**
     * Gets a map of servlet parameters
     * 
     * @return
     */
    protected Map<String, String> getServletParams() {
        Properties properties = getRuntimeProperties();

        Map<String, String> params = new HashMap<String, String>();

        params.put("widgetset", properties.getProperty("widgetset"));
        params.put("productionMode", properties.getProperty("productionMode"));
        return params;
    }
    
    protected boolean isInProductionMode() {
        String productionMode = getRuntimeProperties().getProperty("productionMode");
        return Boolean.valueOf(productionMode);
    }

    /**
     * Returns the properties from runtime.properties, loading them from file if
     * this hasn't already been done.
     * 
     * @return the runtime {@link Properties} 
     *  
     */
    protected Properties getRuntimeProperties() {

        if (props == null) {

            props = new Properties();

            InputStream stream = this.getClass().getClassLoader()
                    .getResourceAsStream("runtime.properties");
            try {
                props.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return props;
    }

    @Provides
    @Named("context_path")
    String contextPath(ServletContext context) {
        return context.getContextPath();
    }

    @Provides
    @Singleton
    WebSecurityManager securityManager(Realm realm) {
        DefaultWebSecurityManager sm = new DefaultWebSecurityManager();
        sm.setRealm(realm);
        // sm.setCacheManager(new EhCacheManager());
        return sm;
    }

    @Provides
    @Singleton
    Configuration templateConfiguration(ServletContext context) {
        Configuration configInstance = new Configuration();
        configInstance.setServletContextForTemplateLoading(context,
                "WEB-INF/templates");
        configInstance.setObjectWrapper(new DefaultObjectWrapper());
        return configInstance;
    }

    /*
     * FIXME: this isn't portable - too much application knowledge in this
     * method
     * 
     * perhaps auth servlet should advertise what to do
     */
    @Provides
    @Named("logout_path")
    String logoutPath(@Named("login_servlet_path") String loginServletPath,
            @Named("context_path") String contextPath) {
        return contextPath + loginServletPath + "?action=logout";
    }
}