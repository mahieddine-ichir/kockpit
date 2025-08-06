package com.accor.wcp.sample.obfuscationlib.masker;

import com.accor.wcp.obfuscation.masker.Masker;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

class CustomParamsUrlMasker implements Masker {
    private final List<String> keys = List.of("key1", "key2");

    @Override
    public String getType() {
        return "paramsUrl";
    }

    @Override
    public String mask(String input) {
        if (isNull(input)) {
            return null;
        }

        String keysList = keys.stream().map(key -> key + "=").collect(Collectors.joining("|"));
        return input.replaceAll("(?<=(" + keysList + "))(.\\w*)", "*****");
    }
}
