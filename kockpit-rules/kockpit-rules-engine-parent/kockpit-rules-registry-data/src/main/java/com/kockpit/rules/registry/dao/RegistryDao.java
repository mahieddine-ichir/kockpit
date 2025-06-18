package com.kockpit.rules.registry.dao;

import com.kockpit.rules.registry.model.Registry;
import java.util.Optional;

public interface RegistryDao {

  Optional<? extends Registry> get(long id);

  void insert(Registry registry);
}
