package it.jrc.form;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class UriTest {
    
    @Test
    public void testUri() {
        
        String loginUrl = "http://www.localhost:8080/esp-client/login#!Home";
        try {
            URI uri = new URI(loginUrl);
            
//            System.out.println(uri.getHost());
            System.out.println(uri.getAuthority());
            System.out.println(uri.getPath());
            System.out.println(uri.getFragment());
            
            URI loginURI = new URI("http//" + uri.getAuthority() + uri.getPath());
            System.out.println(loginURI);
            
            
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
