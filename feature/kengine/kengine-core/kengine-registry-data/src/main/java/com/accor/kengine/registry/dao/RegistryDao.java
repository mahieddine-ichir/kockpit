package com.accor.kengine.registry.dao;

import com.accor.kengine.registry.model.Registry;
import java.util.Optional;

public interface RegistryDao {

  Optional<? extends Registry> get(long id);

  void insert(Registry registry);
}
