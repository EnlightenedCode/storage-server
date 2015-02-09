package com.risevision.storage.api.accessors;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.users.User;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.datastore.ObjectifyTest;
import com.risevision.storage.entities.RvStorageObject;
import com.risevision.storage.entities.Tag;

public class RvStorageObjectAccessorTest extends ObjectifyTest {
  private TagDefinitionAccessor tagDefinitionAccessor;
  private RvStorageObjectAccessor rvsoAccessor;

  private String companyId;
  private String companyId2;
  private String filename;
  private Tag levis, gap, armani, hugo, urban, business, casual, address, comment, value1;
  private List<Tag> values;
  private String timeline;
  private User user;

  @Before
  public void setUp() {
    super.setUp();
    
    companyId = "72dab2d1-e2e5-4598-acdf-20bd28b7fbf5";
    companyId2 = "4598acdf-e2e5-72da-b2d1-20bd28b7fbf5";
    
    filename = "unnamed.png";
    levis = new Tag(TagType.LOOKUP.toString(), "brand", "levis");
    gap = new Tag(TagType.LOOKUP.toString(), "brand", "gap");
    armani = new Tag(TagType.LOOKUP.toString(), "brand", "armani");
    hugo = new Tag(TagType.LOOKUP.toString(), "brand", "hugo");
    urban = new Tag(TagType.LOOKUP.toString(), "style", "urban");
    business = new Tag(TagType.LOOKUP.toString(), "style", "business");
    casual = new Tag(TagType.LOOKUP.toString(), "style", "casual");
    value1 = new Tag(TagType.LOOKUP.toString(), "test", "value1");
    address = new Tag(TagType.FREEFORM.toString(), "address", "test address");
    comment = new Tag(TagType.FREEFORM.toString(), "comment", "test comment");
    values = getList(levis, gap, armani, address, comment);
    timeline = null;
    
    user = new User("test@gmail.com","example.com");

    tagDefinitionAccessor = new TagDefinitionAccessor();
    rvsoAccessor = new RvStorageObjectAccessor();
    
    try {
      tagDefinitionAccessor.put(companyId, TagType.LOOKUP.toString(), "test", getList("value1", "value2"), user);
      tagDefinitionAccessor.put(companyId, TagType.LOOKUP.toString(), "brand", getList("levis", "gap", "armani", "hugo"), user);
      tagDefinitionAccessor.put(companyId, TagType.LOOKUP.toString(), "style", getList("urban", "casual", "business"), user);
      tagDefinitionAccessor.put(companyId, TagType.FREEFORM.toString(), "address", null, user);
      tagDefinitionAccessor.put(companyId, TagType.FREEFORM.toString(), "comment", null, user);
      tagDefinitionAccessor.put(companyId2, TagType.LOOKUP.toString(), "test", getList("value1", "value2", "value3"), user);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void tearDown() throws IOException {
    super.tearDown();
  }

  @Test
  public void itShouldAddATaggedStorageObject() throws Exception {
    RvStorageObject response = rvsoAccessor.put(companyId, filename, values, timeline, user);

    assertThat(response, is(instanceOf(RvStorageObject.class)));
    assertThat(response.getId(), is(notNullValue()));
    assertThat(response.getId(), is(instanceOf(String.class)));
    assertThat(response.getCreatedBy(), is(notNullValue()));
    assertThat(response.getCreationDate(), is(notNullValue()));
    assertThat(response.getChangedBy(), is(notNullValue()));
    assertThat(response.getChangedDate(), is(notNullValue()));
    assertThat(response.getLookupNames(), hasItem("brand"));
    assertThat(response.getLookupTags(), hasItem("brand|levis"));
    assertThat(response.getFreeformNames(), hasItem("comment"));
    assertThat(response.getFreeformTags(), hasItem("comment|test comment"));
    assertThat(response.getAutoTrashDate(), is((Date) null));
  }

  @Test
  public void itShouldAddATaggedStorageObjectWithTimeline() throws Exception {
    timeline = getTimeline(true);
    RvStorageObject response = rvsoAccessor.put(companyId, filename, values, timeline, user);
    
    assertThat(response.getAutoTrashDate(), is(notNullValue()));
  }

  @Test
  public void itShouldAddATaggedStorageObjectWithLowerCaseNameAndValues() throws Exception {
    levis = new Tag(TagType.LOOKUP.toString(), "BRAND", "LEVIS");
    values = getList(levis);
    
    RvStorageObject response = rvsoAccessor.put(companyId, filename, values, timeline, user);

    // check if it saves in lower case
    assertThat(response.getLookupNames(), hasItem("brand"));
    assertThat(response.getLookupTags(), hasItem("brand|levis"));
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATaggedStorageObjectWithoutCompanyId() throws Exception {
    @SuppressWarnings("unused")
    RvStorageObject response = rvsoAccessor.put("", filename, values, timeline, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATaggedStorageObjectWithoutObjectId() throws Exception {
    @SuppressWarnings("unused")
    RvStorageObject response = rvsoAccessor.put(companyId, "", values, timeline, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATaggedStorageObjectWithoutName() throws Exception {
    levis = new Tag(TagType.LOOKUP.toString(), "", "levis");
    values = getList(levis);
    
    rvsoAccessor.put(companyId, filename, values, timeline, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATaggedStorageObjectWithoutType() throws Exception {
    levis = new Tag("", "brand", "levis");
    values = getList(levis);
    
    rvsoAccessor.put(companyId, filename, values, timeline, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATaggedStorageObjectWithInvalidType() throws Exception {
    levis = new Tag("invalidType", "brand", "levis");
    values = getList(levis);
    
    rvsoAccessor.put(companyId, filename, values, timeline, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATaggedStorageObjectWithTimelineType() throws Exception {
    levis = new Tag(TagType.TIMELINE.toString(), "brand", "levis");
    values = getList(levis);
    
    rvsoAccessor.put(companyId, filename, values, timeline, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATaggedStorageObjectIfValueDoesNotExistInParent() throws Exception {
    levis = new Tag(TagType.LOOKUP.toString(), "color", "red");
    values = getList(levis);
    
    rvsoAccessor.put(companyId, filename, values, timeline, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFreeformTaggedStorageObjectWithNoValue() throws Exception {
    levis = new Tag(TagType.LOOKUP.toString(), "brand", "");
    values = getList(levis);
    
    rvsoAccessor.put(companyId, filename, values, timeline, user);
  }
  
  @Test
  public void itShouldRetrieveATaggedStorageObjectById() throws Exception {
    RvStorageObject responseFromAdd = rvsoAccessor.put(companyId, filename, values, timeline, user);

    String id = responseFromAdd.getId();
    
    assertThat(responseFromAdd, is(notNullValue()));
    
    RvStorageObject responseFromGet = rvsoAccessor.get(id);
    
    assertThat(responseFromGet, is(notNullValue()));
    assertThat(responseFromGet.getId(), is(id));
  }

  @Test
  public void itShouldDeleteATaggedStorageObjectById() throws Exception {
    RvStorageObject responseFromAdd = rvsoAccessor.put(companyId, filename, values, timeline, user);

    String id = responseFromAdd.getId();
    
    rvsoAccessor.delete(id);
    
    RvStorageObject responseFromGet = rvsoAccessor.get(id);

    assertThat((RvStorageObject) responseFromGet, is((RvStorageObject) null));
  }

  @Test
  public void itShouldFindTwoElementsByCompanyId() throws Exception {
    rvsoAccessor.put(companyId, "file1", values, timeline, user);
    rvsoAccessor.put(companyId, "file2", values, timeline, user);
    rvsoAccessor.put(companyId2, "file3", getList(new Tag(TagType.LOOKUP.toString(), "test", "value1")), timeline, user);
    
    PagedResult<RvStorageObject> responseFromList = rvsoAccessor.list(companyId, null, 100, null, null);
    
    assertThat(responseFromList.getList().size(), is(2));
  }

  @Test
  public void itShouldFindTwoElementsBySearchFilter() throws Exception {
    rvsoAccessor.put(companyId, "file1", values, timeline, user);
    rvsoAccessor.put(companyId, "file2", values, timeline, user);
    rvsoAccessor.put(companyId2, "file3", getList(new Tag(TagType.LOOKUP.toString(), "test", "value1")), timeline, user);
    
    PagedResult<RvStorageObject> responseFromList = rvsoAccessor.list(companyId, "objectId: file2", 100, null, null);
    
    assertThat(responseFromList.getList().size(), is(1));
  }

  public void itShouldFindOneFileByTagSearch() throws Exception {
    List<RvStorageObject> responseFromList;
    
    rvsoAccessor.put(companyId, "file1", getList(levis), timeline, user);
    rvsoAccessor.put(companyId, "file2", getList(gap, hugo, urban), timeline, user);
    
    // Should only return file1
    responseFromList = rvsoAccessor.listFilesByTags(companyId, getList(levis));
    
    assertThat(responseFromList.size(), is(1));
    
    // Should only return file2
    responseFromList = rvsoAccessor.listFilesByTags(companyId, getList(urban));
    
    assertThat(responseFromList.size(), is(1));
  }

  @Test
  public void itShouldFindFilesByTagSearch() throws Exception {
    List<RvStorageObject> responseFromList;
    Tag address2 = new Tag(TagType.FREEFORM.toString(), "address", "random");
    
    rvsoAccessor.put(companyId, "file1", getList(armani, business), timeline, user);
    rvsoAccessor.put(companyId, "file2", getList(gap, hugo, urban, address), timeline, user);
    rvsoAccessor.put(companyId, "file3", getList(hugo, levis, casual, value1, address2), timeline, user);
    
    // Should return file1, file2 and file3
    responseFromList = rvsoAccessor.listFilesByTags(companyId, getList(new Tag(TagType.LOOKUP.toString(), "brand", null)));
    
    assertThat(responseFromList.size(), is(3));
    
    // Should return file2 and file3
    responseFromList = rvsoAccessor.listFilesByTags(companyId, getList(new Tag(TagType.FREEFORM.toString(), "address", null)));
    
    assertThat(responseFromList.size(), is(2));
    assertThat(responseFromList.get(0).getObjectId(), anyOf(is("file2"), is("file3")));
    assertThat(responseFromList.get(1).getObjectId(), anyOf(is("file2"), is("file3")));
    
    // Should return file3
    responseFromList = rvsoAccessor.listFilesByTags(companyId, getList(new Tag(TagType.LOOKUP.toString(), "test", null)));
    
    assertThat(responseFromList.size(), is(1));
    assertThat(responseFromList.get(0).getObjectId(), is("file3"));
    
    // Should return file1 and file3
    responseFromList = rvsoAccessor.listFilesByTags(companyId, getList(armani, casual));
    
    assertThat(responseFromList.size(), is(2));
    
    // Should return file2
    responseFromList = rvsoAccessor.listFilesByTags(companyId, getList(address));
    
    assertThat(responseFromList.size(), is(1));
    assertThat(responseFromList.get(0).getObjectId(), is("file2"));
    
    // Should only return file3
    responseFromList = rvsoAccessor.listFilesByTags(companyId, getList(levis, casual));
    
    assertThat(responseFromList.get(0).getObjectId(), is("file3"));
  }
  
  @SuppressWarnings("unchecked")
  public <T> List<T> getList(T... values) {
    return Arrays.asList(values);
  }
  
  protected String getTimeline(boolean trash) {
    return "{\"timeDefined\":true,\"duration\":60,\"pud\":\"false\","
          + "\"trash\":\"" + trash + "\",\"carryon\":\"false\","
          + "\"startDate\":\"02/02/15 12:00 AM\",\"endDate\":\"02/03/15 12:00 AM\","
          + "\"startTime\":null,\"endTime\":null,\"recurrenceOptions\":null}";
  }
}
