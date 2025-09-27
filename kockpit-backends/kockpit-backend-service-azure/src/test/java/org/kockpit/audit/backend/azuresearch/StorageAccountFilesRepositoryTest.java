package org.kockpit.audit.backend.azuresearch;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.IterableStream;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kockpit.audit.backend.ConfigItem;
import org.kockpit.sdk.SdkApplicationProperties;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(StorageAccountFilesRepository.class)
@Slf4j
class StorageAccountFilesRepositoryTest {

    @Autowired
    StorageAccountFilesRepository storageAccountFilesRepository;

    @MockitoBean
    BlobContainerClient blobContainerClient;

    @MockitoBean
    SdkApplicationProperties sdkApplicationProperties;

    @Test
    void download_file() {
        BlobClient blobClient = mock(BlobClient.class);
        Mockito.doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(0);
            outputStream.write(this.getClass().getResourceAsStream("/sample/api-config.json").readAllBytes());
            return null;
        }).when(blobClient).downloadStream(Mockito.any(OutputStream.class));

        BlobItem blobItem = mock(BlobItem.class);
        when(blobItem.getName()).thenReturn("api-config.json");
        PagedResponse<BlobItem> pagedResponse = mock(PagedResponse.class);

        List<BlobItem> blobItems = List.of(blobItem);
        when(pagedResponse.getValue()).thenReturn(blobItems);
        when(pagedResponse.getElements()).thenReturn(IterableStream.of(blobItems));

        PagedIterable<BlobItem> blobClients = new PagedIterable<>(() -> pagedResponse);

        blobClients.stream().forEach(blobItem1 -> log.info("{}", blobItem1));

        when(blobContainerClient.listBlobs()).thenReturn(blobClients);
        when(blobContainerClient.getBlobClient(Mockito.anyString())).thenReturn(blobClient);

        List<ConfigItem> configItems = storageAccountFilesRepository.getConfig().getBody();
        log.info("configItems {}", configItems);

        configItems.stream().map(ConfigItem::getServices)
                .flatMap(List::stream)
                .filter(service -> service.getName().equals("audit"))
                .findFirst()
                .ifPresentOrElse(service -> {
                    Object config = service.getConfig();
                    if (config instanceof Map map) {
                        List columns = (List) map.get("columns");
                        assertTrue(columns.contains("appId"));
                    } else {
                        fail();
                    }
                }, Assertions::fail);

        configItems.stream().map(ConfigItem::getServices)
                .flatMap(List::stream)
                .filter(service -> service.getName().equals("dyna-config"))
                .findFirst()
                .ifPresentOrElse(service -> {
                    Object config = service.getConfig();
                    if (config instanceof Map map) {
                        List<Map<String, Object>> keys = ((List) map.get("keys"));
                        assertTrue(keys.stream()
                                .peek(key -> log.info("key({}) {}", key.getClass(), key))
                                .anyMatch(key -> key.get("name").equals("keyString")
                                        && key.get("value").equals("any string value")));
                    } else {
                        fail();
                    }
                }, Assertions::fail);
    }

}
