package com.accor.kengine.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thepavel.icomponent.InterfaceComponentScan;

@SpringBootApplication
@InterfaceComponentScan
public class KEngineStarterTesterApp {
  public static void main(String[] args) {
    SpringApplication.run(KEngineStarterTesterApp.class, args);
  }
}
