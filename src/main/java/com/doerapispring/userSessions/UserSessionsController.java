package com.doerapispring.userSessions;

import com.doerapispring.LoginForm;
import com.doerapispring.SignupForm;
import com.doerapispring.apiTokens.SessionToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Created by chiragtailor on 8/11/16.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class UserSessionsController {
    private UserSessionsService userSessionsService;

    @Autowired
    UserSessionsController(UserSessionsService userSessionsService) {
        this.userSessionsService = userSessionsService;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    SessionToken signup(@RequestBody SignupForm signupForm) {
        return userSessionsService.signup(signupForm.getIdentifier(), signupForm.getCredentials());
    }

    // Resource - session (CRUD)
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    SessionToken login(@RequestBody LoginForm loginForm) {
        return userSessionsService.login(loginForm.getUserIdentifier(), loginForm.getCredentials());
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    void logout(@AuthenticationPrincipal String userEmail) {
        // This request should probably short circuit somewhere earlier in the request handling
        // because we don't really care about the user or need to enter our domain in this scenario
        // This is only the case right now bc the session handling is stateful and stored in the database
        userSessionsService.logout(userEmail);
    }
}
