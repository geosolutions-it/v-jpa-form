package it.jrc.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.expressme.openid.OpenIdManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import freemarker.template.Configuration;

@Singleton
public class AnonymousAuthServlet extends AuthServlet {

    @Inject
    public AnonymousAuthServlet(OpenIdManager manager, 
            @Named("context_path") String contextPath,
            Configuration templateConf, EntityManagerFactory emf) {
        super(manager, contextPath, templateConf, emf);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        /*
         * Either log them in anonymously or if a user change is requested, they go through the login process
         */

        /*
         * Should probably put this in as an option in the huge doGet method
         */

        String action = request.getParameter("action");

        //These are the two possible options, indicating it's not just a change login request
        String openidEndpoint = request.getParameter(OPENID_OP_ENDPOINT);
        String openIdProvider = request.getParameter("op");
        
        if (openidEndpoint != null || openIdProvider != null) {
            super.doGet(request, response);
            return;
        }

        if (action != null && action.equals("change")) {

            super.doGet(request, response);
            return;

        } else {

            SecurityUtils.getSubject().login(new UsernamePasswordToken(Anonymous.USERNAME, ""));

            URI x;
            try {
                x = new URI(request.getRequestURL().toString().replace("login", ""));
                redirectToApp(response, x);
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
