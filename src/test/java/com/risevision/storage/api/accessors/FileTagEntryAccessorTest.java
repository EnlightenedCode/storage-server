package com.risevision.storage.api.accessors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
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
import com.risevision.storage.entities.FileTagEntry;

/**
 * Created by rodrigopavezi on 12/9/14.
 */
public class FileTagEntryAccessorTest extends ObjectifyTest {
  private TagDefinitionAccessor tagDefinitionAccessor;
  private FileTagEntryAccessor fileTagEntryAccessor;

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
    
    try {
      tagDefinitionAccessor.put(companyId, type, name, getList("value1", "value2"), user);
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
  public void itShouldAddATagDefinition() throws Exception {
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, name, type, values, user);

    assertThat(response, is(instanceOf(FileTagEntry.class)));
  }

  @Test
  public void itShouldAddATagDefinitionWithAnUUID() throws Exception {
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, name, type, values, user);

    assertThat(response.getId(), is(notNullValue()));
    assertThat(response.getId(), is(instanceOf(String.class)));
  }

  @Test
  public void itShouldAddATagDefinitionWithStatusParameters() throws Exception {
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, name, type, values, user);

    assertThat(response.getId(), is(notNullValue()));
    assertThat(response.getCreatedBy(), is(notNullValue()));
    assertThat(response.getCreationDate(), is(notNullValue()));
    assertThat(response.getChangedBy(), is(notNullValue()));
    assertThat(response.getChangedDate(), is(notNullValue()));
  }

  @Test
  public void itShouldAddATagDefinitionWithLowerCaseNameAndValues() throws Exception {
    name = "TEST";
    values.clear();
    values.add("VALUE1");
    values.add("Value2");
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, name, type, values, user);

    // check if it saves in lower case
    assertThat(response.getName(), is("test"));
    assertThat(response.getValues(), hasItem("value1"));
    assertThat(response.getValues(), hasItem("value2"));
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATagDefinitionWithoutCompanyId() throws Exception {
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put("", filename, name, type, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATagDefinitionWithoutObjectId() throws Exception {
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, "", name, type, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATagDefinitionWithoutName() throws Exception {
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, "", type, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATagDefinitionWithoutType() throws Exception {
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, name, "", values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATagDefinitionIfValueDoesNotExistInParent() throws Exception {
    values.clear();
    values.add("value5");
    
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, name, type, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFreeformTagDefinitionWithNoValues() throws Exception {
    values.clear();
    
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, name, "Freeform", values, user);
  }

  @Test(expected = Exception.class)
  public void itShouldNotAddAFreeformTagDefinitionWithMoreThanOneValue() throws Exception {
    values.clear();
    values.add("value1");
    values.add("value2");
    
    @SuppressWarnings("unused")
    FileTagEntry response = fileTagEntryAccessor.put(companyId, filename, name, "Freeform", values, user);
  }
  
  @Test
  public void itShouldRetrieveATagDefinitionById() throws Exception {
    FileTagEntry responseFromAdd = fileTagEntryAccessor.put(companyId, filename, name, type, values, user);

    String id = responseFromAdd.getId();

    FileTagEntry responseFromGet = fileTagEntryAccessor.get(id);

    assertThat(responseFromGet.getId(), is(id));
  }

  @Test
  public void itShouldDeleteATagDefinitionById() throws Exception {
    FileTagEntry responseFromAdd = fileTagEntryAccessor.put(companyId, filename, name, type, values, user);

    String id = responseFromAdd.getId();
    
    fileTagEntryAccessor.delete(id);
    
    FileTagEntry responseFromGet = fileTagEntryAccessor.get(id);

    assertThat((FileTagEntry) responseFromGet, is((FileTagEntry) null));
  }

  @Test
  public void itShouldFindTwoElementsByCompanyId() throws Exception {
    fileTagEntryAccessor.put(companyId, "file1", "test", type, getList("value1"), user);
    fileTagEntryAccessor.put(companyId, "file2", "test", type, getList("value1"), user);
    fileTagEntryAccessor.put(companyId2, "file3", "test", type, getList("value1"), user);
    
    PagedResult<FileTagEntry> responseFromList = fileTagEntryAccessor.list(companyId, null, 100, null, null);
    
    assertThat(responseFromList.getList().size(), is(2));
  }

  @Test
  public void itShouldFindTwoElementsBySearchFilter() throws Exception {
    String companyId2 = "4598acdf-e2e5-72da-b2d1-20bd28b7fbf5";
    
    fileTagEntryAccessor.put(companyId, "file1", "test", type, getList("value1"), user);
    fileTagEntryAccessor.put(companyId, "file2", "test", type, getList("value1"), user);
    fileTagEntryAccessor.put(companyId2, "file3", "test", type, getList("value1"), user);
    
    PagedResult<FileTagEntry> responseFromList = fileTagEntryAccessor.list(companyId, "objectId: file2", 100, null, null);
    
    assertThat(responseFromList.getList().size(), is(1));
  }

  @Test
  public void itShouldRenameObjectIds() throws Exception {
    PagedResult<FileTagEntry> responseFromList = null;
    List<String> objs = getList("file1", "file2");
    
    tagDefinitionAccessor.put(companyId, type, "test2", getList("value1", "value2"), user);
    
    fileTagEntryAccessor.put(companyId, "file1", "test", type, getList("value1"), user);
    fileTagEntryAccessor.put(companyId, "file1", "test2", type, getList("value2"), user);
    fileTagEntryAccessor.put(companyId, "file2", "test", type, getList("value1"), user);
    fileTagEntryAccessor.put(companyId, "file3", "test2", type, getList("value1"), user);
    
    fileTagEntryAccessor.updateObjectId(companyId, objs, Utils.addPrefix(objs, "--TRASH--/"));
    
    responseFromList = fileTagEntryAccessor.list(companyId, "objectId: --TRASH--/file1", 100, null, null);
    assertThat(responseFromList.getList().size(), is(2));
    
    responseFromList = fileTagEntryAccessor.list(companyId, "objectId: file2", 100, null, null);
    assertThat(responseFromList.getList().size(), is(0));
    
    responseFromList = fileTagEntryAccessor.list(companyId, "objectId: file3", 100, null, null);
    assertThat(responseFromList.getList().size(), is(1));
  }
  
  @SuppressWarnings("unchecked")
  public <T> List<T> getList(T... values) {
    return Arrays.asList(values);
  }
}
