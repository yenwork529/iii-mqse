package org.iii.esd.auth.endpoint;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"/","/index"})
    public String index() {
        return "index";
    }

}