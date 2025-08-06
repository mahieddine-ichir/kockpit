package com.accor.wcp.console.services.core.appmanifest.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.services.core.appmanifest.manifest.Manifest;
import com.accor.wcp.console.services.core.appmanifest.s3.datasource.ResourceLoaderS3;
import com.accor.wcp.console.services.sqk.TestUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppManifestS3RepoTest {

  private AppManifestS3Repo underTest;
  @Mock private ResourceLoaderS3 resourceLoaderS3;

  @BeforeEach
  void setUp() {
    List<Manifest> manifests = TestUtils.loadFakeManifests();
    when(resourceLoaderS3.getBucketObjects()).thenReturn(manifests);
    underTest = new AppManifestS3Repo(resourceLoaderS3);
  }

  @Test
  void findAll() {
    List<AppManifest> appManifests = (List<AppManifest>) underTest.findAll();

    assertThat(appManifests.size()).isEqualTo(5);

    assertThat(appManifests.get(0).getDomain()).isEqualTo("WCXSS");
    assertThat(appManifests.get(0).getEnv()).isEqualTo("test");
    assertThat(appManifests.get(0).getApplicationId()).isEqualTo("wcxss-insurance");
    assertThat(appManifests.get(0).getServiceIds()).contains("audit");
    assertThat(appManifests.get(0).getGroups()).contains("test1");

    assertThat(appManifests.get(1).getDomain()).isEqualTo("WCXSS");
    assertThat(appManifests.get(1).getEnv()).isEqualTo("test");
    assertThat(appManifests.get(1).getApplicationId()).isEqualTo("wcxss-insurance-quotation");
    assertThat(appManifests.get(1).getServiceIds()).contains("audit", "cache");
    assertThat(appManifests.get(1).getGroups()).contains("test1", "admin");
  }
}
