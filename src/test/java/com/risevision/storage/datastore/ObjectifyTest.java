package com.risevision.storage.datastore;

import java.io.Closeable;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * Created by rodrigopavezi on 12/10/14.
 */
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
