package org.kockpit.backend.services.search;

import java.util.List;

/**
 * {@code appIds}, when not empty, restricts the results to the audits of these applications
 * (the appIds a manifest's audit service declares); null or empty means every application.
 */
public interface SearchService {

    Object getAudit(String domain, String env, String id);

    Page listAudits(String domain, String env, List<String> appIds, Integer start, Integer size);

    Page searchAudits(String domain, String env, String query, List<SearchTerm> searchTerms, List<String> appIds, Integer start, Integer size);

    Page searchByQuery(String domain, String env, String query, List<String> appIds, Integer start, Integer size);
}
