package com.doerapispring.users;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenEntity;
import com.doerapispring.apiTokens.SessionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by chiragtailor on 8/11/16.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class UsersController {
    private UserService userService;
    private SessionTokenService sessionTokenService;

    @Autowired
    UsersController(UserService userService, SessionTokenService sessionTokenService) {
        this.userService = userService;
        this.sessionTokenService = sessionTokenService;
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    UserResponseWrapper create(@RequestBody UserRequestWrapper userRequestWrapper) {
        UserEntity userEntity = userRequestWrapper.getUser();
        User savedUser = userService.create(userEntity);
        SessionToken savedSessionToken = sessionTokenService.create(savedUser.id);
        return UserResponseWrapper.builder()
                .user(UserEntity.builder().email(savedUser.email).build())
                .sessionToken(SessionTokenEntity.builder().token(savedSessionToken.token).build())
                .build();
    }
}
