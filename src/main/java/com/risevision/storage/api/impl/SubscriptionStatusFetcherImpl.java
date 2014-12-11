package com.risevision.storage.api.impl;

import java.net.MalformedURLException;
import java.net.URLEncoder;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.gson.Gson;
import com.risevision.storage.Globals;
import com.risevision.storage.api.SubscriptionStatusFetcher;
import com.risevision.storage.entities.SubscriptionStatus;
import com.risevision.storage.info.ServiceFailedException;

public class SubscriptionStatusFetcherImpl implements SubscriptionStatusFetcher {
  private static final String HTTP_CHARSET = "UTF-8";
  private static HttpRequestFactory httprequestFactory = new UrlFetchTransport().createRequestFactory();
  
  @Override
  public SubscriptionStatus getSubscriptionStatus(String companyId) throws ServiceFailedException {
    try {
      GenericUrl url = new GenericUrl(Globals.SUBSCRIPTION_STATUS_URL.replace("companyId", URLEncoder.encode(companyId, HTTP_CHARSET)));
      HttpRequest request = httprequestFactory.buildGetRequest(url);
      
      return new Gson().fromJson(request.execute().parseAsString(), SubscriptionStatus[].class)[0];
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
      throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
  }
}
