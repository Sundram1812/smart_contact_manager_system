package com.smartcontactmanager.controller;

import com.smartcontactmanager.entities.MyUser;
import com.smartcontactmanager.helper.Message;
import com.smartcontactmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("title","Home-smart contact manager");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model){
        model.addAttribute("title","About-smart contact manager");
        return "about";
    }

    @GetMapping("/signup")
    public String signup(Model model){
        model.addAttribute("title","Register-smart contact manager");
        model.addAttribute("user",new MyUser());
        return "signup";
    }

//    Handler for registring user
    @PostMapping("/do_register")
    public String registerUser(@Valid @ModelAttribute("user") MyUser user , BindingResult bindingResult  ,
                               @RequestParam(value = "agreement", defaultValue = "false") Boolean agreement ,
                               Model model , HttpSession session ,
                               @RequestParam("profileImage")MultipartFile multipartFile){

        try{
            if(!agreement){
                System.out.println("you have not agreed terms and conditions");
                throw new Exception("you have not agreed the terms and conditions");
            }

            if(bindingResult.hasErrors()){
                System.out.println("this is binding");
                model.addAttribute("user",user);
                return "signup";
            }

            if(! multipartFile.isEmpty()){
                File file=new ClassPathResource("static/img").getFile();
                Path path= Paths.get(file.getAbsolutePath()+File.separator+multipartFile.getOriginalFilename());
                Files.copy(multipartFile.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                user.setImageUrl(multipartFile.getOriginalFilename());
            }

            user.setId(100);
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

            System.out.println("agreement "+agreement);
            System.out.println("User "+user);

            MyUser result = userRepository.save(user);
            session.setAttribute("message",new Message("Successfully Registered!!","alert-success"));

            model.addAttribute("user",new MyUser());
            return "signup";
        }catch (Exception e){
            //e.printStackTrace();
            session.setAttribute("message",new Message("Something went wrong!!"+e.getMessage(), "alert-danger"));
            model.addAttribute("user",new MyUser());
            return "signup";
        }

    }

//    Handler for custom login
    @GetMapping("/signin")
    public String customLogin(Model model){
        model.addAttribute("title","Login Page");
        return "login";
    }
}
