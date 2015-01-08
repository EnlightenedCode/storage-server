package com.risevision.storage.api.accessors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.users.User;
import com.risevision.storage.Utils;
import com.risevision.storage.api.UserCompanyVerifier;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.datastore.ObjectifyTest;
import com.risevision.storage.entities.AutoTrashTag;
import com.risevision.storage.entities.FileTagEntry;
import com.risevision.storage.entities.StorageEntity;

/**
 * Created by rodrigopavezi on 12/9/14.
 */
public class FileTagEntryAccessorTest extends ObjectifyTest {
  private TagDefinitionAccessor tagDefinitionAccessor;
  private FileTagEntryAccessor fileTagEntryAccessor;
  private AutoTrashTagAccessor autoTrashTagAccessor;

  private String companyId;
  private String companyId2;
  private String filename;
  private String name;
  private String type;
  private List<String> values;
  private User user;

  UserCompanyVerifier userCompanyVerifier;

  @Before
  public void setUp() {
    super.setUp();
    companyId = "72dab2d1-e2e5-4598-acdf-20bd28b7fbf5";
    companyId2 = "4598acdf-e2e5-72da-b2d1-20bd28b7fbf5";
    
    filename = "unnamed.png";
    name = "test";
    type =  "LOOKUP";
    values = new LinkedList<String>();
    values.add("value1");
    user = new User("test@gmail.com","example.com");

    userCompanyVerifier = mock(UserCompanyVerifier.class);

    tagDefinitionAccessor = new TagDefinitionAccessor();
    fileTagEntryAccessor = new FileTagEntryAccessor();
    autoTrashTagAccessor = new AutoTrashTagAccessor();
    
    try {
      tagDefinitionAccessor.put(companyId, type, name, getList("value1", "value2"), user);
      tagDefinitionAccessor.put(companyId, type, "brand", getList("levis", "gap", "armani", "hugo"), user);
      tagDefinitionAccessor.put(companyId, type, "style", getList("urban", "casual", "business"), user);
      tagDefinitionAccessor.put(companyId2, type, name, getList("value1", "value2", "value3"), user);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void tearDown() throws IOException {
    super.tearDown();
  }


  @Test
  public void itShouldAddAFileTagEntry() throws Exception {
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, type, name, values, user);

    assertThat(response, is(instanceOf(FileTagEntry.class)));
  }

  @Test
  public void itShouldAddAFileTagEntryWithId() throws Exception {
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, type, name, values, user);

    assertThat(response.getId(), is(notNullValue()));
    assertThat(response.getId(), is(instanceOf(String.class)));
  }

  @Test
  public void itShouldAddAFileTagEntryWithStatusParameters() throws Exception {
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, type, name, values, user);

    assertThat(response.getId(), is(notNullValue()));
    assertThat(response.getCreatedBy(), is(notNullValue()));
    assertThat(response.getCreationDate(), is(notNullValue()));
    assertThat(response.getChangedBy(), is(notNullValue()));
    assertThat(response.getChangedDate(), is(notNullValue()));
  }

  @Test
  public void itShouldAddAFileTagEntryWithLowerCaseNameAndValues() throws Exception {
    name = "TEST";
    values.clear();
    values.add("VALUE1");
    values.add("Value2");
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, type, name, values, user);

    // check if it saves in lower case
    assertThat(response.getName(), is("test"));
    assertThat(response.getValues(), hasItem("value1"));
    assertThat(response.getValues(), hasItem("value2"));
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFileTagEntryWithoutCompanyId() throws Exception {
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put("", filename, type, name, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFileTagEntryWithoutObjectId() throws Exception {
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, "", type, name, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFileTagEntryWithoutName() throws Exception {
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, type, "", values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFileTagEntryWithoutType() throws Exception {
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, "", name, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFileTagEntryIfValueDoesNotExistInParent() throws Exception {
    values.clear();
    values.add("value5");
    
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, type, name, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFreeformFileTagEntryWithNoValues() throws Exception {
    values.clear();
    
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, "Freeform", name, values, user);
  }

  @Test(expected = Exception.class)
  public void itShouldNotAddAFreeformFileTagEntryWithMoreThanOneValue() throws Exception {
    values.clear();
    values.add("value1");
    values.add("value2");
    
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, "Freeform", name, values, user);
  }
  
  @Test
  public void itShouldRetrieveAFileTagEntryById() throws Exception {
    FileTagEntry responseFromAdd = fileTagEntryAccessor.put(companyId, filename, type, name, values, user);

    String id = responseFromAdd.getId();

    FileTagEntry responseFromGet = fileTagEntryAccessor.get(id);

    assertThat(responseFromGet.getId(), is(id));
  }

  @Test
  public void itShouldDeleteAFileTagEntryById() throws Exception {
    FileTagEntry responseFromAdd = fileTagEntryAccessor.put(companyId, filename, type, name, values, user);

    String id = responseFromAdd.getId();
    
    fileTagEntryAccessor.delete(id);
    
    FileTagEntry responseFromGet = fileTagEntryAccessor.get(id);

    assertThat((FileTagEntry) responseFromGet, is((FileTagEntry) null));
  }

  @Test
  public void itShouldDeleteATimelineEntryById() throws Exception {
    String timeline = "{\"type\":\"TIMELINE\","
        + "\"timeDefined\":true,\"duration\":60,\"pud\":\"false\","
        + "\"trash\":\"false\",\"carryon\":\"false\","
        + "\"startDate\":\"02/02/15 12:00 AM\",\"endDate\":\"02/03/15 12:00 AM\","
        + "\"startTime\":null,\"endTime\":null,\"recurrenceOptions\":null}"; 
    FileTagEntry responseFromAdd = fileTagEntryAccessor.put(companyId, filename, "TIMELINE", name, getList(timeline), user);

    String id = responseFromAdd.getId();
    
    // Checks not AutoTrashTag was created
    AutoTrashTag autoTrashTagFromAdd = autoTrashTagAccessor.get(new AutoTrashTag(companyId, filename, null, null).getId());
    
    assertThat(autoTrashTagFromAdd, is((AutoTrashTag) null));
    
    fileTagEntryAccessor.delete(id);
    
    FileTagEntry responseFromGet = fileTagEntryAccessor.get(id);

    assertThat((FileTagEntry) responseFromGet, is((FileTagEntry) null));
  }

  @Test
  public void itShouldDeleteATrashTimelineEntryById() throws Exception {
    String timeline = "{\"type\":\"TIMELINE\","
        + "\"timeDefined\":true,\"duration\":60,\"pud\":\"false\","
        + "\"trash\":\"true\",\"carryon\":\"false\","
        + "\"startDate\":\"02/02/15 12:00 AM\",\"endDate\":\"02/03/15 12:00 AM\","
        + "\"startTime\":null,\"endTime\":null,\"recurrenceOptions\":null}"; 
    FileTagEntry responseFromAdd = fileTagEntryAccessor.put(companyId, filename, "TIMELINE", name, getList(timeline), user);

    String id = responseFromAdd.getId();
    
    // Checks AutoTrashTag was created
    AutoTrashTag autoTrashTagFromAdd = autoTrashTagAccessor.get(new AutoTrashTag(companyId, filename, null, null).getId());
    
    assertThat(autoTrashTagFromAdd, not(is((AutoTrashTag) null)));
    
    fileTagEntryAccessor.delete(id);
    
    FileTagEntry responseFromGet = fileTagEntryAccessor.get(id);

    assertThat((FileTagEntry) responseFromGet, is((FileTagEntry) null));
    
    AutoTrashTag autoTrashTagAfterDelete = autoTrashTagAccessor.get(new AutoTrashTag(companyId, filename, null, null).getId());
    
    assertThat(autoTrashTagAfterDelete, is((AutoTrashTag) null));
  }

  @Test
  public void itShouldFindTwoElementsByCompanyId() throws Exception {
    fileTagEntryAccessor.put(companyId, "file1", type, "test", getList("value1"), user);
    fileTagEntryAccessor.put(companyId, "file2", type, "test", getList("value1"), user);
    fileTagEntryAccessor.put(companyId2, "file3", type, "test", getList("value1"), user);
    
    PagedResult<FileTagEntry> responseFromList = fileTagEntryAccessor.list(companyId, null, 100, null, null);
    
    assertThat(responseFromList.getList().size(), is(2));
  }

  @Test
  public void itShouldFindTwoElementsBySearchFilter() throws Exception {
    String companyId2 = "4598acdf-e2e5-72da-b2d1-20bd28b7fbf5";
    
    fileTagEntryAccessor.put(companyId, "file1", type, "test", getList("value1"), user);
    fileTagEntryAccessor.put(companyId, "file2", type, "test", getList("value1"), user);
    fileTagEntryAccessor.put(companyId2, "file3", type, "test", getList("value1"), user);
    
    PagedResult<FileTagEntry> responseFromList = fileTagEntryAccessor.list(companyId, "objectId: file2", 100, null, null);
    
    assertThat(responseFromList.getList().size(), is(1));
  }

  @Test
  public void itShouldRenameObjectIds() throws Exception {
    PagedResult<FileTagEntry> responseFromList = null;
    List<String> objs = getList("file1", "file2");
    
    tagDefinitionAccessor.put(companyId, type, "test2", getList("value1", "value2"), user);
    
    fileTagEntryAccessor.put(companyId, "file1", type, "test", getList("value1"), user);
    fileTagEntryAccessor.put(companyId, "file1", type, "test2", getList("value2"), user);
    fileTagEntryAccessor.put(companyId, "file2", type, "test", getList("value1"), user);
    fileTagEntryAccessor.put(companyId, "file3", type, "test2", getList("value1"), user);
    
    fileTagEntryAccessor.updateObjectId(companyId, objs, Utils.addPrefix(objs, "--TRASH--/"));
    
    responseFromList = fileTagEntryAccessor.list(companyId, "objectId: --TRASH--/file1", 100, null, null);
    assertThat(responseFromList.getList().size(), is(2));
    
    responseFromList = fileTagEntryAccessor.list(companyId, "objectId: file2", 100, null, null);
    assertThat(responseFromList.getList().size(), is(0));
    
    responseFromList = fileTagEntryAccessor.list(companyId, "objectId: file3", 100, null, null);
    assertThat(responseFromList.getList().size(), is(1));
  }

  public void itShouldFindOneFileByTagSearch() throws Exception {
    List<StorageEntity> responseFromList;
    
    fileTagEntryAccessor.put(companyId, "file1", type, "brand", getList("levis"), user);
    fileTagEntryAccessor.put(companyId, "file2", type, "brand", getList("gap", "hugo"), user);
    fileTagEntryAccessor.put(companyId, "file2", type, "style", getList("urban"), user);
    
    // Should only return file1
    responseFromList = fileTagEntryAccessor.listFilesByTags(companyId, getList("brand:levis"), false);
    
    assertThat(responseFromList.size(), is(1));
    
    // Should only return file2
    responseFromList = fileTagEntryAccessor.listFilesByTags(companyId, getList("style:urban"), true);
    
    assertThat(responseFromList.get(0).getTags().size(), is(2));
  }

  @Test
  public void itShouldFindFilesByTagSearch() throws Exception {
    List<StorageEntity> responseFromList;
    
    fileTagEntryAccessor.put(companyId, "file1", type, "brand", getList("armani"), user);
    fileTagEntryAccessor.put(companyId, "file1", type, "style", getList("business"), user);
    fileTagEntryAccessor.put(companyId, "file2", type, "brand", getList("gap", "hugo"), user);
    fileTagEntryAccessor.put(companyId, "file2", type, "style", getList("urban"), user);
    fileTagEntryAccessor.put(companyId, "file3", type, "brand", getList("hugo", "levis"), user);
    fileTagEntryAccessor.put(companyId, "file3", type, "style", getList("casual"), user);
    fileTagEntryAccessor.put(companyId, "file3", type, "test", getList("value1"), user);
    
    // Should return file1, file2 and file3
    responseFromList = fileTagEntryAccessor.listFilesByTags(companyId, getList("brand"), true);
    
    assertThat(responseFromList.size(), is(3));
    
    // Should return file3
    responseFromList = fileTagEntryAccessor.listFilesByTags(companyId, getList("test"), true);
    
    assertThat(responseFromList.size(), is(1));
    assertThat(responseFromList.get(0).getName(), is("file3"));
    
    // Should return file1 and file3
    responseFromList = fileTagEntryAccessor.listFilesByTags(companyId, getList("brand:armani", "style:casual"), false);
    
    assertThat(responseFromList.size(), is(2));
    
    // Should only return file3
    responseFromList = fileTagEntryAccessor.listFilesByTags(companyId, getList("brand:levis", "style:casual"), true);
    
    assertThat(responseFromList.get(0).getTags().size(), is(3));
  }
  
  @SuppressWarnings("unchecked")
  public <T> List<T> getList(T... values) {
    return Arrays.asList(values);
  }
}
