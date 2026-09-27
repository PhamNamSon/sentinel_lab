package io.namson.targetapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TargetController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
