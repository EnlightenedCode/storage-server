import org.junit.Test;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;

import com.risevision.storage.gcs.StorageServiceImpl;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.gcs.LocalCredentialBuilder;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects.Insert;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.AbstractInputStreamContent;

public class ServiceImplTest {

  @Test
  public void confirmGCSInstance() {
    MediaLibraryService gcsInstance = MediaLibraryService.getGCSInstance();
    assertThat(gcsInstance, instanceOf(StorageServiceImpl.class));
  }

  @Test
  public void folderCanBeCreated() {
    StorageServiceImpl gcsInstance = MediaLibraryService.getGCSInstance();
    
    //Mock the google client objects
    Storage storageMock = mock(Storage.class);
    Storage.Objects objectsMock = mock(Storage.Objects.class);
    Storage.Objects.Insert insertMock = mock(Storage.Objects.Insert.class);
    gcsInstance.setClient(storageMock);

    //Set up the argument watching
    ArgumentCaptor<String> stringArg = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<StorageObject> folderArg = ArgumentCaptor.forClass(StorageObject.class);
    ArgumentCaptor<ByteArrayContent> contentArg = ArgumentCaptor.forClass(ByteArrayContent.class);

    //We'll be verifying insert and execute calls were made in order
    InOrder inOrder = inOrder(objectsMock, insertMock);

    try {
      when(storageMock.objects()).thenReturn(objectsMock);
      when(objectsMock.insert(any(String.class),
                              any(StorageObject.class),
                              any(AbstractInputStreamContent.class))
      ).thenReturn(insertMock);

      gcsInstance.createFolder("testBucket", "testFolder");

      //Verify insert call was made properly
      inOrder.verify(objectsMock).insert(stringArg.capture(),
                                         folderArg.capture(),
                                         contentArg.capture());
      assertEquals("testBucket", stringArg.getValue());
      assertEquals("testFolder/", folderArg.getValue().getName());
      assertEquals(-1, contentArg.getValue().getInputStream().read());

      //Verify execute was called
      inOrder.verify(insertMock).execute();
    } catch (Exception e) {
      fail("Unexpected exception: " + e.toString());
    }
  }

  @Test
  public void bucketListRequestWithNoParameters() {
    StorageServiceImpl gcsInstance = MediaLibraryService.getGCSInstance();
    
    //Mock the google client objects
    Storage storageMock = mock(Storage.class);
    Storage.Objects objectsMock = mock(Storage.Objects.class);
    Storage.Objects.List listMock = mock(Storage.Objects.List.class);
//    com.google.api.services.storage.model.Objects listResult;
    gcsInstance.setClient(storageMock);

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
