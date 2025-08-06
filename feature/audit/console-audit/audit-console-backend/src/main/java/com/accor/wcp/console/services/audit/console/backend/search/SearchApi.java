package com.accor.wcp.console.services.audit.console.backend.search;

import com.accor.wcp.console.services.audit.console.backend.AuditServiceActivator;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReport;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReportPage;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditViewDto;
import com.accor.wcp.console.services.audit.console.backend.search.dto.SearchQuery;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@RestController
@RequestMapping("${console.services.path-prefix}/audit/requests")
@CrossOrigin(origins = "*", originPatterns = "*")
@RequiredArgsConstructor
@Slf4j
public class SearchApi {

  private final AuditRequestService auditRequestService;

  private final AuditServiceActivator auditServiceActivator;

  @GetMapping(value = "/{auditViewName}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public AuditReport one(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String auditViewName,
      @PathVariable String id) {
    return auditRequestService.one(domain, env, id);
  }

  @SneakyThrows
  @PostMapping("/{auditViewName}/_search")
  public List<AuditReport> search(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String auditViewName,
      @RequestBody Collection<SearchQuery> query,
      @RequestParam("size") Optional<Integer> size) {
    Optional<AuditViewDto> auditView =
        auditServiceActivator.getAuditServiceManifest().getAuditViews().stream()
            .filter(av -> av.getName().equals(auditViewName))
            .findFirst();
    if (auditView.isPresent()) {
      return auditRequestService.search(domain, env, auditView.get(), query, size);
    } else {
      return emptyList();
    }
  }

  @SneakyThrows
  @PostMapping("/{auditViewName}/v2/_search")
  public AuditReportPage search(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String auditViewName,
      @RequestBody Collection<SearchQuery> query,
      @RequestParam(value = "from", defaultValue = "0") Integer from,
      @RequestParam(value = "size", defaultValue = "10") Integer size
      ) {

    return auditServiceActivator.getAuditServiceManifest().getAuditViews().stream()
            .filter(av -> av.getName().equals(auditViewName))
            .findFirst()
            .map(auditViewDto -> auditRequestService.search(domain, env, auditViewDto, query, Optional.of(from), Optional.of(size)))
            .orElse(AuditReportPage.empty());
  }
}
