package org.kockpit.audit.backend.azuresearch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kockpit.audit.backend.ConfigAudit;
import org.kockpit.audit.backend.ConfigItem;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.OutputStream;
import java.util.List;

@ExtendWith(SpringExtension.class)
@Import(StorageAccountFilesRepository.class)
class StorageAccountFilesRepositoryTest {

    @Autowired
    StorageAccountFilesRepository storageAccountFilesRepository;

    @MockitoBean
    BlobContainerClient blobContainerClient;

    @Test
    void download_file() {
        BlobClient blobClient = Mockito.mock(BlobClient.class);

        Mockito.doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(0);
            outputStream.write(this.getClass().getResourceAsStream("/rcu-manifest.json").readAllBytes());
            return null;
        }).when(blobClient).downloadStream(Mockito.any(OutputStream.class));

        Mockito.when(blobContainerClient.getBlobClient("manifests/rcu-manifest.json"))
                .thenReturn(blobClient);

        List<ConfigItem> configs = storageAccountFilesRepository.findConfigs();
        System.out.println(configs);

        ConfigAudit config = configs.get(0).getServices().get(0).getConfig();
        Assertions.assertNotNull(config);
        Assertions.assertFalse(config.getColumns().isEmpty());
    }

}