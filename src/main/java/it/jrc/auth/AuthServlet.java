package it.jrc.auth;

import it.jrc.domain.auth.Nonce;
import it.jrc.domain.auth.OpenIdIdentity;
import it.jrc.domain.auth.Role;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.expressme.openid.Association;
import org.expressme.openid.Authentication;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdException;
import org.expressme.openid.OpenIdManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCAccessTokenResponse;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author will
 * 
 *         Deals with all login requests. Pathways are: 1) show login page 2)
 *         process openid login request (sends redirect to provider) a)
 *         authenticate login with shiro and redirect to application b) if a) is
 *         an unknown account but user has provided openId, create user and tell
 *         them c) if a) has a locked account, throw an error.
 * 
 */
@Singleton
public class AuthServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(AuthServlet.class);

    static final long ONE_HOUR = 3600000L;
    static final long TWO_HOUR = ONE_HOUR * 2L;

    private static final String OPENID_MAC = "openid_mac";
    private static final String OPENID_ALIAS = "openid_alias";
    private static final String OPENID_NONCE = "openid.response_nonce";
    private static final String OPENID_RETURN_TO = "openid.return_to";
    protected static final String OPENID_OP_ENDPOINT = "openid.op_endpoint";
    private static final Object GOOGLE_ENDPOINT = "https://www.google.com/accounts/o8/ud";
    
    private static final String OPENIDCONNECT_TYPE = "openidconnect";
    private static final String ECAS_TYPE = "ecas";

    private final Configuration templateConf;

    private final EntityManager em;

    private final OpenIdManager manager;

    private String contextPath;
    private ClientID clientId;
    private Secret clientSecret;
    private URL tokenUrl;
    private URL profileUrl;
    private String loginPageUrl;

    @Inject
    public AuthServlet(OpenIdManager manager,
            @Named("context_path") String contextPath,
            @Named("openid_clientid") String clientId,
            @Named("openid_clientsecret") String clientSecret,
            @Named("token_url") String tokenUrl,
            @Named("profile_url") String profileUrl,
            @Named("login_page_url") String loginPageUrl,
            Configuration templateConf, EntityManagerFactory emf) {

        this.em = emf.createEntityManager();
        this.manager = manager;

        this.templateConf = templateConf;
        this.contextPath = contextPath;
        this.clientId = new ClientID(clientId);
        this.clientSecret = new Secret(clientSecret);
        this.loginPageUrl = loginPageUrl;
        try {
            this.tokenUrl = new URL(tokenUrl);
            this.profileUrl = new URL(profileUrl);
        } catch (MalformedURLException e) {
            logger.error("Malformed token_url configuration parameter", e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String openIdProvider = request.getParameter("op");
        String openidEndpoint = request.getParameter(OPENID_OP_ENDPOINT);
        String action = request.getParameter("action");
        
        // type of auth to use, currently we support OpenID-Connect (for Google)
        // and OAuth 2.0 (for others)
        String type = request.getParameter("type");
        // parameters used by OpenID-Connect (state, code)
        String state = request.getParameter("state");
        String code = request.getParameter("code");

        String loginUrl = loginPageUrl;
        if(loginUrl == null || loginUrl.isEmpty()) {
            loginUrl = request.getParameter("servlet_url");
        }
        
        if (loginUrl == null) {
            loginUrl = request.getRequestURL().toString();
        }
        
        URI loginUri = this.getLoginURI(loginUrl);

        /*
         * Get the current user
         */
        Subject currentUser = SecurityUtils.getSubject();

        if (action != null && action.equals("logout")) {
            currentUser.logout();
            request.getSession().removeAttribute("ecasPrincipal");
            showLoginPage(
                    request,
                    response,
                    "You have been logged out. Please note you're still logged in with your OpenID provider.");
            return;
        }

        if (action != null && action.equals("change")) {
            currentUser.logout();
            showLoginPage(request, response, "HI");
            return;
        }

        /*
         * The user may be authenticated already.
         */
        if (currentUser.isAuthenticated()) {
            redirectToApp(response, loginUri);
            return;
        }

        /*
         * Catch redirect from provider
         */
        if (openidEndpoint != null) {
            // OAuth
            verifyOpenIdReply(request, response, currentUser, openidEndpoint);
        } else if (code != null && state != null) {
            try {
                verifyOpenIdConnectReply(request, response, currentUser, state, code, loginUri);
            } catch (OpenIdException e) {
                showLoginError(request, response, e, "Could not authorize the user: ");
                return;
            }
        } else if (openIdProvider != null) {

            String lookup = null;

            lookup = request.getParameter("url");

            try {
                doOpenIdRequest(request, response, lookup, loginUri, type);
            } catch (OpenIdException e) {

                showLoginError(request, response, e, "Could not resolve this OpenID: ");
                return;
            }

        } else {
            showLoginPage(request, response, null);
        }
    }

    private void showLoginError(HttpServletRequest request, HttpServletResponse response,
            OpenIdException e, String message) throws IOException {
        StringBuilder msg = new StringBuilder();
        msg.append(message);
        msg.append("Cause: ");
        msg.append(e.getCause().getMessage());
        msg.append("\n");
        msg.append("Host = " + request.getServerName());
        msg.append("\n");
        msg.append("Port = " + request.getServerPort());
        msg.append("\n");
   
        for (StackTraceElement st : e.getCause().getStackTrace()) {
            msg.append(st.toString());
            msg.append("\n");
        }
        showLoginPage(request, response, msg.toString());
    }

    private void verifyOpenIdConnectReply(HttpServletRequest request, HttpServletResponse response,
            Subject currentUser, String state, String code, URI loginUri) {
        if (request.getSession().getAttribute("state") == null || !state.equals(request.getSession().getAttribute("state"))) {
              response.setStatus(401);
        } else {
            try {
                TokenRequest tokenRequest = new TokenRequest(
                        tokenUrl,
                        new ClientSecretPost(clientId, clientSecret),
                        new AuthorizationCodeGrant(new AuthorizationCode(code),loginUri.toURL())
                );
                HTTPResponse resp = tokenRequest.toHTTPRequest().send();
                OIDCAccessTokenResponse tokenResponse = OIDCAccessTokenResponse.parse(resp);
                ReadOnlyJWTClaimsSet claims = tokenResponse.getIDToken().getJWTClaimsSet();
                
                Authentication authentication = new Authentication();
                authentication.setEmail(claims.getClaim("email").toString());
                authentication.setIdentity(authentication.getEmail());
                authenticateFromIdentity(request, response, currentUser, loginUri, authentication, authentication.getEmail(), (BearerAccessToken)tokenResponse.getAccessToken());
            } catch (SerializeException e) {
                throw new OpenIdException("Cannot contact token service", e);
            } catch (IOException e) {
                throw new OpenIdException("Error sending token request", e);
            } catch (com.nimbusds.oauth2.sdk.ParseException e) {
                throw new OpenIdException("Error parsing token response", e);
            } catch (ParseException e) {
                throw new OpenIdException("Error parsing token claims", e);
            }
        }
    }

    private URI getLoginURI(String loginUrl) {
        try {
            URI uri = new URI(loginUrl);
            return uri;
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    protected void redirectToApp(HttpServletResponse response, URI loginUri)
            throws IOException {

        // StringBuilder sb = new StringBuilder();
        // sb.append(loginUri.getAuthority());
        // sb.append(loginUri.get)

        String url = getAppUrl(loginUri);
        // sb.append("#");
        // sb.append(fragment);
        response.sendRedirect(url);
        return;
    }
    
    protected void redirectToEcas(HttpServletResponse response, URI loginUri)
            throws IOException {

        // StringBuilder sb = new StringBuilder();
        // sb.append(loginUri.getAuthority());
        // sb.append(loginUri.get)

        String url = getEcasUrl(loginUri);
        // sb.append("#");
        // sb.append(fragment);
        response.sendRedirect(url);
        return;
    }

    /**
     * Gets the realm (e.g. *.jrc.it) with wildcard subdomain
     * 
     * @param url
     * @return
     */
    private String getRealm(URI uri) {

        return uri.getAuthority().replace("www", "*");

    }

    private String getLoginUriAsStringFromURLWhichDoesSomeCleaningOnlyReally(
            URI uri) {

        try {
            URI loginURI = new URI("http://" + uri.getAuthority()
                    + uri.getPath());
            return loginURI.toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;

    }


    // FIXME login hardcode, replacing login with nothing
    private String getAppUrl(URI loginUri) {

        String fragment = loginUri.getFragment();
        URI loginURI;
        try {
            loginURI = new URI("http://" + loginUri.getAuthority()
                    + loginUri.getPath().replace("login", ""));
            return loginURI.toString();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fragment;
    }
    
    // FIXME login hardcode, replacing login with nothing
    private String getEcasUrl(URI loginUri) {

        String fragment = loginUri.getFragment();
        URI loginURI;
        try {
            loginURI = new URI("http://" + loginUri.getAuthority()
                    + loginUri.getPath().replace("login", "ecas/"));
            return loginURI.toString();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fragment;
    }

    /**
     * Makes an openid request
     * 
     * @param request
     * @param response
     * @param lookup
     * @param loginUri
     * @throws IOException
     */
    private void doOpenIdRequest(HttpServletRequest request,
            HttpServletResponse response, String lookup, URI loginUri, String type)
            throws IOException {
    	if(type.equalsIgnoreCase(ECAS_TYPE)) {
    		redirectToEcas(response, loginUri);
    	} else if(type.equalsIgnoreCase(OPENIDCONNECT_TYPE)) {
            doOpenIdConnectRequest(request, response, lookup, loginUri);
        } else {
            doOAuthRequest(request, response, lookup, loginUri);
        }
    }

    private void doOAuthRequest(HttpServletRequest request, HttpServletResponse response,
            String lookup, URI loginUri) throws IOException {
        Endpoint endpoint = manager.lookupEndpoint(lookup);
        Association association = manager.lookupAssociation(endpoint);
        request.getSession().setAttribute(OPENID_MAC,
                association.getRawMacKey());
        request.getSession().setAttribute(OPENID_ALIAS, endpoint.getAlias());
   
        // seems pointless but cleans the URL
        manager.setReturnTo(getLoginUriAsStringFromURLWhichDoesSomeCleaningOnlyReally(loginUri));
   
        String url = manager.getAuthenticationUrl(endpoint, association);
        response.sendRedirect(url);
    }

    private void doOpenIdConnectRequest(HttpServletRequest request, HttpServletResponse response, String lookup, URI loginUri)
            throws MalformedURLException, IOException {
        // generate an anti-forging state, that will be verified
        // on server authorization request 
        State state = new State();
        request.getSession().setAttribute("state", state.getValue());
        ResponseType responseType = new ResponseType();
        responseType.add(ResponseType.Value.CODE);
        
        // redirect to OpenId-Connect authorization service
        String finalLoginUri = getLoginUriAsStringFromURLWhichDoesSomeCleaningOnlyReally(loginUri);
        AuthorizationRequest req = new AuthorizationRequest(
                new URL(lookup),
                responseType,
                clientId,
                new URL(finalLoginUri),
                Scope.parse("openid email profile"),
                state
                );
        
        try {
            
            String location = req.toHTTPRequest().getURL().toExternalForm()+"?"+req.toHTTPRequest().getQuery();
            response.sendRedirect(location);
        } catch (SerializeException e) {
            throw new OpenIdException("Cannot redirect to OpenID Connect service.");
        }
    }

    /**
     * check response_nonce to prevent replay-attack:
     * 
     * @param nonce
     */
    void checkNonce(String nonce) {
        if (nonce == null || nonce.length() < 20) {
            throw new OpenIdException("Verify failed.");
        }

        long nonceTime = getNonceTime(nonce);
        long diff = System.currentTimeMillis() - nonceTime;

        if (diff < 0) {
            diff = (-diff);
        }

        if (diff > ONE_HOUR) {
            throw new OpenIdException(
                    "Nonce is too old, possible replay attack.");
        }

        /*
         * Check the nonce doesn't exist, to ensure a replay attack isn't
         * happening.
         */
        if (nonceExists(nonce)) {
            throw new OpenIdException(
                    "Possible replay attack in progress, aborting.");
        }
        storeNonce(nonce, nonceTime + TWO_HOUR);
    }

    /**
     * Determines if a nonce already exists.
     * 
     * @param nonce
     * @return
     */
    private boolean nonceExists(String nonce) {
        Nonce obj = em.find(Nonce.class, nonce);
        if (obj != null) {
            return true;
        }
        return false;
    }

    /**
     * Store a nonce and also clear any unexpired ones.
     * 
     * @param nonce
     * @param expires
     */
    private void storeNonce(String nonce, long expires) {
        Date nonceExpiry = new Date(expires);
        em.persist(new Nonce(nonce, nonceExpiry));

        executeNativeUpdate("DELETE from auth.nonce where expires <= now()");
    }

    private void executeNativeUpdate(String sql) {
        em.getTransaction().begin();
        Query q = em.createNativeQuery(sql);
        q.executeUpdate();
        em.getTransaction().commit();
    }

    /**
     * Retrieves the time of a nonce
     * 
     * @param nonce
     * @return
     */
    private long getNonceTime(String nonce) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(
                    nonce.substring(0, 19) + "+0000").getTime();
        } catch (ParseException e) {
            throw new OpenIdException("Bad nonce time.");
        }
    }

    /**
     * Creates a new role that cannot login. Sends a notification email to the
     * app administrator.
     * 
     * UPDATE: looks for a user with the identity.
     * 
     * @param returnTo
     * 
     */
    private void createUser(Authentication authentication, boolean canLogin,
            URI returnTo, BearerAccessToken token) {

        Role role = new Role();
        // role.setIdentity(authentication.getIdentity());
        role.setCanLogin(canLogin);
        role.setIsSuperUser(false);

        role.setEmail(authentication.getEmail());
        if(token != null) {
            // get profile info for user
            UserInfoRequest userRequest = new UserInfoRequest(profileUrl, token);
            try {
                HTTPResponse resp = userRequest.toHTTPRequest().send();
                UserInfoResponse userInfo = UserInfoResponse.parse(resp);
                if(userInfo instanceof UserInfoErrorResponse) {
                    throw new OpenIdException("Error in retrieving user profile for " + authentication.getEmail());
                }
                UserInfoSuccessResponse profile = (UserInfoSuccessResponse)userInfo;
                role.setFirstName(profile.getUserInfo().getName());
                role.setLastName(profile.getUserInfo().getFamilyName());
            } catch (SerializeException e) {
                throw new OpenIdException("Cannot contact profile service", e);
            } catch (IOException e) {
                throw new OpenIdException("Cannot get answer from profile service", e);
            } catch (com.nimbusds.oauth2.sdk.ParseException e) {
                throw new OpenIdException("Cannot read answer from profile service", e);
            }
        } else {
            role.setFirstName(authentication.getFirstname());
            role.setLastName(authentication.getLastname());
        }
        em.getTransaction().begin();
        em.persist(role);

        OpenIdIdentity id = new OpenIdIdentity();
        id.setIdentity(authentication.getIdentity());
        if (returnTo == null) {
            logger.error("Return to was null");
        } else {
            id.setRealm(getRealm(returnTo));
        }
        id.setRole(role);
        em.persist(id);
        em.getTransaction().commit();

    }

    /**
     * Shows the user the login page and a particular message.
     * 
     * @param req
     * @param resp
     * @param authenticationMessage
     * @throws IOException
     */
    private void showLoginPage(HttpServletRequest req,
            HttpServletResponse resp, String authenticationMessage)
            throws IOException {

        resp.setContentType("text/html");

        Map<String, String> root = new HashMap<String, String>();
        String returnTo = req.getParameter(OPENID_RETURN_TO);
        if (returnTo == null) {
            returnTo = "";
        }
        root.put("loginReturnTo", returnTo);
        root.put("contextPath", contextPath);
        root.put("authMessage", authenticationMessage);
        root.put("originalHost", req.getHeader("host"));

        Template ftl = templateConf.getTemplate("Login.ftl");

        PrintWriter out = resp.getWriter();

        try {
            ftl.process(root, out);
            out.flush();
        } catch (TemplateException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * The response from the OpenID provider must be verified to ensure it is
     * properly signed and not a replay attack.
     * 
     * @param request
     * @param response
     * @param currentUser
     * @param openidEndpoint
     * @throws IOException
     */
    private void verifyOpenIdReply(HttpServletRequest request,
            HttpServletResponse response, Subject currentUser,
            String openidEndpoint) throws IOException {

        URI returnTo;
        try {
            returnTo = new URI(request.getParameter(OPENID_RETURN_TO));
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        // check nonce:
        checkNonce(request.getParameter(OPENID_NONCE));

        // get authentication:
        byte[] mac_key = (byte[]) request.getSession().getAttribute(OPENID_MAC);
        String alias = (String) request.getSession().getAttribute(OPENID_ALIAS);
        Authentication authentication = manager.getAuthentication(request,
                mac_key, alias);

        String identity = authentication.getIdentity();

        /*
         * With google ONLY, the email is used instead of the identity, as the
         * identity changes according to the server host name
         */
        if (openidEndpoint.equals(GOOGLE_ENDPOINT)) {
            identity = authentication.getEmail();
        }
        authenticateFromIdentity(request, response, currentUser, returnTo, authentication, identity, null);
    }

    private void authenticateFromIdentity(HttpServletRequest request, HttpServletResponse response,
            Subject currentUser, URI returnTo, Authentication authentication, String identity, BearerAccessToken accessToken)
            throws IOException {
        UsernamePasswordToken token = new UsernamePasswordToken(identity, "",
                "");

        String authFailureMessage = "Login failed.";
        try {
            currentUser.login(token);
            if (currentUser.isAuthenticated()) {
                redirectToApp(response, returnTo);
                return;
            }
        } catch (UnknownAccountException uae) {
            logger.debug("Unknown account: " + token.getUsername());
            createUser(authentication, true, returnTo, accessToken);
            redirectToApp(response, returnTo);
            // authFailureMessage = String
            // .format("%s, your account has been created but requires unlocking by an administrator.",
            // authentication.getFirstname());
        } catch (IncorrectCredentialsException ice) {
            logger.info("Incorrect credentials supplied: "
                    + token.getUsername());
        } catch (LockedAccountException lae) {
            logger.info("Locked account: " + token.getUsername());
            authFailureMessage = String
                    .format("%s, your account exists but currently locked. Contact your administrator to unlock it.",
                            token.getUsername());
        } catch (ExcessiveAttemptsException eae) {
            logger.info("An excessive number of login attempts have been made: "
                    + token.getUsername());
        } catch (AuthenticationException ae) {
            logger.error("Unknown authentication exception: " + ae.getMessage());
        }

        showLoginPage(request, response, authFailureMessage);
    }

}