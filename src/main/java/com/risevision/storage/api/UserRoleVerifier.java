package com.risevision.storage.api;

import org.apache.commons.lang.StringUtils;

import com.risevision.storage.Globals;
import com.risevision.storage.api.impl.CoreUserFetcherImpl;
import com.risevision.storage.entities.core.CoreError;
import com.risevision.storage.entities.core.CoreResponse;
import com.risevision.storage.entities.core.CoreUser;
import com.risevision.storage.info.ServiceFailedException;

public class UserRoleVerifier extends AbstractVerifier {
  public enum RequiredRole {
    ANY, ALL;
  }
  
  public void verifyUserRoles(String email, RequiredRole requiredRole, String... roles) throws ServiceFailedException {
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
    else if(requiredRole == RequiredRole.ANY) {
      for(String role : roles) {
        if(user.getRoles().contains(role)) {
          return;
        }
      }
      
      throw new ServiceFailedException(403, "User " + email + " does not have any of the provided roles: " + StringUtils.join(roles, ", "));
    }
    else if(requiredRole == RequiredRole.ALL) {
      for(String role : roles) {
        if(!user.getRoles().contains(role)) {
          throw new ServiceFailedException(403, "User " + email + " does not have role " + role);
        }
      }
    }
  }
  
  public void verifyPrivilegedRole(String email) throws ServiceFailedException {
    verifyUserRoles(email, RequiredRole.ANY, "ua", "ce", "cp");
  }
}
