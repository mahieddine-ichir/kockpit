package com.accor.wcp.obfuscation.impl.masker.maskers;

import com.accor.wcp.obfuscation.masker.Masker;

import static java.util.Objects.isNull;

public class KeepLastNbCharsMasker implements Masker {

    private final int nbLastCharsToKeep;
    private final String REGEX_KEEP_NB_LAST_CHARS;

    public KeepLastNbCharsMasker(int nbCharsToKeep) {
        this.nbLastCharsToKeep = nbCharsToKeep;
        this.REGEX_KEEP_NB_LAST_CHARS = String.format("(.)(?=.{%d})", nbCharsToKeep);
    }

    @Override
    public String getType() {
        return "keepLast" + nbLastCharsToKeep;
    }

    @Override
    public String mask(String input) {
        if (isNull(input)) {
            return null;
        }

        if(input.length() <= nbLastCharsToKeep) {
            return input.replaceAll("(.)", "*");
        }
        else {
            return input.replaceAll(REGEX_KEEP_NB_LAST_CHARS, "*");
        }
    }
}
