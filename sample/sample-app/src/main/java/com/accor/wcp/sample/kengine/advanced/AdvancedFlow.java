package com.accor.wcp.sample.kengine.advanced;

import com.accor.kengine.seamless.Flow;
import java.util.UUID;

@Flow(id = "AdvancedFlow", documentation = "Advanced flow definition",
ruleIds = {"LoadUser", "LoadCompany", "EnrichUser"})
public interface AdvancedFlow {

  EnrichedUser enrichedUser(UUID userId, UUID companyId);

}
