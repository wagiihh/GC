package ghoneimcaptures.gc.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/landing")
public class LandingController {
    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("LandingPage.html");
        return modelAndView;
    }
    @GetMapping("/projects")
    public ModelAndView projects() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Projects.html");
        return modelAndView;
    }
    @GetMapping("/contact")
    public ModelAndView contact() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Contact.html");
        return modelAndView;
    }
    @GetMapping("/about")
    public ModelAndView about() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("About.html");
        return modelAndView;
    }
}
