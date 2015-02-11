package com.risevision.storage.datastore;

import com.googlecode.objectify.*;
import com.googlecode.objectify.util.Closeable;
import com.risevision.storage.entities.*;

public class OfyService {
    static {
        factory().register(DatastoreEntity.class);
        factory().register(TagDefinition.class);
        factory().register(FileTagEntry.class);
        factory().register(AutoTrashTag.class);
        factory().register(ThrottleBaseline.class);
        factory().register(RvStorageObject.class);
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
