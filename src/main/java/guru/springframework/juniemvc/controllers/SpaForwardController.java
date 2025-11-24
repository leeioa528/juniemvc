package guru.springframework.juniemvc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA forward controller to serve index.html for client-side routes in production.
 * Excludes API and Actuator endpoints and does not interfere with static asset files (containing a dot).
 */
@Controller
class SpaForwardController {

    // Root path
    @GetMapping("")
    String index() { return "forward:/index.html"; }

    // Single-segment paths excluding api and actuator
//    @GetMapping({
//            "/{path:^(?!api$|actuator$).*$}",
//            // Multi-segment paths (no file extension) excluding api/actuator as first segment
//            "/{path:^(?!api$|actuator$).*$}/**/{rest:[^\\.]*}"
//    })
//    String forward() {
//        return "forward:/index.html";
//    }
}
