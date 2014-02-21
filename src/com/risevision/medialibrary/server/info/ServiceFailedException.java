package com.risevision.medialibrary.server.info;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class ServiceFailedException  extends Exception implements Serializable {
//	private int errorCode;
	public static final int BAD_REQUEST = 400;
	public static final int AUTHENTICATION_FAILED = 401;
	public static final int ENTITY_GONE = 410;
	public static final int FORBIDDEN = 403;
	public static final int NOT_FOUND = 404;
	public static final int CONFLICT = 409;
	public static final int METHOD_NOT_ALLOWED = 405;
	
	public static final int PRECONDITION_FAILED = 412;

	public static final int SERVICE_FAILED = 0;
//	public static final int NOT_LOGGED_IN = 1;
	
	private int reason;

	public ServiceFailedException() {
	}
	
	public ServiceFailedException(int reason) {
		this.reason = reason;
	}
	
	public int getReason() {
		return reason;
	}
	
//	public void setErrorCode(int errorCode) {
//		this.errorCode = errorCode;
//	}
//
//	public int getErrorCode() {
//		return errorCode;
//	}
}
