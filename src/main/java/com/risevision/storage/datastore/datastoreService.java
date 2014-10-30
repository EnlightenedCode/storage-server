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
        ShareFolder shrCheck = ofy().load().type(ShareFolder.class).filter("originCompanyId", companyId).filter("folderName", folder).first().now();
        if(shrCheck != null) {
            if(shrCheck.permissions.contains(permToAdd)){
                SharePermission tmp = shrCheck.permissions.get(shrCheck.permissions.indexOf(permToAdd));
                tmp.view = view;
                tmp.edit = edit;
                tmp.add = add;
                shrCheck.permissions.set(shrCheck.permissions.indexOf(permToAdd), tmp);
                ofy().save().entity(shrCheck).now();
            } else {
                shrCheck.permissions.add(permToAdd);
                ofy().save().entity(shrCheck).now();
            }
        } else {
            List<SharePermission> accessList = new ArrayList<SharePermission>();
            ShareFolder shrFold = new ShareFolder(companyId, folder, accessList);
            accessList.add(permToAdd);
            ofy().save().entity(shrFold).now();
            assert shrFold.id != null;
        }
    }

    public void addShareFolderLink(String companyId, String sharedCompanyId, String folder, boolean view, boolean edit, boolean add) throws ServiceFailedException{
        ShareFolderLink shared = new ShareFolderLink(companyId, sharedCompanyId, folder, view, edit, add);
        ShareFolderLink fetched2 = ofy().load().type(ShareFolderLink.class).filter("originCompanyId", companyId).filter("folderName", folder).first().now();
        if(fetched2 == null) {
            ofy().save().entity(shared).now();
            assert shared.id != null;
        } else {
            ofy().delete().entity(fetched2).now();
            ofy().save().entity(shared).now();
            assert shared.id != null;
        }
    }

    public List<ShareFolder> getSharedFolders(String companyId) throws ServiceFailedException {
        return ofy().load().type(ShareFolder.class).filter("originCompanyId", companyId).list();
    }
}
