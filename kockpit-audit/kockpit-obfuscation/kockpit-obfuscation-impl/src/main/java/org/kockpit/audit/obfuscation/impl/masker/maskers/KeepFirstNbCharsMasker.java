package org.kockpit.audit.obfuscation.impl.masker.maskers;

import org.kockpit.audit.obfuscation.masker.Masker;

import static java.util.Objects.isNull;

public class KeepFirstNbCharsMasker implements Masker {

    private final int nbFirstCharsToKeep;
    private final String REGEX_KEEP_NB_FIRST_CHARS;

    public KeepFirstNbCharsMasker(int nbCharsToKeep) {
        this.nbFirstCharsToKeep = nbCharsToKeep;
        this.REGEX_KEEP_NB_FIRST_CHARS = String.format("(.)(?<=..{%d})", nbCharsToKeep);
    }

    @Override
    public String getType() {
        return "keepFirst" + nbFirstCharsToKeep;
    }

    @Override
    public String mask(String input) {
        if (isNull(input)) {
            return null;
        }

        if(input.length() <= nbFirstCharsToKeep) {
            return input.replaceAll("(.)", "*");
        }
        else {
            return input.replaceAll(REGEX_KEEP_NB_FIRST_CHARS, "*");
        }
    }
}
