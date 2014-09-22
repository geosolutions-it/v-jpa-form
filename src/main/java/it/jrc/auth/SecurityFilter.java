package it.jrc.auth;

import java.util.Map;

import org.apache.shiro.config.Ini;
import org.apache.shiro.web.config.WebIniSecurityManagerFactory;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.IniShiroFilter;
import org.apache.shiro.mgt.SecurityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("deprecation")
@Singleton
public class SecurityFilter extends IniShiroFilter {

  static class SecurityManagerFactory extends WebIniSecurityManagerFactory {

    private final WebSecurityManager securityManager;

    public SecurityManagerFactory(WebSecurityManager securityManager) {
      this.securityManager = securityManager;
    }

    public SecurityManagerFactory(WebSecurityManager securityManager, Ini ini) {
      super(ini);
      this.securityManager = securityManager;
    }

    @Override
    protected SecurityManager createDefaultInstance() {
      return securityManager;
    }
  }

  private final Provider<WebSecurityManager> securityManager;

  @Inject
  SecurityFilter(Provider<WebSecurityManager> securityManager) {
    this.securityManager = securityManager;
  }
  

  protected Map<String, ?> applySecurityManager(Ini ini) {
    SecurityManagerFactory factory;
    if (ini == null || ini.isEmpty()) {
      factory = new SecurityManagerFactory(securityManager.get());
    } else {
      factory = new SecurityManagerFactory(securityManager.get(), ini);
    }
    setSecurityManager((WebSecurityManager) factory.getInstance());
    return factory.getBeans();
  }
  
  
}