package com.risevision.storage.datastore;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.risevision.storage.entities.ShareFolder;
import com.risevision.storage.entities.ShareFolderLink;

/**
 * Created by Andrew on 10/29/2014.
 */
public class OfyService {
    static {
        factory().register(ShareFolderLink.class);
        factory().register(ShareFolder.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}