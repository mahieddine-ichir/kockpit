package com.accor.wcp.console.services.core.appmanifest.memory;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.services.core.appmanifest.manifest.Manifest;
import com.accor.wcp.console.services.core.servicemanager.ServiceManagerImpl;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/console/manifests")
@RequiredArgsConstructor
@Slf4j
//@Conditional(ConfigCondition.class)
public class AppManifestMemoryApi {
  private final AppManifestMemoryRepo appManifestMemoryRepo;
  private final ServiceManagerImpl serviceManagerImpl;

  @PostMapping()
  public Collection<AppManifest> uploadManifest(
      @RequestParam("file") List<MultipartFile> manifestFiles) throws IOException {
    for (MultipartFile manifestFile : manifestFiles) {
      String manifestContent = new String(manifestFile.getBytes(), StandardCharsets.UTF_8);
      JSONObject manifestJsonContent = new JSONObject(manifestContent);

      Manifest manifest =
          new Manifest(
              manifestFile.getOriginalFilename(), Instant.now(), manifestJsonContent, "Memory");
      appManifestMemoryRepo.addManifestInMemory(manifest);
    }
    serviceManagerImpl.processAllManifests();
    return appManifestMemoryRepo.findAll();
  }

  @PostMapping("delete")
  public Collection<AppManifest> deleteManifest(@RequestBody String fileName) {
    appManifestMemoryRepo.deleteManifestInMemory(fileName);
    serviceManagerImpl.processAllManifests();
    return appManifestMemoryRepo.findAll();
  }
}
