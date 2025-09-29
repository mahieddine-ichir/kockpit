package org.kockpit.backend.services.search;

import java.util.List;

public interface SearchService {

    Object getAudit(String domain, String env, String id);

    Page listAudits(String domain, String env, Integer start, Integer size);

    Page searchAudits(String domain, String env, String query, List<SearchTerm> searchTerms, Integer start, Integer size);
}
