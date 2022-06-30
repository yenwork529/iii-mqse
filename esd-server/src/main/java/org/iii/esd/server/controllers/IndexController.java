package org.iii.esd.server.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class IndexController {

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    //    @PostMapping(value = "/resources/sr/{id}", produces = {MediaType.APPLICATION_XML_VALUE})
    //    public ResponseEntity<String> srAudio(@PathVariable("id") Long id) {
    //        String temp ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
    //        		"<Response>\r\n" +
    //        		"    <Play loop=\"2\">http://61.66.218.42/resources/audio/%d/sr_playback.mp3</Play>\r\n" +
    //        		"</Response>";
    //        return ResponseEntity.ok(String.format(temp,id));
    //    }

}