package com.risevision.medialibrary.server.info;

import java.io.Serializable;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONStringer;
import com.google.appengine.labs.repackaged.org.json.JSONWriter;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.medialibrary.server.data.DataService;

@SuppressWarnings("serial")
public class CompanyInfo implements Serializable {
	
	private String id;
	private String name;
	private boolean authorized;
	
	private String enabledFeaturesJson;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isAuthorized() {
		return authorized;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	public String getEnabledFeaturesJson() {
		return enabledFeaturesJson;
	}
	
	public void setEnabledFeaturesJson(String enabledFeaturesJson) {
		this.enabledFeaturesJson = enabledFeaturesJson;
	}
	
	public boolean isMediaLibraryEnabled() {
		return id.equals(DataService.getInstance().getConfig().getRiseId()) 
				|| !RiseUtils.strIsNullOrEmpty(enabledFeaturesJson);
	}

	public void enableMediaLibrary() {
		try {
			// [{"name":"Media Library","value":"true"}] 
			JSONWriter stringer = new JSONStringer();
			
			stringer.array();
			stringer.object();
			stringer.key("name").value("Media Library");
			stringer.key("value").value("true");
			stringer.endObject();
			stringer.endArray();

			enabledFeaturesJson = stringer.toString();		

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}