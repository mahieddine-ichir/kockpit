package com.accor.wcp.obfuscation.impl.masker.maskers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class KeepLastNbCharsMaskerTest {

    private final KeepLastNbCharsMasker keepLast1CharsMasker = new KeepLastNbCharsMasker(1);
    private final KeepLastNbCharsMasker keepLast2CharsMasker = new KeepLastNbCharsMasker(2);
    private final KeepLastNbCharsMasker keepLast3CharsMasker = new KeepLastNbCharsMasker(3);
    private final KeepLastNbCharsMasker keepLast4CharsMasker = new KeepLastNbCharsMasker(4);


    @Test
    void should_return_keepLastNbCharsMasker_masker_type() {
        //Last 1 char to keep
        assertThat(keepLast1CharsMasker.getType()).isEqualTo("keepLast1");

        //Last 2 chars to keep
        assertThat(keepLast2CharsMasker.getType()).isEqualTo("keepLast2");

        //Last 3 chars to keep
        assertThat(keepLast3CharsMasker.getType()).isEqualTo("keepLast3");

        //Last 4 chars to keep
        assertThat(keepLast4CharsMasker.getType()).isEqualTo("keepLast4");
    }

    @Test
    void should_mask_data_with_keepLastNbCharsMasker_regex() {
        //Last 1 char to keep
        assertThat(keepLast1CharsMasker.mask("This is a test")).isEqualTo("*************t");
        assertThat(keepLast1CharsMasker.mask("ab")).isEqualTo("*b");

        //Last 2 chars to keep
        assertThat(keepLast2CharsMasker.mask("This is a test")).isEqualTo("************st");
        assertThat(keepLast2CharsMasker.mask("abc")).isEqualTo("*bc");

        //Last 3 chars to keep
        assertThat(keepLast3CharsMasker.mask("This is a test")).isEqualTo("***********est");
        assertThat(keepLast3CharsMasker.mask("abcd")).isEqualTo("*bcd");

        //Last 4 chars to keep
        assertThat(keepLast4CharsMasker.mask("This is a test")).isEqualTo("**********test");
        assertThat(keepLast4CharsMasker.mask("abcde")).isEqualTo("*bcde");
    }

    @Test
    void should_mask__data_with_keepLastNbCharsMasker_regex_if_data_inf_nbToKeep() {
        //Last 1 char to keep
        assertThat(keepLast1CharsMasker.mask("a")).isEqualTo("*");

        //Last 2 chars to keep
        assertThat(keepLast2CharsMasker.mask("a")).isEqualTo("*");
        assertThat(keepLast2CharsMasker.mask("ab")).isEqualTo("**");

        //Last 3 chars to keep
        assertThat(keepLast3CharsMasker.mask("a")).isEqualTo("*");
        assertThat(keepLast3CharsMasker.mask("ab")).isEqualTo("**");
        assertThat(keepLast3CharsMasker.mask("abc")).isEqualTo("***");

        //Last 4 chars to keep
        assertThat(keepLast4CharsMasker.mask("a")).isEqualTo("*");
        assertThat(keepLast4CharsMasker.mask("ab")).isEqualTo("**");
        assertThat(keepLast4CharsMasker.mask("abc")).isEqualTo("***");
        assertThat(keepLast4CharsMasker.mask("abcd")).isEqualTo("****");
    }

    @Test
    void should_not_mask_data_when_input_is_null() {
        //Last 1 char to keep
        assertThat(keepLast1CharsMasker.mask(null)).isNull();

        //Last 2 chars to keep
        assertThat(keepLast2CharsMasker.mask(null)).isNull();

        //Last 3 chars to keep
        assertThat(keepLast3CharsMasker.mask(null)).isNull();

        //Last 4 chars to keep
        assertThat(keepLast4CharsMasker.mask(null)).isNull();
    }
}
