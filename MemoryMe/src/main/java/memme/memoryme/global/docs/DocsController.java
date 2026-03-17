package memme.memoryme.global.docs;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsController {

    @GetMapping("/v1/docs")
    public String docs() {
        return "redirect:/docs/index.html";
    }
}