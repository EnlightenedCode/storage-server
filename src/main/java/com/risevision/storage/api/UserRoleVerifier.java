package com.risevision.storage.api;

import org.apache.commons.lang.StringUtils;

import com.risevision.storage.Globals;
import com.risevision.storage.api.impl.CoreUserFetcherImpl;
import com.risevision.storage.entities.core.CoreError;
import com.risevision.storage.entities.core.CoreResponse;
import com.risevision.storage.entities.core.CoreUser;
import com.risevision.storage.info.ServiceFailedException;

public class UserRoleVerifier extends AbstractVerifier {
  public void verifyUserRoles(String email, String... roles) throws ServiceFailedException {
    email = replaceWithLocalUserEmail(email);

    log.info("Verifying user " + email + " has the roles: " + StringUtils.join(roles, ", "));

    if (Globals.devserver) {return;}

    CoreUserFetcher coreUserFetcher = new CoreUserFetcherImpl();
    CoreResponse<CoreUser> response = coreUserFetcher.getCoreUser(email);
    CoreUser user = response.getItem();
    CoreError error = response.getError();
    
    if(error != null) {
      throw new ServiceFailedException(error.getCode(), error.getMessage());
    }
    else {
      for(String role : roles) {
        if(!user.getRoles().contains(role)) {
          throw new ServiceFailedException(403, "User " + email + " does not have role " + role);
        }
      }
    }
  }
  
  public void verifyContentProducer(String email) throws ServiceFailedException {
    verifyUserRoles(email, "cp");
  }
}
