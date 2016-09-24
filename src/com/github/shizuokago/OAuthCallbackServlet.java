package com.github.shizuokago;

import java.io.IOException;
import javax.servlet.http.*;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

@SuppressWarnings("serial")
public class OAuthCallbackServlet extends HttpServlet {

	private static final String REDIRECT_URI = "http://java.kneetenzero.appspot.com/callback";
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
	    String code;
	    try {
	      OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(req);
	      code = oar.getCode();
	    } catch (OAuthProblemException e) {
	    	throw new RuntimeException(e);
	    }

		String cid = (String)req.getSession().getAttribute("cid");
		String csecret = (String)req.getSession().getAttribute("csecret");

	    OAuthClientRequest oauthRequest;
		try {
			oauthRequest = OAuthClientRequest
			    .tokenProvider(OAuthProviderType.GOOGLE)
			    .setClientId(cid)
			    .setClientSecret(csecret)
			    .setRedirectURI(REDIRECT_URI)
			    .setGrantType(GrantType.AUTHORIZATION_CODE)
			    .setCode(code)
			    .buildBodyMessage();
		} catch (OAuthSystemException e) {
	    	throw new RuntimeException(e);
		}

	    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient()); 
	    OAuthAccessTokenResponse oAuthResponse;
		try {
			oAuthResponse = oAuthClient.accessToken(oauthRequest, "POST");
		} catch (OAuthSystemException e) {
			throw new RuntimeException(e);
		} catch (OAuthProblemException e) {
			throw new RuntimeException(e);
		}

	    String accessToken = oAuthResponse.getAccessToken();
	    
	    OAuthClientRequest bearerClientRequest;
		try {
			bearerClientRequest = new OAuthBearerClientRequest("https://www.googleapis.com/gmail/v1/users/me/labels").setAccessToken(accessToken).buildHeaderMessage();
		} catch (OAuthSystemException e) {
			throw new RuntimeException(e);
		}
        oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthResourceResponse resourceResponse;
		try {
			resourceResponse = oAuthClient.resource(bearerClientRequest, "GET", OAuthResourceResponse.class);
		} catch (OAuthSystemException e) {
			throw new RuntimeException(e);
		} catch (OAuthProblemException e) {
			throw new RuntimeException(e);
		}

        String json = resourceResponse.getBody();
        resp.getWriter().println(json);
        resp.getWriter().close();
	}
}
