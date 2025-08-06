package com.accor.wcp.obfuscation.impl.obfuscators.value;

import com.accor.wcp.obfuscation.ObfuscateConfig;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValueObfuscateConfig implements ObfuscateConfig {

    private final String maskerId;

    public ValueObfuscateConfig(String maskerId) {
        this.maskerId = maskerId;
    }

    public ValueObfuscateConfig(){
        this.maskerId = DEFAULT_MASKER_ID;
    }

    @Override
    public String getMaskerId() {
        return this.maskerId;
    }

}
