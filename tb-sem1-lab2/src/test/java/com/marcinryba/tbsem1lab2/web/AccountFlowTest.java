package com.marcinryba.tbsem1lab2.web;

import com.marcinryba.tbsem1lab2.identity.User;
import com.marcinryba.tbsem1lab2.identity.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountFlowTest {

    private static final String EMAIL = "student@example.test";
    private static final String PASSWORD = "Abcdef1!";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    private long existingUsers;

    @BeforeEach
    void countExistingUsers() {
        existingUsers = users.count();
    }

    @Test
    void protectedPageRedirectsAnonymousToLoginWithReturnUrl() throws Exception {
        mvc.perform(get("/Account/CheckLogin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/Account/Login?returnUrl=%2FAccount%2FCheckLogin"));
    }

    @Test
    void emptyRegistrationFormShowsMessagesAndCreatesNoAccount() throws Exception {
        mvc.perform(register("", "", "").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/register"))
                .andExpect(model().attributeHasFieldErrorCode("registerForm", "email", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("registerForm", "password", "NotBlank"))
                .andExpect(model().attributeHasFieldErrorCode("registerForm", "confirmPassword", "NotBlank"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Podaj adres e-mail.")));
        assertThat(users.count()).isEqualTo(existingUsers);
    }

    @Test
    void invalidEmailAndMismatchedPasswordsAreRejected() throws Exception {
        mvc.perform(register("nie-email", PASSWORD, "Inne1234!").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerForm", "email", "confirmPassword"));
        assertThat(users.count()).isEqualTo(existingUsers);
    }

    @Test
    void weakPasswordsAreRejectedByTheForm() throws Exception {
        mvc.perform(register(EMAIL, "abcdefgh1!", "abcdefgh1!").with(csrf()))
                .andExpect(model().attributeHasFieldErrors("registerForm", "password"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("dużą literę")));
        mvc.perform(register(EMAIL, "Abcdefgh!", "Abcdefgh!").with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("cyfrę")));
        mvc.perform(register(EMAIL, "Abcdefgh1", "Abcdefgh1").with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("znak specjalny")));
        assertThat(users.count()).isEqualTo(existingUsers);
    }

    @Test
    void successfulRegistrationLogsTheUserIn() throws Exception {
        MvcResult result = mvc.perform(register(EMAIL, PASSWORD, PASSWORD).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

        mvc.perform(get("/Account/CheckLogin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Zostałeś zalogowany."));
        mvc.perform(get("/").session(session))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Wyloguj")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Rejestracja</a>"))));

        User saved = users.findByEmail(EMAIL).orElseThrow();
        assertThat(saved.getPasswordHash()).doesNotContain(PASSWORD);
    }

    @Test
    void anonymousNavigationOffersLoginAndRegistration() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Rejestracja")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Logowanie")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Wyloguj"))));
    }

    @Test
    void logoutThenLoginAgainWorksAndBadCredentialsAreRejected() throws Exception {
        registerUser();

        MvcResult ok = mvc.perform(login(EMAIL, PASSWORD, null).with(csrf()))
                .andExpect(redirectedUrl("/"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) ok.getRequest().getSession();
        mvc.perform(post("/Account/Logout").session(session).with(csrf()))
                .andExpect(redirectedUrl("/"));
        mvc.perform(get("/Account/CheckLogin").session(session))
                .andExpect(status().is3xxRedirection());

        assertLoginRejected(EMAIL, "Zle-haslo1!");
        assertLoginRejected("nieistnieje@example.test", PASSWORD);
    }

    @Test
    void failedLoginShowsGenericMessageAndKeepsEmail() throws Exception {
        registerUser();
        MvcResult failed = mvc.perform(login(EMAIL, "Zle-haslo1!", null).with(csrf()))
                .andExpect(redirectedUrl("/Account/Login"))
                .andReturn();

        mvc.perform(get("/Account/Login").session((MockHttpSession) failed.getRequest().getSession()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Logowanie nie powiodło się.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(EMAIL)))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Zle-haslo1!"))));
    }

    @Test
    void emptyLoginFieldsGetFieldMessages() throws Exception {
        MvcResult failed = mvc.perform(login("", "", null).with(csrf())).andReturn();
        mvc.perform(get("/Account/Login").session((MockHttpSession) failed.getRequest().getSession()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Podaj adres e-mail.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Podaj hasło.")));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        registerUser();
        mvc.perform(register("STUDENT@example.test", PASSWORD, PASSWORD).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("już istnieje")));
        assertThat(users.count()).isEqualTo(existingUsers + 1);
    }

    @Test
    void returnUrlIsHonouredOnlyWhenLocal() throws Exception {
        registerUser();
        mvc.perform(login(EMAIL, PASSWORD, "/Account/CheckLogin").with(csrf()))
                .andExpect(redirectedUrl("/Account/CheckLogin"));
        mvc.perform(login(EMAIL, PASSWORD, "https://evil.example/").with(csrf()))
                .andExpect(redirectedUrl("/"));
        mvc.perform(login(EMAIL, PASSWORD, "//evil.example").with(csrf()))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void accountIsLockedAfterFiveFailuresEvenForTheCorrectPassword() throws Exception {
        registerUser();
        for (int i = 0; i < 5; i++) {
            assertLoginRejected(EMAIL, "Zle-haslo1!");
        }

        assertLoginRejected(EMAIL, PASSWORD);
        assertThat(users.findByEmail(EMAIL).orElseThrow().getLockedUntil()).isAfter(Instant.now());

        User user = users.findByEmail(EMAIL).orElseThrow();
        user.setLockedUntil(Instant.now().minusSeconds(1));
        users.flush();
        mvc.perform(login(EMAIL, PASSWORD, null).with(csrf()))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void postsWithoutCsrfTokenAreRejected() throws Exception {
        mvc.perform(register(EMAIL, PASSWORD, PASSWORD))
                .andExpect(status().isForbidden());
        mvc.perform(login(EMAIL, PASSWORD, null))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginFormRendersAntiForgeryToken() throws Exception {
        mvc.perform(get("/Account/Login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
    }

    @Test
    void veryLongPasswordOnLoginDoesNotBreakTheServer() throws Exception {
        registerUser();
        mvc.perform(login(EMAIL, "A1!" + "x".repeat(200), null).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/Account/Login"));
    }

    private void registerUser() throws Exception {
        mvc.perform(register(EMAIL, PASSWORD, PASSWORD).with(csrf())).andExpect(redirectedUrl("/"));
    }

    private void assertLoginRejected(String email, String password) throws Exception {
        mvc.perform(login(email, password, null).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/Account/Login"));
    }

    private MockHttpServletRequestBuilder register(String email, String password, String confirm) {
        return post("/Account/Register")
                .param("email", email)
                .param("password", password)
                .param("confirmPassword", confirm);
    }

    private MockHttpServletRequestBuilder login(String email, String password, String returnUrl) {
        MockHttpServletRequestBuilder request = post("/Account/Login")
                .param("email", email)
                .param("password", password);
        return returnUrl == null ? request : request.param("returnUrl", returnUrl);
    }
}
