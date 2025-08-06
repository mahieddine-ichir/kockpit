package com.accor.wcp.obfuscation.impl.masker.maskers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class KeepFirstNbCharsMaskerTest {

    private final KeepFirstNbCharsMasker keepFirst1CharsMasker = new KeepFirstNbCharsMasker(1);
    private final KeepFirstNbCharsMasker keepFirst2CharsMasker = new KeepFirstNbCharsMasker(2);
    private final KeepFirstNbCharsMasker keepFirst3CharsMasker = new KeepFirstNbCharsMasker(3);
    private final KeepFirstNbCharsMasker keepFirst4CharsMasker = new KeepFirstNbCharsMasker(4);


    @Test
    void should_return_keepFirstNbCharsMasker_masker_type() {
        //1 first char to keep
        assertThat(keepFirst1CharsMasker.getType()).isEqualTo("keepFirst1");

        //2 first chars to keep
        assertThat(keepFirst2CharsMasker.getType()).isEqualTo("keepFirst2");

        //3 first chars to keep
        assertThat(keepFirst3CharsMasker.getType()).isEqualTo("keepFirst3");

        //4 first chars to keep
        assertThat(keepFirst4CharsMasker.getType()).isEqualTo("keepFirst4");
    }

    @Test
    void should_mask_data_with_keepFirstNbCharsMasker_regex() {
        //1 first char to keep
        assertThat(keepFirst1CharsMasker.mask("This is a test")).isEqualTo("T*************");
        assertThat(keepFirst1CharsMasker.mask("ab")).isEqualTo("a*");

        //2 first chars to keep
        assertThat(keepFirst2CharsMasker.mask("This is a test")).isEqualTo("Th************");
        assertThat(keepFirst2CharsMasker.mask("abc")).isEqualTo("ab*");

        //3 first chars to keep
        assertThat(keepFirst3CharsMasker.mask("This is a test")).isEqualTo("Thi***********");
        assertThat(keepFirst3CharsMasker.mask("abcd")).isEqualTo("abc*");

        //4 first chars to keep
        assertThat(keepFirst4CharsMasker.mask("This is a test")).isEqualTo("This**********");
        assertThat(keepFirst4CharsMasker.mask("abcde")).isEqualTo("abcd*");
    }

    @Test
    void should_mask_data_with_keepFirstNbCharsMasker_regex_if_char_inf_nbToKeep() {
        //1 first char to keep
        assertThat(keepFirst1CharsMasker.mask("a")).isEqualTo("*");

        //2 first chars to keep
        assertThat(keepFirst2CharsMasker.mask("a")).isEqualTo("*");
        assertThat(keepFirst2CharsMasker.mask("ab")).isEqualTo("**");

        //3 first chars to keep
        assertThat(keepFirst3CharsMasker.mask("a")).isEqualTo("*");
        assertThat(keepFirst3CharsMasker.mask("ab")).isEqualTo("**");
        assertThat(keepFirst3CharsMasker.mask("abc")).isEqualTo("***");

        //4 first chars to keep
        assertThat(keepFirst4CharsMasker.mask("a")).isEqualTo("*");
        assertThat(keepFirst4CharsMasker.mask("ab")).isEqualTo("**");
        assertThat(keepFirst4CharsMasker.mask("abc")).isEqualTo("***");
        assertThat(keepFirst4CharsMasker.mask("abcd")).isEqualTo("****");
    }

    @Test
    void should_not_mask_data_when_input_is_null() {
        //1 first char to keep
        assertThat(keepFirst1CharsMasker.mask(null)).isNull();

        //2 first chars to keep
        assertThat(keepFirst2CharsMasker.mask(null)).isNull();

        //3 first chars to keep
        assertThat(keepFirst3CharsMasker.mask(null)).isNull();

        //4 first chars to keep
        assertThat(keepFirst4CharsMasker.mask(null)).isNull();
    }

}
