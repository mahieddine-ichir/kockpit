package com.accor.kengine.testers.v32;

import com.accor.kengine.seamless.Flow;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thepavel.icomponent.InterfaceComponentScan;

@SpringBootApplication
@InterfaceComponentScan(annotation = Flow.class)
class SeamLessV32Application {}
