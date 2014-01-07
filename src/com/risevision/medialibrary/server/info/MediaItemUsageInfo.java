package com.risevision.medialibrary.server.info;

import java.util.Date;

public class MediaItemUsageInfo {
	private Date time;
	private String ip;
	private String method;
	private String uri;
	private int status;
	private long inBytes;
	private long outBytes;
	private int timeTaken;
	private String userAgent;
	private String object;
	
//	time_micros", "c_ip", "c_ip_type",
//	"c_ip_region", "cs_method", "cs_uri", "sc_status", "cs_bytes",
//	"sc_bytes", "time_taken_micros", "cs_host", "cs_referer",
//	"cs_user_agent", "s_request_id", "cs_operation", "cs_bucket",
//	"cs_object
	
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public long getInBytes() {
		return inBytes;
	}
	
	public void setInBytes(long inBytes) {
		this.inBytes = inBytes;
	}
	
	public long getOutBytes() {
		return outBytes;
	}
	
	public void setOutBytes(long outBytes) {
		this.outBytes = outBytes;
	}
	
	public int getTimeTaken() {
		return timeTaken;
	}
	
	public void setTimeTaken(int timeTaken) {
		this.timeTaken = timeTaken;
	}
	
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getObject() {
		return object;
	}
	
	public void setObject(String object) {
		this.object = object;
	}
	
}
