package com.risevision.storage.gcs;

import java.io.IOException;

import com.google.api.client.googleapis.testing.json.*;
import com.google.api.client.googleapis.testing.services.json.*;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.*;
import com.google.api.client.http.*;

import com.google.api.services.storage.*;
import com.google.api.services.storage.model.*;

public class GCSMockClientBuilder {
  HttpTransport transport;

  final JsonFactory jsonFactory = new JacksonFactory();

  public GCSMockClientBuilder
  (final String mockResponse, final int mockStatusCode) {
    transport = new MockHttpTransport() {
      MockedRequest mockedRequest;

      public LowLevelHttpRequest buildRequest(String name, String url) {
        mockedRequest = new MockedRequest(mockResponse, mockStatusCode);
        return mockedRequest;
      }

      class MockedRequest extends MockLowLevelHttpRequest {
        final String mockResponse;
        final int mockStatusCode;

        MockedRequest(String mockResponse, int mockStatusCode) {
          super();
          this.mockResponse = mockResponse;
          this.mockStatusCode = mockStatusCode;
          System.out.println("HELLO " + getUrl());
        }

        public LowLevelHttpResponse execute() throws IOException {
          System.out.println("HELLO " + getUrl());
          MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
          result.setStatusCode(mockStatusCode);
          result.setContentType(Json.MEDIA_TYPE);
          result.setContent(mockResponse);
          if (mockStatusCode > 299) {
            String jsonErrorMessage = "Mocked server side error";
            throw GoogleJsonResponseExceptionFactoryTesting.newMock
            (jsonFactory, mockStatusCode, jsonErrorMessage);
          }
          System.out.println("HELLO " + getUrl());
          return result;
        }
      }
    };
  }

  public GCSMockClientBuilder() {
    this("{}", 200);
  }

  public GCSMockClientBuilder(String response) {
    this(response, 200);
  }

  public GCSMockClientBuilder(int statusCode) {
    this("{}", statusCode);
  }

  public Storage build() {
    return new Storage.Builder
      (transport, jsonFactory, null)
      .setApplicationName("Test Applicatoin").build();
  }
}


