package com.risevision.storage.datastore;

import com.risevision.storage.entities.ShareFolderLink;
import com.risevision.storage.info.ServiceFailedException;

import java.util.ArrayList;
import java.util.List;

import static com.risevision.storage.datastore.OfyService.ofy;
/**
 * Created by Andrew on 10/30/2014.
 */
public class datastoreService {

    private static datastoreService instance;
    private datastoreService() {}

    public static datastoreService getInstance() {
        try {
            if (instance == null) {
                instance = new datastoreService();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public void addShareFolderLink(String companyId, String sharedCompanyId, String folder, boolean view, boolean edit, boolean add) throws ServiceFailedException{
        ShareFolderLink shared = new ShareFolderLink(companyId, sharedCompanyId, folder, view, edit, add);
        //check if the sharedLink is already there with teh same originCompanyId and folderName.
        ShareFolderLink sharedLinkCheck = ofy().load().type(ShareFolderLink.class).filter("originCompanyId", companyId).filter("folderName", folder).first().now();
        if(sharedLinkCheck == null) {
            // if not there, then add one.
            ofy().save().entity(shared).now();
            assert shared.id != null;
        } else {
            //if it is there, delete the old entry and add the new entry. this handles updating / editing the permissions as well.
            ofy().delete().entity(sharedLinkCheck).now();
            ofy().save().entity(shared).now();
            assert shared.id != null;
        }
    }

    public List<ShareFolderLink> getSharedFolders(String companyId) throws ServiceFailedException {
        return ofy().load().type(ShareFolderLink.class).filter("originCompanyId", companyId).list();
    }
}
