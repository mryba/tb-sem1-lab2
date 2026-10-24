package com.marcinryba.tbsem1lab2.web;

import com.marcinryba.tbsem1lab2.config.LocalUrl;
import com.marcinryba.tbsem1lab2.identity.DatabaseUserDetailsService;
import com.marcinryba.tbsem1lab2.identity.RegistrationResult;
import com.marcinryba.tbsem1lab2.identity.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class AccountController {

    private final UserService userService;
    private final DatabaseUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    public AccountController(UserService userService,
                             DatabaseUserDetailsService userDetailsService,
                             SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/Account/Register")
    public String register(@ModelAttribute RegisterForm registerForm) {
        return "account/register";
    }

    @PostMapping("/Account/Register")
    public String register(@Valid @ModelAttribute RegisterForm registerForm, BindingResult bindingResult,
                           HttpServletRequest request, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "account/register";
        }

        RegistrationResult result = userService.register(registerForm.getEmail(), registerForm.getPassword());
        if (result.succeeded()) {
            signIn(result.user().getEmail(), request, response);
            return "redirect:/";
        }
        result.errors().forEach(error -> bindingResult.reject("registration.failed", error));
        return "account/register";
    }

    @GetMapping("/Account/Login")
    public String login(@RequestParam(required = false) String returnUrl, Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        model.addAttribute("returnUrl", LocalUrl.isLocal(returnUrl) ? returnUrl : null);
        return "account/login";
    }

    @GetMapping(path = "/Account/CheckLogin", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String checkLogin() {
        return "Zostałeś zalogowany.";
    }

    @RequestMapping("/Account/AccessDenied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public String accessDenied() {
        return "";
    }

    private void signIn(String email, HttpServletRequest request, HttpServletResponse response) {
        UserDetails principal = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());

        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
