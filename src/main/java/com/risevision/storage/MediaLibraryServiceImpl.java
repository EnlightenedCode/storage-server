package com.risevision.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.util.Base64;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.StringUtils;
import com.google.common.base.Strings;
import com.risevision.storage.amazonImpl.ListAllMyBucketsResponse;
import com.risevision.storage.amazonImpl.ListBucketResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.gcs.ServiceAccountAPIRequestor;

public class MediaLibraryServiceImpl extends MediaLibraryService {
	
	public static final int MAX_KEYS = 1000;
	
	/** Global configuration of Google Cloud Storage OAuth 2.0 scope. */
	private static final String STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.full_control";
	
	protected MediaLibraryServiceImpl() {
		
	}

	public ListAllMyBucketsResponse getAllMyBuckets() throws ServiceFailedException {
		try {
			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));

//			bucketName = "first-bucket-test";
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildGetRequest(url);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", Globals.PROJECT_ID);
			
			request.setHeaders(headers);
			HttpResponse response = request.execute();

//			log.warning(response.parseAsString());

			ListAllMyBucketsResponse listAllBucketsResponse = new ListAllMyBucketsResponse(response.getContent());
			
			return listAllBucketsResponse;
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage());
			
			throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

		return null;
	}
	
	public String getBucketPropertyString(String bucketName, String property) throws ServiceFailedException {
		try {
			return getBucketPropertyResponse(bucketName, property).parseAsString();
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}
		return null;
	}
	
	public InputStream getBucketProperty(String bucketName, String property) throws ServiceFailedException {
		try {
			return getBucketPropertyResponse(bucketName, property).getContent();
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}
		return null;
	}
		
	private HttpResponse getBucketPropertyResponse(String bucketName, String property) throws ServiceFailedException {	
		try {
			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));

//			bucketName = "first-bucket-test";
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName + "?" + property;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildGetRequest(url);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", Globals.PROJECT_ID);
			
			request.setHeaders(headers);
			HttpResponse response = request.execute();

//			log.warning(response.parseAsString());

			return response;
			
		} catch (HttpResponseException e) {
			if (e.getStatusCode() != ServiceFailedException.NOT_FOUND) {
				log.warning(e.getStatusCode() + " - " + e.getMessage());
			}
			
			throw new ServiceFailedException(e.getStatusCode());
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

		return null;
	}
	
	public List<MediaItemInfo> getBucketItems(String bucketName, String prefix, String marker) throws ServiceFailedException {
		try {
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName;
			if (!Strings.isNullOrEmpty(prefix)) {
				URI += "?prefix=" + prefix;
			}
			if (!Strings.isNullOrEmpty(marker)) {
				URI += URI.contains("?") ? "&" : "?";
				URI += "marker=" + marker;
			}

			URI += URI.contains("?") ? "&" : "?";
			URI += "max-keys=" + MAX_KEYS;
			
			GenericUrl url = new GenericUrl(URI);

			HttpResponse response = ServiceAccountAPIRequestor.makeRequest
                        (ServiceAccountAPIRequestor.SERVICE_ACCOUNT.MEDIA_LIBRARY, "GET", url, null);
//			
//			request.setHeaders(headers);

//			log.warning(response.parseAsString());

			ListBucketResponse listBucketResponse = new ListBucketResponse(response.getContent());
			
			List<MediaItemInfo> mediaItems = (ArrayList<MediaItemInfo>) listBucketResponse.entries;
			
			return mediaItems;
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage());
			
			throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

		return null;
	}
	
        public void createFolder(String bucketName, String folderName)
        throws ServiceFailedException {
                       log.info("folder creation not implemented in " + 
                                 "MediaLibraryServiceImpl.java");
        }

	public void createBucket(String bucketName) throws ServiceFailedException {
		try {
			log.info("Creating bucket");

			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));
	
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildPutRequest(url, null);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", Globals.PROJECT_ID);
			
			request.setHeaders(headers);

			request.execute();
			
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage());
			if (e.getStatusCode() != 409) {
                          throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
                        }
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

	}
	
	public void updateBucketProperty(String bucketName, String property, String propertyXMLdoc) throws ServiceFailedException {
		try {
			log.info("Update Bucket Property");

			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));
	
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName + "?" + property;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildPutRequest(url, ByteArrayContent.fromString(null, propertyXMLdoc));
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", Globals.PROJECT_ID);
			
			request.setHeaders(headers);

			request.execute();
			
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage());
			
			throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

	}
	
	public boolean deleteMediaItem(String bucketName, String itemName) throws ServiceFailedException {
		try {
			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));
	
			itemName = URLEncoder.encode(itemName, "UTF-8");
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName + "/" + itemName;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildDeleteRequest(url);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", Globals.PROJECT_ID);
			
			request.setHeaders(headers);

			request.execute();
			
			return true;
			
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage() + " (" + itemName + ")");
			
			throw new ServiceFailedException(e.getStatusCode());
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}
		
		return false;

	}
	
	public InputStream getMediaItem(String bucketName, String itemName) throws ServiceFailedException {
		try {
			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));
	
			itemName = URLEncoder.encode(itemName, "UTF-8");
			String URI = MediaItemInfo.MEDIA_LIBRARY_URL + bucketName + "/" + itemName;
			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
			
			GenericUrl url = new GenericUrl(URI);
			HttpRequest request = requestFactory.buildGetRequest(url);
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-goog-project-id", Globals.PROJECT_ID);
			
			request.setHeaders(headers);

			HttpResponse response = request.execute();			
			
			return response.getContent();
			
		} catch (HttpResponseException e) {
			log.warning(e.getStatusCode() + " - " + e.getMessage());
			
			throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
		} catch (IOException e) {
			log.severe("Error - " + e.getMessage());
		}

		return null;
	}
	
	public String getMediaItemUrl(String bucketName, String key) throws Exception {
		long expiry = new Date().getTime() + 3600;
		String stringPolicy = "GET\n" + "\n" + "\n" + expiry + "\n" + '/' + bucketName + '/' + key;

		String signedPolicy = getSignedPolicy(stringPolicy);
		
		log.info("Policy Signed");
		
		String fileUrl = "http://" + bucketName + ".storage.googleapis.com/" 
				+ key
				+ "?GoogleAccessId=" + Globals.RVMEDIA_ID 
				+ "&Expires=" + expiry 
				+ "&Signature=" + URLEncoder.encode(signedPolicy, "UTF-8")
				+ "&response-content-disposition=attachment";
		
		return fileUrl;
	}
	
//    public String getSignedPolicy(final String stringToSign) {
//        AppIdentityService identityService = AppIdentityServiceFactory.getAppIdentityService();
//
//        log.warning(identityService.getServiceAccountName());
//        final SigningResult signingResult = identityService.signForApp(stringToSign.getBytes());
//        String encodedSignature = "";
//		try {
//			encodedSignature = new String(org.apache.commons.codec.binary.Base64.encodeBase64(signingResult.getSignature(), false), "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        return encodedSignature;
//    }

	
	public String getSignedPolicy(String policyBase64) {		
		PrivateKey privateKey = null;
		try {
			privateKey = setServiceAccountPrivateKeyFromP12File();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	    byte[] contentBytes = StringUtils.getBytesUtf8(policyBase64);
	    try {
//		    Signature signer = Signature.getInstance("SHA256withRSA");
	    	Signature signer = SecurityUtils.getSha256WithRsaSignatureAlgorithm();
	    	
		    signer.initSign(privateKey);
		    signer.update(contentBytes);
		    byte[] signature = signer.sign();
		    return Base64.encodeBase64String(signature);
	    } catch (GeneralSecurityException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
	    
	    return null;
	}
	
	/**
	 * Sets the private key to use with the the service account flow or
	 * {@code null} for none.
	 * 
	 * <p>
	 * Overriding is only supported for the purpose of calling the super
	 * implementation and changing the return type, but nothing else.
	 * </p>
	 * 
	 * @param p12File
	 *            input stream to the p12 file (closed at the end of this method
	 *            in a finally block)
	 */
	public PrivateKey setServiceAccountPrivateKeyFromP12File()
			throws GeneralSecurityException, IOException {
//		String p12FileName = "key/65bd1c5e62dadd4852c8b04bf5124749985e8ff8-privatekey.p12";
		String p12FileName = "/65bd1c5e62dadd4852c8b04bf5124749985e8ff8-privatekey.p12";
		
//		ServletContext context = getServletContext();
//		InputStream is = context.getResourceAsStream(p12FileName);
		
//		InputStream is = this.getClass().getClassLoader().getResourceAsStream(p12FileName);
		InputStream is = MediaLibraryServiceImpl.class.getResourceAsStream(p12FileName);
		
		PrivateKey serviceAccountPrivateKey = SecurityUtils.loadPrivateKeyFromKeyStore(KeyStore.getInstance("PKCS12"), is, "notasecret", "privatekey", "notasecret");

		return serviceAccountPrivateKey;
	}
}
