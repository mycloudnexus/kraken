package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.ingestion.fs.ClassPathResourceLoader;
import com.consoleconnect.kraken.operator.core.ingestion.fs.FileResourceLoader;
import com.consoleconnect.kraken.operator.core.ingestion.fs.RawDataResourceLoader;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceLoaderTest {
  @Test
  void testLoadDataFromClassPathFile() {
    ClassPathResourceLoader loader = new ClassPathResourceLoader();
    Assertions.assertEquals("classpath:", loader.getResourcePrefix());
    Optional<FileContentDescriptor> fileContentDescriptorOptional =
        loader.readFile("classpath:/deployment-config/kraken.yaml");
    Assertions.assertTrue(fileContentDescriptorOptional.isPresent());
    FileContentDescriptor fileContentDescriptor = fileContentDescriptorOptional.get();
    Assertions.assertNotNull(fileContentDescriptor);
    Assertions.assertNotNull(fileContentDescriptor.getContent());
    Assertions.assertNotNull(fileContentDescriptor.getSha());
  }

  @Test
  void testLoadDataFromFileSystem() {
    FileResourceLoader loader = new FileResourceLoader();
    Assertions.assertEquals("file:", loader.getResourcePrefix());
    Optional<FileContentDescriptor> fileContentDescriptorOptional =
        loader.readFile("file:src/test/resources/deployment-config/kraken.yaml");
    Assertions.assertTrue(fileContentDescriptorOptional.isPresent());
    FileContentDescriptor fileContentDescriptor = fileContentDescriptorOptional.get();
    Assertions.assertNotNull(fileContentDescriptor);
    Assertions.assertNotNull(fileContentDescriptor.getContent());
    Assertions.assertNotNull(fileContentDescriptor.getSha());
  }

  @Test
  void testLoadRawData() {
    UnifiedAsset asset = new UnifiedAsset();

    asset.setKind("kind");
    asset.setApiVersion("v1");
    asset.setFacets(new HashMap<>());
    Metadata metadata = new Metadata();
    metadata.setKey("key");
    metadata.setLabels(new HashMap<>());
    asset.setMetadata(metadata);

    String rawData = "raw:" + JsonToolkit.toJson(asset);
    RawDataResourceLoader loader = new RawDataResourceLoader();
    Assertions.assertEquals("raw:", loader.getResourcePrefix());
    Optional<FileContentDescriptor> fileContentDescriptorOptional = loader.readFile(rawData);
    Assertions.assertTrue(fileContentDescriptorOptional.isPresent());
    FileContentDescriptor fileContentDescriptor = fileContentDescriptorOptional.get();
    Assertions.assertNotNull(fileContentDescriptor);
    Assertions.assertNotNull(fileContentDescriptor.getContent());
    Assertions.assertNotNull(fileContentDescriptor.getSha());

    UnifiedAsset loadedAsset =
        JsonToolkit.fromJson(fileContentDescriptor.getContent(), UnifiedAsset.class);

    Assertions.assertEquals(asset.getKind(), loadedAsset.getKind());
    Assertions.assertEquals(asset.getApiVersion(), loadedAsset.getApiVersion());
    Assertions.assertEquals(asset.getMetadata().getKey(), loadedAsset.getMetadata().getKey());
  }
}
