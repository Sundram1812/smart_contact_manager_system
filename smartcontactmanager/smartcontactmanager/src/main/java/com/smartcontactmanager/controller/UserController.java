package com.smartcontactmanager.controller;

import com.smartcontactmanager.entities.Contact;
import com.smartcontactmanager.entities.MyUser;
import com.smartcontactmanager.helper.Message;
import com.smartcontactmanager.repository.ContactRepository;
import com.smartcontactmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @ModelAttribute
    public void addCommonData(Model model, Principal principal){

//        this principle class is use for getting username(gmail)
        String userName=principal.getName();
        MyUser user=userRepository.getMyUserByMyUserName(userName);
        model.addAttribute("user",user);
    }

    @GetMapping("/index")
    public String dashboard(Model model , Principal principal){

        model.addAttribute("title","User Home Page");
        System.out.println("this is dashboard method");
        return "normal/user_dashboard";
    }


//    open addform handler
    @GetMapping(value = "/add-contact")
    public String openAddContactForm(Model model,Principal principal){
        model.addAttribute("title","Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    @GetMapping("/contact-info")
    public String viewContact(Model model ,Principal principal){
        model.addAttribute("title","Contact Information");
        return "normal/view_contact";
    }

    @PostMapping("/process-contact")
    public String processContact(@Valid @ModelAttribute("contact") Contact contact,
                                 BindingResult bindingResult,
                                 @RequestParam("profileImage")MultipartFile multipartFile,
                                 Model model, Principal principal,
                                 HttpSession httpSession) throws Exception {



        try{
            if(bindingResult.hasErrors()){
                System.out.println("Validation failed");
                model.addAttribute("contact",contact);
                return "normal/add_contact_form" ;
            }

            String userName=principal.getName();
            MyUser user=userRepository.getMyUserByMyUserName(userName);

            contact.setUser(user);

            if(multipartFile.isEmpty()){
                System.out.println("photo not availble for this contact");
                contact.setImageUrl("contact.png");
            }else {
//                For making unique image name I will cancatenate my image name with contact mobile number
                String imageName=contact.getPhone()+"_"+multipartFile.getOriginalFilename();
                contact.setImageUrl(imageName);
                File file=new ClassPathResource("static/img").getFile();
                System.out.println("Photo uploaded in this folder : "+file.getAbsolutePath());
                Path path=Paths.get(file.getAbsolutePath() +File.separator+ imageName);
                Files.copy(multipartFile.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("photo uploaded to folder");
            }

            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("data added to data base");
            System.out.println("Data is :"+contact);
            //sending success message to page
            httpSession.setAttribute("message",new Message("Contact added successfully !! Add more...","success"));

        }catch (Exception e){
            System.out.println("Error : "+e.getMessage());
            //sending failure message to page
            httpSession.setAttribute("message",new Message("Something wents wrong !! try again..","danger"));

        }
        return "normal/add_contact_form";
    }


//    show contact handler

    @GetMapping("/show-contact/{pageNo}")
    public String showContacts(@PathVariable("pageNo") Integer pageNo , Model model , Principal principal){
        model.addAttribute("title","show user contact");

//        Now we have to send the user contact to our show_contact.html page
        String userName=principal.getName();
        MyUser user=userRepository.getMyUserByMyUserName(userName);

        Pageable pageable=PageRequest.of(pageNo,3);

        Page<Contact> contactList=this.contactRepository.findContactByMyUser(user.getId() , pageable);
        model.addAttribute("contact",contactList);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", contactList.getTotalPages());
        return "normal/show_contact";
    }


//    handler for particular contact information
    @GetMapping("/contact/{cId}")
    public String contact(@PathVariable("cId") Integer cId , Model model , Principal principal){

        Contact contact=contactRepository.findById(cId).get();

        String userName=principal.getName();
        MyUser user=userRepository.getMyUserByMyUserName(userName);
        if(user.getId()==contact.getUser().getId()){
            model.addAttribute("title",contact.getName());
            model.addAttribute("contactDetails", contact);
        }
        return "normal/contact_detail";
    }

//    Handler for deleting a particular contact
    @GetMapping("/delete_contact/{cId}")
    public String deleteContactById(@PathVariable Integer cId , Model model ,Principal principal , HttpSession httpSession){
        String userName=principal.getName();
        MyUser user=userRepository.getMyUserByMyUserName(userName);
        Contact contact=contactRepository.findById(cId).get();
//        verification
        if(user.getId()==contact.getUser().getId()){
            contactRepository.delete(contact);

            httpSession.setAttribute("message", new Message("contact deleted successfully","success"));
        }
        return "redirect:/user/show-contact/0";
    }


//Handler for update or Edit contact----------

    @PostMapping("/update_contact/{cId}")
    public String updateForm(@PathVariable Integer cId, Model model){
        model.addAttribute("title", "Update Contact");
        Contact contact=this.contactRepository.findById(cId).get();
        model.addAttribute("contact",contact);
        return "normal/update_form";
    }

//    Processing update contact ....................

    @PostMapping("/process_update")
    public String processUpdateContact(@Valid @ModelAttribute Contact contact,
                                       Model model, Principal principal,
                                       HttpSession httpSession ,
                                       @RequestParam("profileImage")MultipartFile multipartFile ,
                                       BindingResult bindingResult){

        try{

//            Old contact information
            Contact oldContactDetail=this.contactRepository.findById(contact.getcId()).get();

            if(bindingResult.hasErrors()){
                System.out.println("validation failed");
                model.addAttribute("contact",contact);
                return "normal/update_form";
            }

            if(multipartFile.isEmpty()){
                System.out.println("Image is not updated");
                contact.setImageUrl(oldContactDetail.getImageUrl());
            }else {
//              Deleting old images
                if(oldContactDetail.getImageUrl() !=null){
                    File deleteFile=new ClassPathResource("static/img").getFile();
                    File file1=new File(deleteFile,oldContactDetail.getImageUrl());
                    file1.delete();
                }

//              Saving new images
                    String imageName=contact.getPhone()+"_"+multipartFile.getOriginalFilename();
                    contact.setImageUrl(imageName);
                    System.out.println("image updated");
                    File file=new ClassPathResource("static/img").getFile();
                    Path path=Paths.get(file.getAbsolutePath()+File.separator+imageName);
                    Files.copy(multipartFile.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
            }


            String userName=principal.getName();
            MyUser user=userRepository.getMyUserByMyUserName(userName);
            contact.setUser(user);
            this.contactRepository.save(contact);
            System.out.println("contact updated successfully");
            model.addAttribute("contact",contact);
            httpSession.setAttribute("message", new Message("contact updated successfully...","success"));


        }catch(Exception e){
            httpSession.setAttribute("Something wents wrong!!","danger");
        }


        return "redirect:/user/contact/"+contact.getcId();
    }



//  Handler for your profile i.e User Profile
    @GetMapping("/myProfile")
    public String myProfile(Model model , Principal principal){


        try{
            MyUser user=userRepository.getMyUserByMyUserName(principal.getName());
            model.addAttribute("title","Profile Page");
            model.addAttribute("userDetails",user);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return "/normal/userProfile";
    }





}

