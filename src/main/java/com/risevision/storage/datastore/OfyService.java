package com.risevision.storage.datastore;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import com.risevision.storage.entities.AutoTrashTag;
import com.risevision.storage.entities.DatastoreEntity;
import com.risevision.storage.entities.FileTagEntry;
import com.risevision.storage.entities.TagDefinition;

public class OfyService {
    static {
        factory().register(DatastoreEntity.class);
        factory().register(TagDefinition.class);
        factory().register(FileTagEntry.class);
        factory().register(AutoTrashTag.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

    public static Closeable begin(){
        return ObjectifyService.begin();
    }
}