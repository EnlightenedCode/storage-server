package com.risevision.medialibrary.server.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.risevision.core.api.attributes.CompanyAttribute;
import com.risevision.medialibrary.server.info.CompanyInfo;
import com.risevision.medialibrary.server.info.ServiceFailedException;
import com.risevision.medialibrary.server.utils.XmlUtils;

public class CompanyService extends RiseService {
	public static CompanyService instance;
	
	public static CompanyService getInstance() {
		try {
			if (instance == null)
				instance = new CompanyService();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	private CompanyService() {
		super();
	}
	
	public CompanyInfo getCompany(String companyId, String username) throws ServiceFailedException {
		String url = createCompanyResource(companyId);

		Document d = get(url, username);
		
		if (d != null){
			CompanyInfo company = docToCompany(d);
			return company;		
		}
		return null;
	}
	
	private String createCompanyResource(String companyId) {
		return "/company/" + companyId;
	}
	
	public static CompanyInfo docToCompany(Document doc) {
		try {
			doc.getDocumentElement().normalize();
			
			NodeList nodeList = doc.getElementsByTagName("company");

			Node fstNode = nodeList.item(0);

			CompanyInfo company = new CompanyInfo();
	
			if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

				Element fstElmnt = (Element) fstNode;
	
				company.setId(XmlUtils.getNode(fstElmnt, CompanyAttribute.ID));
				company.setName(XmlUtils.getNode(fstElmnt, CompanyAttribute.NAME));
				
				// Record retrieved successfully shows user has access to Company
				company.setAuthorized(true);
				
//				company.setEnabledFeaturesJson(XmlUtils.getNode(fstElmnt, CompanyAttribute.ENABLED_FEATURES));
				
			}	
			return company;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
//	public CompanyInfo saveCompany(CompanyInfo company, String username) throws ServiceFailedException {
//		// new user- generate userId
//		if ((company.getId() == null) || (company.getId().isEmpty()))
//			return null;
//		String url = createCompanyResource(company.getId());
//
//		Form form = new Form();
//
//		form.add(CompanyAttribute.ID, company.getId());
//		form.add(CompanyAttribute.ENABLED_FEATURES, company.getEnabledFeaturesJson());
//		
//		put(url, form, username);		
//		
//		return company;
//	}
	
}
