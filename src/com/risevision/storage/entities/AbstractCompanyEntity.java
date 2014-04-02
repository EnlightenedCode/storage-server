package com.risevision.storage.entities;

import java.util.Date;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;

public abstract class AbstractCompanyEntity extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	// field names

	static public final String COMPANY_ID = "companyId";
	static public final String CREATION_DATE = "creationDate";
	static public final String CHANGE_DATE = "changeDate";

	// fields

	public String companyId;
	public Date creationDate;
	public Date changeDate;

	public AbstractCompanyEntity(String companyId, Date creationDate, Date changeDate) {

		super();
		
		this.companyId = companyId;
		this.creationDate = creationDate;
		this.changeDate = changeDate;
	}
	
	public AbstractCompanyEntity(Entity e) {
		super(e);
	}
	
//	public AbstractCompanyEntity(String companyId, Form form) {
//		
//		super(form);
//		this.companyId = companyId;
//		this.creationDate = null;
//		this.changeDate = null;
//	}

	@Override
	public void getProperties(Map<String, Object> p) {
		
		this.companyId = (String) p.get(COMPANY_ID);
		this.creationDate = (Date) p.get(CREATION_DATE);
		this.changeDate = (Date) p.get(CHANGE_DATE);

	}

	@Override
	public void setProperties(Entity e) {

		e.setProperty(COMPANY_ID, companyId);

		e.setUnindexedProperty(CREATION_DATE, creationDate);
		e.setUnindexedProperty(CHANGE_DATE, changeDate);
	}
	
	@Override
	public void recordChange(Date changeDate) {
		
		this.changeDate = changeDate;
		if (this.creationDate == null) {
			this.creationDate = changeDate;
		}
	}
	
}
