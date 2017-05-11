package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class ListsController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private ListApiService listApiService;

    @Autowired
    ListsController(HateoasLinkGenerator hateoasLinkGenerator, ListApiService listApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApiService = listApiService;
    }

    @RequestMapping(value = "/lists/unlock", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> unlock(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            listApiService.unlock(authenticatedUser);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(
                    hateoasLinkGenerator.unlockListLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ListsResponse> index(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            List<ListDTO> listDTOs = listApiService.getAll(authenticatedUser);
            listDTOs.stream().forEach(listDTO ->
                    listDTO.add(hateoasLinkGenerator.listLink(listDTO.getName()).withRel("list")));
            ListsResponse listsResponse = new ListsResponse(listDTOs);
            listsResponse.add(hateoasLinkGenerator.listsLink().withSelfRel());
            return ResponseEntity.ok(listsResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/lists/{name}", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity show(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                        @PathVariable String name) {
        return null;
    }

    // TODO: Need to start treating the lists as their own resources
    // Lists can be locked or unlocked
    // When unlocked, they have a property indicating the time at which they will be locked again
    // When locked, they have a property indicating when they can be locked again
}