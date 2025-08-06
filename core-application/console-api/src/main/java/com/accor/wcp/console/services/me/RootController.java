package com.accor.wcp.console.services.me;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

@Controller
class RootController {

    @Value("${auth.root.redirecturl}")
    private String redirectUrl;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public RedirectView root() {
        return new RedirectView(redirectUrl);
    }
}
