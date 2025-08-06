package com.accor.wcp.obfuscation.impl.masker;

import com.accor.wcp.obfuscation.impl.masker.maskers.EmailMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepFirstNbCharsMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepLastNbCharsMasker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MaskerServiceImplTest {

  @Test
  void should_mask_with_default_when_maskerId_is_not_recognized() {
    // Given
    MaskerServiceImpl underTest =
            new MaskerServiceImpl(List.of(new EmailMasker(), new KeepFirstNbCharsMasker(1), new KeepFirstNbCharsMasker(2), new KeepFirstNbCharsMasker(3), new KeepFirstNbCharsMasker(4),
                    new KeepLastNbCharsMasker(1), new KeepLastNbCharsMasker(2), new KeepLastNbCharsMasker(3), new KeepLastNbCharsMasker(4)));

    // When
    String masked = underTest.mask("hello world", "DEFAULT");

    // Then
    assertThat(masked).isEqualTo("*");
  }

  @Test
  void should_mask_with_default_when_maskerId_is_null() {
    // Given
    MaskerServiceImpl underTest =
            new MaskerServiceImpl(List.of(new EmailMasker(), new KeepFirstNbCharsMasker(1), new KeepFirstNbCharsMasker(2), new KeepFirstNbCharsMasker(3), new KeepFirstNbCharsMasker(4),
                    new KeepLastNbCharsMasker(1), new KeepLastNbCharsMasker(2), new KeepLastNbCharsMasker(3), new KeepLastNbCharsMasker(4)));

    // When
    String masked = underTest.mask("hello world", null);

    // Then
    assertThat(masked).isEqualTo("*");
  }
}
