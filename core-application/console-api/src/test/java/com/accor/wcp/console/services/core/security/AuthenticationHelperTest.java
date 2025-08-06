package com.accor.wcp.console.services.core.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.accor.wcp.console.services.core.security.AuthenticationHelper.computeCustomAdGroupsStringToList;

class AuthenticationHelperTest {
    @Test
    public void testComputeCustomAdGroupsStringToList() {
        List<String> groups = computeCustomAdGroupsStringToList("[AWS-359195335135-wcc_user, AWS-531583874639-platform-dev-MCO, AWS-359195335135-wcc-dev-MCO, AWS-428415080805-xss-dev-Admin]");
        Assertions.assertNotNull(groups);
        Assertions.assertEquals(4, groups.size());
        Assertions.assertEquals("AWS-359195335135-wcc-dev-MCO", groups.get(2));
        Assertions.assertEquals("AWS-428415080805-xss-dev-Admin", groups.get(3));
    }
}
