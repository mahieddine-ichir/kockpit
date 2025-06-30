package org.kockpit.rules.registry.registryappwithflow;

import org.kockpit.rules.registry.dao.RegistryDao;
import org.kockpit.rules.registry.model.Registry;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
