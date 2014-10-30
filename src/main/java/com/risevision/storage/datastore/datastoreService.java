package com.risevision.storage.datastore;

import com.risevision.storage.entities.ShareFolder;
import com.risevision.storage.entities.ShareFolderLink;
import com.risevision.storage.entities.SharePermission;
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

    public void addShareFolder(String companyId, String sharedCompanyId, String folder, boolean view, boolean edit, boolean add) throws ServiceFailedException {
        SharePermission permToAdd = new SharePermission(sharedCompanyId,view,edit,add);
        //check to see if there is a folderName already created under that originCompanyId
        ShareFolder shrCheck = ofy().load().type(ShareFolder.class).filter("originCompanyId", companyId).filter("folderName", folder).first().now();
        if(shrCheck != null) {
            //if there is an entry, check to see if the permission entry is identical...this compares sharedCompanyId to see if the same shared company is already present.
            if(shrCheck.permissions.contains(permToAdd)){
                // if it is present update / edit the permissions
                SharePermission tmp = shrCheck.permissions.get(shrCheck.permissions.indexOf(permToAdd));
                tmp.view = view;
                tmp.edit = edit;
                tmp.add = add;
                shrCheck.permissions.set(shrCheck.permissions.indexOf(permToAdd), tmp);
                ofy().save().entity(shrCheck).now();
            } else {
                //if not present add a new SharePermission object
                shrCheck.permissions.add(permToAdd);
                ofy().save().entity(shrCheck).now();
            }
        } else {
            //if entry for Sharefolder is not there at all add it with a new permission object inside it's permissions list.
            List<SharePermission> accessList = new ArrayList<SharePermission>();
            accessList.add(permToAdd);
            ShareFolder shrFold = new ShareFolder(companyId, folder, accessList);
            ofy().save().entity(shrFold).now();
            assert shrFold.id != null;
        }
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

    public List<ShareFolder> getSharedFolders(String companyId) throws ServiceFailedException {
        return ofy().load().type(ShareFolder.class).filter("originCompanyId", companyId).list();
    }
}
