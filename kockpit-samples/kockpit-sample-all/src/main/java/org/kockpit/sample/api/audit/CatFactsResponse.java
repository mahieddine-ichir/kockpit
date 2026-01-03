package org.kockpit.sample.api.audit;

import lombok.Data;

import java.util.List;

@Data
public class CatFactsResponse {
    private List<CatFact> data;
}
