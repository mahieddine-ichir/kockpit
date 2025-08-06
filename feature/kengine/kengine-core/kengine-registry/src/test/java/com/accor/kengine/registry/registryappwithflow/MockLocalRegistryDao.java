package com.accor.kengine.registry.registryappwithflow;

import com.accor.kengine.registry.dao.RegistryDao;
import com.accor.kengine.registry.model.Registry;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MockLocalRegistryDao implements RegistryDao {
  private Registry registry;

  @Override
  public Optional<? extends Registry> get(long id) {
    return Optional.empty();
  }

  @Override
  public void insert(Registry registry) {
    this.registry = registry;
  }

  public Registry getRegistry() {
    return registry;
  }
}
