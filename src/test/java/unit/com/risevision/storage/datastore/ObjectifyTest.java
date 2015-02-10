package com.risevision.storage.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.junit.After;
import org.junit.Before;

import java.io.Closeable;
import java.io.IOException;

public class ObjectifyTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    Closeable session;

    @Before
    public void setUp() {
        helper.setUp();
        session = OfyService.begin();
    }

    @After
    public void tearDown() throws IOException {
        session.close();
        helper.tearDown();
    }
}
