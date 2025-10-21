package ghoneimcaptures.gc.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ghoneimcaptures.gc.Model.User;
import ghoneimcaptures.gc.Repositories.UserRepository;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/GC")
public class UserController {
    @Autowired
    private UserRepository userRepository;


     @GetMapping("/Login")
    public ModelAndView Login() {
        ModelAndView mav = new ModelAndView("Login.html");
        return mav;
    }

    @PostMapping("/Login")
    public RedirectView loginprocess(@RequestParam ("email") String email,@RequestParam ("pass") String pass, HttpSession session) {
        User userAcc=this.userRepository.findByEmail(email);
        if(userAcc!=null)
        {
            // Boolean passwordsMatch=BCrypt.checkpw(pass,userAcc.getPassword());
            Boolean passwordsMatch=pass.equals(userAcc.getPassword());
            if(passwordsMatch)
            {
                session.setAttribute("email", userAcc.getEmail());
                session.setAttribute("Firstname", userAcc.getFirstname());
                session.setAttribute("Lastname", userAcc.getLastname());
                return new RedirectView("/GC/HomePage");
            }
            else {
                return new RedirectView("/GC/Login?error=incorrectPassword" + email);
            }
           
        
        }
        
        return new RedirectView("/GC/Login?error=userNotFound" + email);
    }

    
    @GetMapping("/HomePage")
    public ModelAndView getHomePage(HttpSession session) {
        ModelAndView mav = new ModelAndView("HomePage.html");
        mav.addObject("email", (String) session.getAttribute("email"));
        mav.addObject("Firstname", (String) session.getAttribute("Firstname"));
        return mav;
    }


    @GetMapping("/logout")
    public RedirectView logout(HttpSession session) {
        session.invalidate();
        return new RedirectView("/GC/Login");
    }
        
    
    }
    

