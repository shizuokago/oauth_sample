package com.github.shizuokago;

import java.io.IOException;

import javax.servlet.http.*;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

@SuppressWarnings("serial")
public class OAuthRequestServlet extends HttpServlet {


	private static final String REDIRECT_URI = "http://java.kneetenzero.appspot.com/callback";
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	    String state = "abc123xyz456";
	    OAuthClientRequest clientRequest;
		try {  
			String cid = req.getParameter("cid");
			String secret = req.getParameter("csecret");
			req.getSession().setAttribute("cid",cid);
			req.getSession().setAttribute("csecret",secret);
			clientRequest = OAuthClientRequest
			    .authorizationProvider(OAuthProviderType.GOOGLE)
			    .setClientId(cid)
			    .setRedirectURI(REDIRECT_URI)
			    .setResponseType("code")
			    .setState(state)
			    .setScope("https://www.googleapis.com/auth/gmail.readonly")
			    .buildQueryMessage();
		} catch (OAuthSystemException e) {
			throw new RuntimeException(e);
		}

	    String redirectURL = clientRequest.getLocationUri();
	    resp.sendRedirect(redirectURL);
	}
}
