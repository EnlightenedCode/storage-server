package com.risevision.storage.api;

import java.util.logging.Logger;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.risevision.storage.Globals;
import com.risevision.storage.info.ServiceFailedException;

public abstract class AbstractVerifier {
  protected static final String HTTP_CHARSET = "UTF-8";
  protected static final Logger log = Logger.getAnonymousLogger();

  protected String replaceWithLocalUserEmail(String email) throws ServiceFailedException {
    if (Globals.devserver) {
      User localUser = UserServiceFactory.getUserService().getCurrentUser();
      if (localUser == null) {
        String loginURL = "http://localhost:8888/_ah/login?continue=%2f";
        log.warning("Local user not logged in. Log in at " + loginURL);
        throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
      }
      return localUser.getEmail();
    }else {
      return email;
    }
  }
}
