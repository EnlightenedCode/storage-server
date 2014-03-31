//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
//  affiliates.

package com.risevision.storage.amazonImpl;

import java.net.HttpURLConnection;
import java.io.IOException;

import com.risevision.storage.info.ServiceFailedException;


/**
 * The parent class of all other Responses.  This class keeps track of the
 * HttpURLConnection response.
 */
public class Response {
    public HttpURLConnection connection;

    public Response(HttpURLConnection connection) throws IOException, ServiceFailedException {
        this.connection = connection;
        
        if (connection != null && connection.getResponseCode() >= 400) {
			System.out.printf("Status: %s %s", connection.getResponseCode(), connection.getResponseMessage());
			System.out.println();

	        if (connection.getResponseCode() == ServiceFailedException.AUTHENTICATION_FAILED) {
				throw new ServiceFailedException();
	        }
	        else {
				throw new ServiceFailedException(connection.getResponseCode());
	        }
        }
    }
}
