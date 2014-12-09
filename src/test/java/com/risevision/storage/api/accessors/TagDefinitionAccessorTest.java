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
import com.risevision.storage.api.UserCompanyVerifier;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.datastore.ObjectifyTest;
import com.risevision.storage.entities.TagDefinition;

/**
 * Created by rodrigopavezi on 12/9/14.
 */
public class TagDefinitionAccessorTest extends ObjectifyTest {

  private TagDefinitionAccessor tagDefinitionAccessor;

  private String companyId;
  private String name;
  private String type;
  private List<String> values;
  private User user;

  UserCompanyVerifier userCompanyVerifier;

  @Before
  public void setUp() {
    super.setUp();
    companyId = "72dab2d1-e2e5-4598-acdf-20bd28b7fbf5";
    name = "test";
    type =  "Lookup";
    values = new LinkedList<String>();
    values.add("value1");
    user = new User("test@gmail.com","example.com");

    userCompanyVerifier = mock(UserCompanyVerifier.class);

    tagDefinitionAccessor = new TagDefinitionAccessor();
  }

  @After
  public void tearDown() throws IOException {
    super.tearDown();
  }


  @Test
  public void itShouldAddATagDefinition() throws Exception {
    TagDefinition response = tagDefinitionAccessor.put(companyId, name, type, values, user);

    assertThat(response, is(instanceOf(TagDefinition.class)));
  }

  @Test
  public void itShouldAddATagDefinitionWithAnUUID() throws Exception {
    TagDefinition response = tagDefinitionAccessor.put(companyId, name, type, values, user);

    assertThat(response.getId(), is(notNullValue()));
    assertThat(response.getId(), is(instanceOf(String.class)));
  }

  @Test
  public void itShouldAddATagDefinitionWithStatusParameters() throws Exception {
    TagDefinition response = tagDefinitionAccessor.put(companyId, name, type, values, user);

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
    TagDefinition response = tagDefinitionAccessor.put(companyId, name, type, values, user);

    // check if it saves in lower case
    assertThat(response.getName(), is("test"));
    assertThat(response.getValues(), hasItem("value1"));
    assertThat(response.getValues(), hasItem("value2"));
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATagDefinitionWithoutCompanyId() throws Exception {
    @SuppressWarnings("unused")
    TagDefinition response = tagDefinitionAccessor.put("", name, type, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATagDefinitionWithoutName() throws Exception {
    @SuppressWarnings("unused")
    TagDefinition response = tagDefinitionAccessor.put(companyId, "", type, values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddATagDefinitionWithoutType() throws Exception {
    @SuppressWarnings("unused")
    TagDefinition response = tagDefinitionAccessor.put(companyId, name, "", values, user);
  }

  @Test(expected = ValidationException.class)
  public void itShouldNotAddAFreeformTagDefinitionWithValues() throws Exception {
    @SuppressWarnings("unused")
    TagDefinition response = tagDefinitionAccessor.put(companyId, name, "Freeform", values, user);
  }

  @Test
  public void itShouldRetrieveATagDefinitionById() throws Exception {
    TagDefinition responseFromAdd = tagDefinitionAccessor.put(companyId, name, type, values, user);

    String id = responseFromAdd.getId();

    TagDefinition responseFromGet = tagDefinitionAccessor.get(id);

    assertThat(responseFromGet.getId(), is(id));
  }

  @Test
  public void itShouldDeleteATagDefinitionById() throws Exception {
    TagDefinition responseFromAdd = tagDefinitionAccessor.put(companyId, name, type, values, user);

    String id = responseFromAdd.getId();
    
    tagDefinitionAccessor.delete(id);
    
    TagDefinition responseFromGet = tagDefinitionAccessor.get(id);

    assertThat((TagDefinition) responseFromGet, is((TagDefinition) null));
  }

  @Test
  public void itShouldFindTwoElementsByCompanyId() throws Exception {
    String companyId2 = "4598acdf-e2e5-72da-b2d1-20bd28b7fbf5";
    
    tagDefinitionAccessor.put(companyId, "test1", type, getList("test_value1"), user);
    tagDefinitionAccessor.put(companyId, "test2", type, getList("test_value2"), user);
    tagDefinitionAccessor.put(companyId2, "test3", type, getList("test_value3"), user);
    
    PagedResult<TagDefinition> responseFromList = tagDefinitionAccessor.list(companyId, null, 100, null, null);
    
    assertThat(responseFromList.getList().size(), is(2));
  }
  
  @SuppressWarnings("unchecked")
  public <T> List<T> getList(T... values) {
    return Arrays.asList(values);
  }
}
