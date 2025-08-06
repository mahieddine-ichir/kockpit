package com.accor.wcp.console.services.dynaconfig.manifest;

import lombok.Getter;

import java.io.Serializable;
import lombok.Setter;

@Getter
@Setter
public class DynaConfigPropertySettings implements Serializable {
    private String name;
    private String label;
    private String description;
    private String type;
}
