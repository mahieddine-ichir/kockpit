package org.kockpit.audit.obfuscation.impl.masker;

import org.kockpit.audit.obfuscation.masker.Masker;
import org.kockpit.audit.obfuscation.masker.MaskerService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@Slf4j
public class MaskerServiceImpl implements MaskerService {

  private final Map<String, Masker> maskersByType;

  public MaskerServiceImpl(List<Masker> maskers) {
    maskersByType = new HashMap<>();
    maskers.forEach(masker -> {
      if(maskersByType.containsKey(masker.getType())) {
        log.warn("Multiple maskers with the same id {}", masker.getType());
      } else {
        maskersByType.put(masker.getType(), masker);
      }
    });
  }

  @Override
  public String mask(String data, String maskerId) {
    if (nonNull(maskerId)) {
      Masker masker = maskersByType.get(maskerId);
      if (nonNull(masker)) {
        return masker.mask(data);
      } else {
        log.warn("Masker with id: {} not found. Using default one.", maskerId);
      }
    }

    // TODO default mask, dynamic custom mask?

    // Else default mask
    return "*";
  }
}
