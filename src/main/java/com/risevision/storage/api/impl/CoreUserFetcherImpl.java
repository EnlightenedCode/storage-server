package com.risevision.storage.api.impl;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.risevision.storage.Globals;
import com.risevision.storage.api.CoreUserFetcher;
import com.risevision.storage.entities.core.CoreResponse;
import com.risevision.storage.entities.core.CoreUser;
import com.risevision.storage.gcs.ServiceAccountAPIRequestor;
import com.risevision.storage.info.ServiceFailedException;

public class CoreUserFetcherImpl implements CoreUserFetcher {
  private static final String HTTP_CHARSET = "UTF-8";
  private static final Logger log = Logger.getAnonymousLogger();
  
  private Gson gson;
  
  public CoreUserFetcherImpl() {
    this.gson = new Gson();
  }

  @Override
  public CoreResponse<CoreUser> getCoreUser(String email) throws ServiceFailedException {
    Type crType = new TypeToken<CoreResponse<CoreUser>>() {}.getType();
    
    try {
      GenericUrl url = new GenericUrl(Globals.USER_GET_URL.replace("EMAIL", URLEncoder.encode(email, HTTP_CHARSET)));
      HttpResponse httpResponse = ServiceAccountAPIRequestor.makeRequest(ServiceAccountAPIRequestor.SERVICE_ACCOUNT.CORE, "GET", url, null);
      String response = httpResponse.parseAsString();
      
      return gson.fromJson(response, crType);
    } catch (HttpResponseException e) {
      return gson.fromJson(e.getContent(), crType);      
    } catch (Exception e) {
      log.log(Level.SEVERE, "getCoreUser", e);
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }
}
