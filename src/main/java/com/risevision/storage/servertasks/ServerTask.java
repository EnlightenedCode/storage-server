package com.risevision.storage.servertasks;

import java.util.Map;
import java.io.IOException;

import java.util.logging.Logger;
import com.google.common.base.Strings;

import com.google.api.services.storage.Storage;

abstract class ServerTask {
  Storage gcsStorageClient;
  Map<String, String[]> requestParams;
  protected static final Logger log = Logger.getAnonymousLogger();

  ServerTask(Storage client, Map<String, String[]> params) {
    this.gcsStorageClient = client;
    this.requestParams = params;
  }

  protected void verifyParams(String... expectedParams)
  throws IllegalArgumentException {
    for (String expectedParam : expectedParams) {
      if (requestParams.get(expectedParam) == null ||
      Strings.isNullOrEmpty(requestParams.get(expectedParam)[0])) {
        log.severe("Missing parameter " + expectedParam);
        throw new IllegalArgumentException("Missing parameter " + expectedParam);
      }
    }
  }

  abstract void handleRequest() throws IOException;
}
