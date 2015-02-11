package com.risevision.storage.datastore;

/**
 * Created by rodrigopavezi on 12/9/14.
 */

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class DatastoreServiceTest extends ObjectifyTest {

    private DatastoreService datastoreService;


    @Entity
    public static class SampleEntity {

        @Id Long id;
        @Index String sampleAttribute;

        public SampleEntity(){

        }

        public SampleEntity(String sampleValue){
            this.sampleAttribute = sampleValue;
        }

        public SampleEntity(Long id){
            this.id = id;
        }

        public SampleEntity(Long id, String sampleValue){
            this.id = id;
            this.sampleAttribute = sampleValue;
        }
    }

    @Before
    public void setUp() {
        super.setUp();
        OfyService.factory().register(SampleEntity.class);
        datastoreService = DatastoreService.getInstance();
    }

    @After
    public void tearDown() throws IOException {
        super.tearDown();
    }

    @Test
    public void itShouldAddAnEntity(){

        SampleEntity sampleEntity = new SampleEntity("sampleValue");
        datastoreService.put(sampleEntity);

        assertThat(sampleEntity.id, is(notNullValue()));
    }

    @Test
    public void itShouldGetAnEntity(){

        SampleEntity sampleEntity = new SampleEntity("sampleValue");
        datastoreService.put(sampleEntity);

        SampleEntity entity = new SampleEntity(sampleEntity.id);

        SampleEntity entityReturned = (SampleEntity) datastoreService.get(entity);

        assertThat(entityReturned.id, is(entity.id));
        assertThat(entityReturned.sampleAttribute, is(sampleEntity.sampleAttribute));
    }

    @Test
    public void itShouldDeleteAnEntity() {
        SampleEntity sampleEntity = new SampleEntity("sampleValue");
        datastoreService.put(sampleEntity);

        SampleEntity entity = new SampleEntity(sampleEntity.id);

        datastoreService.delete(entity);
        
        assertThat((SampleEntity) datastoreService.get(entity), is((SampleEntity) null));
    }

}
