package it.jrc.auth;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.google.inject.Singleton;

import eu.cec.digit.ecas.client.constants.AssuranceLevel;
import eu.cec.digit.ecas.client.constants.ProxyGrantingProtocol;
import eu.cec.digit.ecas.client.constants.TicketType;
import eu.cec.digit.ecas.client.jaas.DetailedUser;
import eu.cec.digit.ecas.client.jaas.ExtendedUserDetails;

@Singleton
public class FakeEcasFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) req) {

			@Override
			public Principal getUserPrincipal() {
				return new DetailedUser() {

					@Override
					public String getName() {
						return "Fake";
					}

					@Override
					public AssuranceLevel getAssuranceLevel() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getDepartmentNumber() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getDeviceName() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getDomain() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getDomainUsername() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getEmail() {
						return "ecas.fake@dummy.eu";
					}

					@Override
					public String getEmployeeNumber() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getEmployeeType() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Map getExtendedAttributes() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getFirstName() {
						return "Fake";
					}

					@Override
					public String getLastName() {
						return "Ecas";
					}

					@Override
					public String getLocale() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getMobilePhoneNumber() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getOrgId() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Map getRegistrationLevelVersions() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getStorkId() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getTelephoneNumber() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getTimeZone() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getTokenCramId() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getTokenId() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getUid() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getUnversionedUid() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getUserManager() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getPgtId() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getPgtIou() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public List getProxies() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public ProxyGrantingProtocol getProxyGrantingProtocol() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public TicketType getTicketType() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public ExtendedUserDetails getExtendedUserDetails() {
						// TODO Auto-generated method stub
						return null;
					}
					
				};
			}
			
		}, resp);
		
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
