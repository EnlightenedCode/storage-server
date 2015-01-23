package com.risevision.storage.api;

import com.risevision.storage.entities.core.CoreResponse;
import com.risevision.storage.entities.core.CoreUser;
import com.risevision.storage.info.ServiceFailedException;

public interface CoreUserFetcher {
  public CoreResponse<CoreUser> getCoreUser(String email) throws ServiceFailedException;
}
