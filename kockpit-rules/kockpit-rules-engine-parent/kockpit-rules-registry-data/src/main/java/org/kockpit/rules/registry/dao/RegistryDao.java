package org.kockpit.rules.registry.dao;

import org.kockpit.rules.registry.model.Registry;
import java.util.Optional;

public interface RegistryDao {

  Optional<? extends Registry> get(long id);

  void insert(Registry registry);
}
