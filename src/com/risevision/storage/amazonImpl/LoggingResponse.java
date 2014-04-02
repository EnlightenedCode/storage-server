//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006-2007 Amazon Digital Services, Inc. or its
//  affiliates.

package com.risevision.storage.amazonImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.risevision.storage.info.ServiceFailedException;


/**
 * A Response object returned from AWSAuthConnection.getBucketLocation().
 * Parses the response XML and exposes the location constraint
 * via the geteLocation() method.
 */
public class LoggingResponse extends Response {
    boolean logging;
    
    public LoggingResponse(HttpURLConnection connection) throws IOException, ServiceFailedException {
        super(connection);

        if (connection.getResponseCode() < 400) {
        	checkBucketLogging(connection.getInputStream());
        } else {
	        throw new ServiceFailedException(ServiceFailedException.NOT_FOUND);
	    }
    }

    public LoggingResponse(InputStream inputStream) throws IOException, ServiceFailedException {
    	super(null);
    	
    	checkBucketLogging(inputStream);
    }
    
    /**
     * Parse the response to a ?location query.
     * @throws ServiceFailedException 
     */
    public void checkBucketLogging(InputStream inputStream) throws IOException, ServiceFailedException {
        try {
            XMLReader xr = Utils.createXMLReader();;
            LoggingResponseHandler handler = new LoggingResponseHandler();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            xr.parse(new InputSource(inputStream));
            this.logging = handler.logging;
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected error parsing ListAllMyBuckets xml", e);
        }
    }

    /**
     * Report the location-constraint for a bucket.
     * A value of null indicates an error; 
     * the empty string indicates no constraint;
     * and any other value is an actual location constraint value.
     */
    public boolean getLogging() {
        return logging;
    }

    /**
     * Helper class to parse LocationConstraint response XML
     */
    static class LoggingResponseHandler extends DefaultHandler {
        boolean logging = false;
//        private StringBuffer currText = null;
        
        public void startDocument() {
        }

        public void startElement(String uri, String name, String qName, Attributes attrs) {
            if (name.equals("LogBucket")) {
                logging = true;
            }
        }

//        public void endElement(String uri, String name, String qName) {
//            if (name.equals("LocationConstraint")) {
//                logging = this.currText.toString();
//                this.currText = null;
//            }
//        }
        
//        public void characters(char ch[], int start, int length) {
//            if (currText != null)
//                this.currText.append(ch, start, length);
//        }
    }
}
