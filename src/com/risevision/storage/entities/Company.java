package com.risevision.storage.entities;

import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.risevision.storage.CacheUtils;
import com.risevision.storage.EntityKind;

public class Company extends AbstractEntity {

	private static final long serialVersionUID = 5539663986568744032L;

	private static final String KEY_PREFIX = "e#" + EntityKind.COMPANY.toLowerCase() + "_";
	
	// field names
	
	static public final String ID = "id";
	static public final String PARENT_ID = "parentId";
	static public final String NAME = "name";
	static public final String CREATION_DATE = "creationDate";
	static public final String STREET = "street";
	static public final String UNIT = "unit";
	static public final String CITY = "city";
	static public final String PROVINCE = "province";
	static public final String COUNTRY = "country";
	static public final String POSTAL_CODE = "postalCode";
	static public final String TIME_ZONE_OFFSET = "timeZoneOffset";
	static public final String TELEPHONE = "telephone";
	static public final String FAX = "fax";
	static public final String ENABLED_FEATURES = "enabledFeatures";
	static public final String ACTIVE = "active";
	static public final String ACTIVE_CHANGE_DATE = "activeChangeDate";
	static public final String LAST_ACTIVITY_DATE = "lastActivityDate";
	static public final String CHANGE_DATE = "changeDate";
	
	// fields

	public String id;
	public String parentId;
	public String name;
	public Date creationDate;
	public String street;
	public String unit;
	public String city;
	public String province;
	public String country;
	public String postalCode;
	public Integer timeZoneOffset;
	public String telephone;
	public String fax;
	public String enabledFeatures;
	public Boolean active;
	public Date activeChangeDate;
	public Date lastActivityDate;
	public Date changeDate;

	public Company(String id, String parentId, String name, Date creationDate, String street, String unit, String city, String province, String country, 
			String postalCode, Integer timeZoneOffset, String telephone, String fax, String enabledFeatures, Boolean active, Date activeChangeDate, Date lastActivityDate, Date changeDate) {

		super();
		
		this.id = id;
		this.parentId = parentId;
		this.name = name;
		this.creationDate = creationDate;
		this.street = street;
		this.unit = unit;
		this.city = city;
		this.province = province;
		this.country = country;
		this.postalCode = postalCode;
		this.timeZoneOffset = timeZoneOffset;
		this.telephone = telephone;
		this.fax = fax;
		this.enabledFeatures = enabledFeatures;
		this.active = active;
		this.activeChangeDate = activeChangeDate;
		this.lastActivityDate = lastActivityDate;
		this.changeDate = changeDate;
	}
	
	public Company(Entity e) {
		super(e);
	}
	
	public Company(Key k, Map<String, Object> p) {
		super(k, p);
	}
	
//	public Company(String id, Form form) {
//		
//		super(form);
//		this.id = id;
//	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String getEntityKind() {
		return EntityKind.COMPANY;
	}
	
	@Override
	public void getProperties(Entity e)  {

		Map<String, Object> p = e.getProperties();
	
		getProperties(p);
	}
	
	@Override
	public void getProperties(Map<String, Object> p)  {

		this.id = (String) p.get(ID);
		this.parentId = (String) p.get(PARENT_ID);
		this.name = (String) p.get(NAME);
		this.creationDate = (Date) p.get(CREATION_DATE);
		this.street = (String) p.get(STREET);
		this.unit = (String) p.get(UNIT);
		this.city = (String) p.get(CITY);
		this.province = (String) p.get(PROVINCE);
		this.country = (String) p.get(COUNTRY);
		this.postalCode = (String) p.get(POSTAL_CODE);
		this.timeZoneOffset = p.get(TIME_ZONE_OFFSET) != null ? ((Long) p.get(TIME_ZONE_OFFSET)).intValue() : null;
		this.telephone = (String) p.get(TELEPHONE);
		this.fax = (String) p.get(FAX);
		this.enabledFeatures = p.get(ENABLED_FEATURES) != null ? ((Text) p.get(ENABLED_FEATURES)).getValue() : "";
		this.active = (Boolean) p.get(ACTIVE);
		this.activeChangeDate = (Date) p.get(ACTIVE_CHANGE_DATE);
		this.lastActivityDate = (Date) p.get(LAST_ACTIVITY_DATE);
		this.changeDate = (Date) p.get(CHANGE_DATE);

	}
	
	@Override
	public void recordChange(Date changeDate) {
		
		this.changeDate = changeDate;
		if (this.creationDate == null) {
			this.creationDate = changeDate;
		}
	}	
	
	@Override
	public void setProperties(Entity e) {
		
		e.setProperty(ID, id);
		e.setProperty(PARENT_ID, parentId != null ? parentId : "");
		e.setProperty(ACTIVE, active); //!!
		e.setProperty(LAST_ACTIVITY_DATE, lastActivityDate); //!!

		
		e.setUnindexedProperty(NAME, name);
		e.setUnindexedProperty(CREATION_DATE, creationDate);
		e.setUnindexedProperty(STREET, street);
		e.setUnindexedProperty(UNIT, unit);
		e.setUnindexedProperty(CITY, city);
		e.setUnindexedProperty(PROVINCE, province);
		e.setUnindexedProperty(COUNTRY, country);
		e.setUnindexedProperty(POSTAL_CODE, postalCode);
		e.setUnindexedProperty(TIME_ZONE_OFFSET, timeZoneOffset.longValue());
		e.setUnindexedProperty(TELEPHONE, telephone);
		e.setUnindexedProperty(FAX, fax);
		e.setUnindexedProperty(ENABLED_FEATURES, enabledFeatures != null && !enabledFeatures.isEmpty() ? new Text(enabledFeatures) : null);
		//e.setUnindexedProperty(ACTIVE, active);
		e.setUnindexedProperty(ACTIVE_CHANGE_DATE, activeChangeDate);
		e.setUnindexedProperty(CHANGE_DATE, changeDate);
	}
		
	public static Company get(String id) {
		
		return id != null ? CacheUtils.get(Company.class, id) :  null;
		
	}
	
	public static Company get(String id, Key key) {
		
		return key != null ? CacheUtils.get(Company.class, id, key) :  null;
		
	}
	
	// *************************** Form support **********************************
	
	@Override
	public void setDefaults() {
		
		this.parentId = null;
		
		this.street = "";
    	this.unit = "";
    	this.city = "";
    	this.province = "";
    	this.country = "";
    	this.postalCode = "";
    	this.timeZoneOffset = 0;
    	this.telephone = "";
    	this.fax = "";
    	this.enabledFeatures = null;
    	this.active = null;
    	this.activeChangeDate = null;
    	this.lastActivityDate = null;
	}
	
	// ************************* Cacheable support *******************************
	

	static public String getKeyPrefix() {

		return KEY_PREFIX;
	}

	static public Company recreate(String id) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q = new Query(EntityKind.COMPANY).setFilter(new FilterPredicate(ID, Query.FilterOperator.EQUAL, id));
		PreparedQuery pq = datastore.prepare(q);
		Entity e = pq.asSingleEntity();

		Company result = e != null ? new Company(e) : null;
		
		return result;
	}
	
	static public Company recreate(Key key) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Company result = null;

		try {
		
			Entity e = datastore.get(key);
			result = new Company(e);
		
		} catch (EntityNotFoundException ex) {
			Logger.getAnonymousLogger().warning("Error: " + ex.getMessage());
			result = null;
		}
		
		return result;
	}

}
