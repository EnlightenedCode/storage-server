package com.risevision.medialibrary.server.service;

import java.io.IOException;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;

import com.risevision.medialibrary.server.info.ServiceFailedException;
import com.risevision.medialibrary.server.oauth.HttpOAuthHelper;
import com.risevision.medialibrary.server.utils.ServerUtils;

public abstract class RiseService {
//	private static boolean isDevelopmentMode = false;
	
	private static String getMethod = "GET";
//	private static String putMethod = "PUT";
//	private static String postMethod = "POST";
//	private static String deleteMethod = "DELETE";
	
	public static final String URL_PATH_V1 = "v1";
	public static final String URL_PATH_V2 = "v2";
	
	protected static final Logger log = Logger.getAnonymousLogger();

	public RiseService() {
		init();
	}
	
	private void init() {
//		String serverInfo = getServletContext().getServerInfo();
//		/* ServletContext.getServerInfo() will return "Google App Engine Development/x.x.x"
//		* if will run locally, and "Google App Engine/x.x.x" if run on production environment */
//		if (serverInfo.contains("Development")) {
//			isDevelopmentMode = true;
//		}else {
//			isDevelopmentMode = false;
//		}
		
		Engine.getInstance().getRegisteredAuthenticators().add(new HttpOAuthHelper());
	}
	
	public Document get(String url) throws ServiceFailedException {
		return get(url, true);
	}
	
	public Document get(String url, boolean useAuthentication) throws ServiceFailedException {
		ClientResource clientResource = createResource(url, getMethod, useAuthentication);
		
		try {
//			if (isDevelopmentMode) {
//				clientResource.get();
//				ServerUtils.writeDebugInfo(clientResource);
//			}
			
			DomRepresentation representation = new DomRepresentation(clientResource.get(MediaType.TEXT_XML));
	
			if (clientResource.getStatus().isSuccess() && clientResource.getResponseEntity().isAvailable()) {
				try {
					Document d = representation.getDocument();

					return d;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (ResourceException e) {
			handleResourceException(e, clientResource);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private ClientResource createResource(String url, String method, boolean useAuthentication) throws ServiceFailedException {
		return createResource(url, URL_PATH_V1, method, useAuthentication);
	}
	
	private ClientResource createResource(String url, String urlPath, String method, boolean useAuthentication) throws ServiceFailedException {
		String fullUrl = ServerUtils.formatUrl(url, urlPath);
		ClientResource clientResource = new ClientResource(fullUrl);

		if (useAuthentication) {
			ServerUtils.signResource(clientResource, fullUrl, method);
		}
			
		return clientResource;
	}
	
	private void handleResourceException(ResourceException e, ClientResource clientResource) throws ServiceFailedException {
//		if (isDevelopmentMode) {
//			System.out.printf("Status: %s %s", clientResource.getStatus().getCode(), 
//					clientResource.getStatus().getDescription());
//			System.out.println();
//		}
		
		if (e.getStatus().getCode() >= 500) {
			log.severe(e.getStatus().getCode() + " - " + e.getMessage());
		}
		else {
			log.warning(e.getStatus().getCode() + " - " + e.getMessage());
		}
		
		if (e.getStatus().equals(Status.CLIENT_ERROR_BAD_REQUEST)) {
			throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
		}
		else if (e.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
			throw new ServiceFailedException(ServiceFailedException.AUTHENTICATION_FAILED);
		}
		else if (e.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
			throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
		}
		else if (e.getStatus().equals(Status.CLIENT_ERROR_GONE)) {
			throw new ServiceFailedException(ServiceFailedException.ENTITY_GONE);
		}
		else if (e.getStatus().equals(Status.CLIENT_ERROR_CONFLICT)) {
			throw new ServiceFailedException(ServiceFailedException.CONFLICT);
		}
		else if (e.getStatus().equals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED)) {
			throw new ServiceFailedException(ServiceFailedException.METHOD_NOT_ALLOWED);
		}
		else if (e.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
			throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
		}
		else {
			throw new ServiceFailedException();
		}
	}

}
