package it.jrc.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Denies access to all resources unless user is authenticated or it is the login page.
 * Must be after {@link SecurityFilter} in the chain.
 * 
 * Every servlet (or filter) is required to be a @Singleton
 * 
 * @author will
 *
 */
@Singleton
public class AuthFilter implements Filter {
  
  private final String loginServletPath;
  private final String contextPath;

  @Inject
  public AuthFilter(@Named("login_servlet_path") String loginServletPath, @Named("context_path") String contextPath) {
    this.loginServletPath = loginServletPath;
    this.contextPath = contextPath;
  }

  public void destroy() {
  }

  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    
    String servletPath = ((HttpServletRequest) req).getServletPath();
    
    /*
     * Allow unauthenticated requests to Vaadin themes and login servlets.
     */
    if (servletPath.startsWith(loginServletPath) || servletPath.startsWith("/VAADIN/themes")) {
      chain.doFilter(req, resp);
      return;
    }
    
    Subject subject = SecurityUtils.getSubject();
    
    if (!subject.isAuthenticated()) {
        
        
      //TODO: 
      //Causes the horrible exception message when session expires and VAADIN makes an ajax request.
      //Would be good to determine if this is an ajax request or not and send an appropriate response if so.
        
      ((HttpServletResponse) resp).sendRedirect(contextPath + loginServletPath);
      return;
    }
    
    chain.doFilter(req, resp);
    
  }

  public void init(FilterConfig arg0) throws ServletException {
      
  }


}
