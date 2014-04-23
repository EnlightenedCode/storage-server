package com.risevision.storage.gcs;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP servlet to process access granted from user.
 * 
 * @author Nick Miceli
 */
public class AuthCallbackServlet extends AbstractAppEngineAuthorizationCodeCallbackServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential) 
			throws ServletException, IOException {
		resp.sendRedirect(AuthUtils.MAIN_SERVLET_PATH);
	}

	@Override
	protected void onError(HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
			throws ServletException, IOException {
//		String nickname = UserServiceFactory.getUserService().getCurrentUser()
//				.getNickname();
		resp.getWriter().print("Not allowed");
		resp.setStatus(200);
		resp.addHeader("Content-Type", "text/html");
		return;
	}

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
		return AuthUtils.initializeFlow();
	}

	@Override
	protected String getRedirectUri(HttpServletRequest req)	throws ServletException, IOException {
		return AuthUtils.getRedirectUri(req);
	}

}