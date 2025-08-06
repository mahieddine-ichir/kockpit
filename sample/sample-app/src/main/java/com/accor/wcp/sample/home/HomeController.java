package com.accor.wcp.sample.home;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @RequestMapping(value = "/home")
  public ResponseEntity<String> home(HttpServletResponse response) {
    return ResponseEntity.ok("UP");
  }
}
