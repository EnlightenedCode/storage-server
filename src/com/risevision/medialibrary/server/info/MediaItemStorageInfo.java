package com.risevision.medialibrary.server.info;

import java.util.Date;

public class MediaItemStorageInfo {
	private Date time;
	private String logName;
	private long bytes;
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
	
	public String getLogName() {
		return logName;
	}
	
	public void setLogName(String logName) {
		this.logName = logName;
	}
	
	public long getBytes() {
		return bytes;
	}
	
	public void setBytes(long bytes) {
		this.bytes = bytes;
	}
	
}
