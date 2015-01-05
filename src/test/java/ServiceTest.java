import org.junit.*;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;

import com.risevision.storage.gcs.StorageService;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.gcs.GCSMockClientBuilder;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects.Insert;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.AbstractInputStreamContent;

public class ServiceTest {

  @Test
  public void confirmGCSInstance() {
    Storage mockClient = new GCSMockClientBuilder().build();
    StorageService gcsInstance = new StorageService(mockClient);
    assertThat(gcsInstance, instanceOf(StorageService.class));
  }

  @Ignore @Test
  public void folderCanBeCreated() {
    GCSMockClientBuilder mockClientBuilder = new GCSMockClientBuilder();
    Storage gcsClient = mockClientBuilder.build();
    StorageService service = new StorageService(gcsClient);
    
    try{
      service.createFolder("testBucket", "testFolder");
      assertEquals("testBucket", mockClientBuilder);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception: " + e.toString());
    }
  }

  @Ignore @Test
  public void bucketListRequestWithNoParameters() {
    StorageService gcsInstance = new StorageService(new GCSMockClientBuilder().build());
    
    //Mock the google client objects
    Storage storageMock = mock(Storage.class);
    Storage.Objects objectsMock = mock(Storage.Objects.class);
    Storage.Objects.List listMock = mock(Storage.Objects.List.class);

    //Set up the argument watching
    try {
      when(storageMock.objects()).thenReturn(objectsMock);
      when(objectsMock.list(any(String.class)))
          .thenReturn(listMock);
      when(listMock.setPrefix(any(String.class))).thenReturn(listMock);
      when(listMock.setDelimiter(any(String.class))).thenReturn(listMock);
      when(listMock.execute())
          .thenReturn(new com.google.api.services.storage.model.Objects());

      gcsInstance.getBucketItems("", "", "");

      //Verify insert call was made properly
      verify(objectsMock).list("");

      //Verify execute was called
      verify(listMock).execute();
    } catch (Exception e) {
      fail("Unexpected exception: " + e.toString());
    }
  }
}
