package com.kockpit.samples.impl;

import com.kockpit.rules.seemless.Action;
import com.kockpit.rules.seemless.ContextResult;
import com.kockpit.samples.rules.Action_2Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Action_2ActionImpl implements Action_2Action {

    @Action
    @ContextResult
    String upper(String name, String autreChose) {
        log.info("{} {}", name, autreChose);
        return name.toUpperCase();
    }
}
