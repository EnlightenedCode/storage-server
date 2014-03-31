package com.risevision.storage.gcs;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

//import com.google.api.client.auth.security.PrivateKeys;

public class ServiceAccountAccess {

	/**
	 * Sets the private key to use with the the service account flow or
	 * {@code null} for none.
	 * 
	 * <p>
	 * Overriding is only supported for the purpose of calling the super
	 * implementation and changing the return type, but nothing else.
	 * </p>
	 * 
	 * @param p12File
	 *            input stream to the p12 file (closed at the end of this method
	 *            in a finally block)
	 */
	public byte[] setServiceAccountPrivateKeyFromP12File(File p12File)
			throws GeneralSecurityException, IOException {
//		PrivateKey serviceAccountPrivateKey = PrivateKeys.loadFromP12File(p12File, "notasecret", "privatekey", "notasecret");
//
//		return serviceAccountPrivateKey.getEncoded();
		
		return null;
	}
}
