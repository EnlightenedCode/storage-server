package com.risevision.medialibrary.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.risevision.core.api.types.DisplayStatus;
import com.risevision.core.api.types.UserStatus;
import com.risevision.medialibrary.server.entities.Company;

public class UpdateController {
	
	static public void process(Company company, String kind, String id) {
		
//		ProcessUpdate.Enqueue(company.id, kind, id);
	}
		
	static public void processImmediately(String companyId, String kind, String id) {
		/*
		Logger log = Logger.getAnonymousLogger();
		
		Company company = Company.get(companyId);
		if (company == null) {
			
			log.severe("Company " + companyId + " NOT found?!");
			return;
		}
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<String> displayIdsToUpdate = new ArrayList<String>();
		
//		if (kind.equals(EntityKind.SCHEDULE)) {
//
//			/// Step 1: find out which displays to notify
//		
//			Schedule schedule = CacheUtils.get(Schedule.class, id);
//			
//			if (schedule == null) {
//
//				// schedule has been deleted
//				
//				CompanySchedules companySchedules = CacheUtils.get(CompanySchedules.class, company.id);
//				
//				if (companySchedules.defaultScheduleId != null && companySchedules.defaultScheduleId.equals(id)) {
//					
//					// schedule is default for the company => update all displays in the company
//
//					Displays displays = CacheUtils.get(Displays.class, company.id);
//					
//					for (Display d : displays.displays) {
//						if (!displayIdsToUpdate.contains(d.id) && d.companyId.equals(company.id)) // only add displays of the actual company, NOT its sub-companies
//							displayIdsToUpdate.add(d.id);
//					}
//					
//				} else {
//
//					if (companySchedules.displaySchedule != null) {
//
//						for (Map.Entry<String, String> entry : companySchedules.displaySchedule.entrySet()) { 
//							if (entry.getValue().equals(id)) {
//								if (!displayIdsToUpdate.contains(entry.getKey()))
//									displayIdsToUpdate.add(entry.getKey());
//							}
//						}
//					}
//				}
//				
//			} else {
//				
//				if (schedule.distributeToAll) {
//					
//					// schedule is distributed to all displays => update all displays in the company
//					
//					Displays displays = CacheUtils.get(Displays.class, company.id);
//					
//					for (Display d : displays.displays) {
//						if (!displayIdsToUpdate.contains(d.id) && d.companyId.equals(company.id)) // only add displays of the actual company, NOT its sub-companies
//							displayIdsToUpdate.add(d.id);
//					}
//					
//				} else {
//					
//					displayIdsToUpdate.addAll(schedule.distribution);
//				}
//			}
//			
//			// Step 2: purge cache
//			
//			CacheUtils.purgeFromCache(ScheduleJSON.class, id);
//			CacheUtils.purgeFromCache(SchedulePreviewJSON.class, id);
//
//			CacheUtils.purgeFromCache(Schedules.class, company.id);
//			CacheUtils.purgeFromCache(CompanySchedules.class, company.id);
//
//			log.info("Purged cached data for schedule " + id);
//			
//			
//			// Step 3: send notifications to displays
//						
//			if (!displayIdsToUpdate.isEmpty()) {
//
//				ChannelUtils.send(displayIdsToUpdate, ChannelSignal.CONTENT_UPDATE, true);
//			}
//
//		} else if (kind.equals(EntityKind.PRESENTATION)) {
//
//			Presentations.purge(company.id); //CacheUtils.purgeFromCache(Presentations.class, company.id);
//			Templates.purge(company.id);
//			log.info("Purged cached templates for company " + company.id);
//			
//			Query cq = new Query(EntityKind.COMPANY).setAncestor(company.key).setKeysOnly();
//			List<Entity> subcompanies = datastore.prepare(cq).asList(FetchOptions.Builder.withDefaults().chunkSize(500).prefetchSize(500));
//			for (Entity sc : subcompanies) {
//
//				String subId = sc.getKey().getName();
//				Templates.purge(subId);
//				log.info("Purged cached templates for subcompany " + subId);
//			}
//			
//			// find all presentations that embed this presentation
//			List<String> affectedPreIds = new ArrayList<String>();
//			affectedPreIds.add(id); // !! IMPORTANT to prevent recursion loop AND to refresh the presentation's JSON
//
//			getEmbeddingPresentationIds(datastore, id, affectedPreIds);
//			
//			for (String preId : affectedPreIds) {
//
//				CacheUtils.purgeFromCache(PresentationJSON.class, preId);	
//				log.info("Purged cached JSON for presentation " + preId);
//				
//				List<Schedule> schedules = getDependentSchedules(datastore, preId);
//				
//				for (Schedule sce : schedules) {
//
//					CacheUtils.purgeFromCache(ScheduleJSON.class, sce.id);
//					CacheUtils.purgeFromCache(SchedulePreviewJSON.class, sce.id);
//					log.info("Purged cached JSONs for schedule " + sce.id);
//
//					if (sce.distributeToAll) {
//
//						// schedule is default for the company => update all displays in the company
//
//						Displays displays = CacheUtils.get(Displays.class, sce.companyId);
//
//						for (Display d : displays.displays) {
//							if (!displayIdsToUpdate.contains(d.id) && d.companyId.equals(sce.companyId)) // only add displays of the actual company, NOT its sub-companies
//								displayIdsToUpdate.add(d.id);
//						}
//
//					} else if (sce.distribution != null) {
//
//						// update only displays that are explicitly assigned to this schedule
//
//						for (String did : sce.distribution) {
//							if (!displayIdsToUpdate.contains(did))
//								displayIdsToUpdate.add(did);
//						}
//					}
//				}
//			}
//
//			/// Channel notification
//			
//			if (!displayIdsToUpdate.isEmpty()) {
//
//				ChannelUtils.send(displayIdsToUpdate, ChannelSignal.CONTENT_UPDATE, true);
//			}
//
//		} else if (kind.equals(EntityKind.PRESENTATION + Globals.PREVIEW_SUFFIX)) {
//			
//			Presentations.purge(company.id); //CacheUtils.purgeFromCache(Presentations.class, company.id);
//			Templates.purge(company.id);
//			log.info("Purged cached templates for company " + company.id);
//			
//			Query cq = new Query(EntityKind.COMPANY).setAncestor(company.key).setKeysOnly();
//			List<Entity> subcompanies = datastore.prepare(cq).asList(FetchOptions.Builder.withDefaults().chunkSize(500).prefetchSize(500));
//			for (Entity sc : subcompanies) {
//
//				String subId = sc.getKey().getName();
//				Templates.purge(subId);
//				log.info("Purged cached templates for subcompany " + subId);
//			}
//			
//			// find all presentations that embed this presentation
//			List<String> affectedPreIds = new ArrayList<String>();
//			affectedPreIds.add(id); // !! IMPORTANT to prevent recursion loop AND to refresh the presentation's JSON
//
//			getEmbeddingPresentationIds(datastore, id, affectedPreIds);
//			
//			for (String preId : affectedPreIds) {
//
//				CacheUtils.purgeFromCache(PresentationJSON.class, preId);	
//				log.info("Purged cached data for presentation " + preId);
//				
//				List<Schedule> schedules = getDependentSchedules(datastore, preId);
//				
//				// find which schedules use this presentation
//				for (Schedule sce : schedules) {
//
//					CacheUtils.purgeFromCache(SchedulePreviewJSON.class, sce.id);
//					log.info("Purged cached JSON (preview) for schedule " + sce.id);
//				}
//			}
//			
//		} else if (kind.equals(EntityKind.DISPLAY)) {
//						
//			CacheUtils.purgeFromCache(Displays.class, company.id);
//			log.info("Purged cached display list for company " + company.id);
//			
//			Key parentKey = company.key != null ? company.key.getParent() : null;
//			
//			while (parentKey != null) {
//				
//				String parentId = Utils.getCompanyId(parentKey);
//				
//				CacheUtils.purgeFromCache(Displays.class, parentId);
//				log.info("Purged cached display list for company " + parentId);
//				
//				parentKey = parentKey.getParent();
//			}
//			
//			CacheUtils.purgeFromCache(DisplayJSON.class, id);
//			log.info("Purged cached JSON for display " + id);
//			
//			ChannelUtils.send(id, ChannelSignal.CONTENT_UPDATE, true);
//			log.info("Notified the display via channel.");
//			
//			CacheUtils.purgeFromCache(DisplayInfo.class, id);
//			log.info("Purged cached info for display " + id);
//			
//		} else if (kind.equals(EntityKind.PLAYER)) {
//			
//			ChannelUtils.send(id, ChannelSignal.CONTENT_UPDATE, true);
//			log.info("Notified the display via channel.");
//			
//		} else if (kind.equals(EntityKind.DEMO)) {
//		
//			CacheUtils.purgeFromCache(Demos.class, company.id);
//			CacheUtils.purgeFromCache(CompanyDemoContent.class, company.id);
//
//			Query cq = new Query(EntityKind.COMPANY).setAncestor(company.key).setKeysOnly();
//			List<Entity> subcompanies = datastore.prepare(cq).asList(FetchOptions.Builder.withDefaults().chunkSize(500).prefetchSize(500));
//			for (Entity sc : subcompanies) {
//
//				String cid = sc.getKey().getName();
//
//				CompanyDemoContent demoContent = CacheUtils.loadFromCache(CompanyDemoContent.class, cid);
//				if (demoContent != null && demoContent.sourceCompanyId.equals(company.id)) {
//
//					// only purge CompanyDemoContent for subcompanies that use this company's Demos
//					CacheUtils.purgeFromCache(CompanyDemoContent.class, cid);
//				}
//			}
//			
//		} else if (kind.equals(EntityKind.USER)) {
//			
//			CacheUtils.purgeFromCache(Users.class, company.id);
//			
//			log.info("Purged cached users for company " + company.id);
//			
//		} else if (kind.equals(EntityKind.GADGET)) {
//			
//			CacheUtils.purgeFromCache(Gadgets.class, company.id);
//			log.info("Purged cached gadgets for company " + company.id);
//			
//			Query cq = new Query(EntityKind.COMPANY).setAncestor(company.key).setKeysOnly();
//			List<Entity> subcompanies = datastore.prepare(cq).asList(FetchOptions.Builder.withDefaults().chunkSize(500).prefetchSize(500));
//			for (Entity sc : subcompanies) {
//
//				String subId = sc.getKey().getName();
//				CacheUtils.purgeFromCache(SharedGadgets.class, subId);
//				log.info("Purged cached shared gadgets for subcompany " + subId);
//			}
//			
//		} else 
		if (kind.equals(EntityKind.COMPANY)) {
			
			Displays displays = CacheUtils.get(Displays.class, company.id);
			
			for (Display d : displays.displays) {
				if (!displayIdsToUpdate.contains(d.id) && d.companyId.equals(company.id) && d.useCompanyAddress) // only add displays of the actual company, NOT its sub-companies and that use company's address
					displayIdsToUpdate.add(d.id);
			}
			
			CacheUtils.purgeFromCache(Displays.class, company.id);
			log.info("Purged cached display list for company " + company.id);
			
			CacheUtils.purgeFromCache(Schedules.class, company.id);
			log.info("Purged cached schedule list for company " + company.id);
			
			CacheUtils.purgeFromCache(SharedGadgets.class, company.id);
			log.info("Purged cached shared gadget list for company " + company.id);
			
			CacheUtils.purgeFromCache(Templates.class, company.id);
			log.info("Purged cached template list for company " + company.id);
			
			Key parentKey = company.key != null ? company.key.getParent() : null;
			
			while (parentKey != null) {
				
				String parentId = Utils.getCompanyId(parentKey);
				
				CacheUtils.purgeFromCache(Companies.class, parentId);
				log.info("Purged cached subcompany list for company " + parentId);
				
				CacheUtils.purgeFromCache(Displays.class, parentId);
				log.info("Purged cached display list for company " + parentId);
								
				CacheUtils.purgeFromCache(NetworkOperators.class, parentId);
				log.info("Purged PNO list for company " + parentId);
				
				parentKey = parentKey.getParent();
			}
			
			for (String did : displayIdsToUpdate) {
				
				CacheUtils.purgeFromCache(DisplayJSON.class, did);
				log.info("Purged cached JSON for display " + did);
			}
									
		} else if (kind.equals(EntityKind.COMPANY + Globals.PREVIOUS_PARENT_SUFFIX)) {
			
			Company prevParent = Company.get(id); // ID is the previous parent's ID			

			if (prevParent != null) {
			
				Key parentKey = prevParent.key;

				while (parentKey != null) {

					String parentId = Utils.getCompanyId(parentKey);

					CacheUtils.purgeFromCache(Companies.class, parentId);
					log.info("Purged cached subcompany list for company " + parentId);
					
					CacheUtils.purgeFromCache(Displays.class, parentId);
					log.info("Purged cached display list for company " + parentId);

					CacheUtils.purgeFromCache(NetworkOperators.class, parentId);
					log.info("Purged PNO list for company " + parentId);

					parentKey = parentKey.getParent();
				}
			}
//		} else if (kind.equals(EntityKind.ACCESS_TOKEN)) {
//			
//			CacheUtils.purgeFromCache(AccessTokens.class, id);
//			log.info("Purged cached display social connectors for display " + id);
//			
//			process(company, EntityKind.SOCIAL_CONNECTOR_COMPANY, id);
//			
//		} else if (kind.equals(EntityKind.SOCIAL_CONNECTOR_DISPLAY)) {
//			
//			CacheUtils.purgeFromCache(DisplaySocialJSON.class, id);
//			log.info("Purged cached display social connectors for display " + id);
//			
//		} else if (kind.equals(EntityKind.SOCIAL_CONNECTOR_COMPANY)) {
//			
//			CacheUtils.purgeFromCache(CompanySocialJSON.class, id);
//			log.info("Purged cached company social connectors for company " + id);
//			
//			// purge all display social connector JSONs
//			Displays displays = CacheUtils.get(Displays.class, company.id);
//			
//			for (Display d : displays.displays) {
//				
//				if (d.companyId.equals(company.id)) { // only purge displays of the actual company, NOT its sub-companies
//				
//					CacheUtils.purgeFromCache(DisplaySocialJSON.class, d.id);
//					log.info("Purged cached display social connectors for display " + d.id);
//				
//				}
//			}
//		} else if (kind.equals(EntityKind.ALERT)) {
//			
//			CacheUtils.purgeFromCache(Alerts.class, id);
//			log.info("Purged cached alerts for company " + id);
			
		}
	*/	
	}
	


}
