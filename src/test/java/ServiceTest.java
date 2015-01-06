import org.junit.*;
import java.util.ArrayList;
import java.io.IOException;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;

import com.risevision.storage.gcs.StorageService;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.gcs.GCSMockClientBuilder;
import com.risevision.storage.gcs.StorageClientMock;
import com.risevision.storage.info.ServiceFailedException;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects.Insert;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.AbstractInputStreamContent;

public class ServiceTest {

  @Test public void confirmGCSInstance() {
    Storage mockClient = new GCSMockClientBuilder().build();
    StorageService gcsInstance = new StorageService(mockClient);
    assertThat(gcsInstance, instanceOf(StorageService.class));
  }

  @Test public void folderCanBeCreated() {
    StorageClientMock gcsClient = new StorageClientMock();
    StorageService service = new StorageService(gcsClient);
    
    try{
      service.createFolder("testBucket", "testFolder");
      assertEquals("testBucket", gcsClient.getObjectsInsert().getBucketName());
      assertEquals("testFolder/", gcsClient.getObjectsInsert().getData().getName());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception: " + e.toString());
    }
  }

  @Test public void folderCreationThrows() {
    StorageClientMock clientMock = new StorageClientMock();
    clientMock.setException(new IOException("testing thrown IOException"));
    StorageService service = new StorageService(clientMock);

    try {
      service.createFolder("testBucket", "testFolder");
      fail("should have thrown");
    } catch (ServiceFailedException e) {
      assertEquals(e.getReason(), ServiceFailedException.SERVER_ERROR);
    }
  }
}
