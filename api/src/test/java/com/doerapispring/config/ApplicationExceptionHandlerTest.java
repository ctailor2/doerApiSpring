package com.doerapispring.config;

import com.doerapispring.domain.DomainException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Locale;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationExceptionHandlerTest {

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("fieldCodeA", Locale.getDefault(), "message from source for field code A");
        messageSource.addMessage("fieldCodeB", Locale.getDefault(), "message with arguments {0} and {1} from source for field code B");
        messageSource.addMessage("globalCodeA", Locale.getDefault(), "message from source for global code A");
        messageSource.addMessage("globalCodeB", Locale.getDefault(), "message with arguments {0} and {1} from source for global code B");

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new ApplicationExceptionHandler(messageSource))
            .build();
    }

    @Test
    public void includesFieldErrors_withoutArgs() throws Exception {
        mockMvc.perform(post("/testController/validateRequest")
            .content("{}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field", equalTo("fieldA")))
            .andExpect(jsonPath("$.fieldErrors[0].message", equalTo("message from source for field code A")));
    }

    @Test
    public void includesFieldErrors_withArgs() throws Exception {
        mockMvc.perform(post("/testController/validateRequest")
            .content("{}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[1].field", equalTo("fieldB")))
            .andExpect(jsonPath("$.fieldErrors[1].message", equalTo("message with arguments arg1 and arg2 from source for field code B")));
    }

    @Test
    public void includesFieldErrors_usingADefaultMessage_whenASpecificMessageIsNotDefined() throws Exception {
        mockMvc.perform(post("/testController/validateRequest")
            .content("{}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[2].field", equalTo("fieldC")))
            .andExpect(jsonPath("$.fieldErrors[2].message", equalTo("value was rejected")));
    }

    @Test
    public void includesGlobalErrors_withoutArgs() throws Exception {
        mockMvc.perform(post("/testController/validateRequest")
            .content("{}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.globalErrors[0].message", equalTo("message from source for global code A")));
    }

    @Test
    public void includesGlobalErrors_withArgs() throws Exception {
        mockMvc.perform(post("/testController/validateRequest")
            .content("{}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.globalErrors[1].message", equalTo("message with arguments arg1 and arg2 from source for global code B")));
    }

    @Test
    public void includesGlobalErrors_usingADefaultMessage_whenASpecificMessageIsNotDefined() throws Exception {
        mockMvc.perform(post("/testController/validateRequest")
            .content("{}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.globalErrors[2].message", equalTo("an error occurred")));
    }

    @Test
    public void includesGlobalErrors_forApplicationExceptions_usingTheSpecifiedMessageAndResponseStatus() throws Exception {
        mockMvc.perform(get("/testController/applicationExceptionWithMessageAndStatus"))
            .andExpect(status().isIAmATeapot())
            .andExpect(jsonPath("$.globalErrors[0].message", equalTo("An api error occurred")));
    }

    @Test
    public void includesGlobalErrors_forApplicationExceptions_usingADefaultMessageAndResponseStatus() throws Exception {
        mockMvc.perform(get("/testController/applicationException"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.globalErrors[0].message", equalTo("an error occurred")));
    }

    @Test
    public void includesGlobalErrors_forDomainExceptions_usingTheSpecifiedMessage() throws Exception {
        mockMvc.perform(get("/testController/domainExceptionWithMessageAndStatus"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.globalErrors[0].message", equalTo("An api error occurred")));
    }

    @Test
    public void includesGlobalErrors_forDomainExceptions_usingADefaultMessageAndResponseStatus() throws Exception {
        mockMvc.perform(get("/testController/domainException"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.globalErrors[0].message", equalTo("an error occurred")));
    }

    @RestController
    @RequestMapping("/testController")
    private static class TestController {
        @InitBinder
        public void initBinder(WebDataBinder webDataBinder) {
            webDataBinder.setValidator(new TestValidator());
        }

        @PostMapping("/validateRequest")
        public void validateRequest(@Valid @RequestBody TestRequest testRequest) {}

        @GetMapping("/applicationExceptionWithMessageAndStatus")
        public ResponseEntity applicationExceptionWithMessageAndStatus() {
            throw new TestApplicationException("An api error occurred");
        }

        @GetMapping("/applicationException")
        public ResponseEntity applicationException() {
            throw new ApplicationException();
        }

        @GetMapping("/domainExceptionWithMessageAndStatus")
        public ResponseEntity domainExceptionWithMessageAndStatus() {
            throw new TestDomainException("An api error occurred");
        }

        @GetMapping("/domainException")
        public ResponseEntity domainException() {
            throw new DomainException();
        }
    }

    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    private static class TestApplicationException extends ApplicationException {
        TestApplicationException(String message) {
            super(message);
        }
    }

    private static class TestDomainException extends DomainException {
        TestDomainException(String message) {
            super(message);
        }
    }

    private static class TestRequest {
        private final String fieldA;
        private final String fieldB;
        private final String fieldC;

        @JsonCreator
        private TestRequest(
            @JsonProperty("fieldA") String fieldA,
            @JsonProperty("fieldB") String fieldB,
            @JsonProperty("fieldC") String fieldC) {
            this.fieldA = fieldA;
            this.fieldB = fieldB;
            this.fieldC = fieldC;
        }

        public String getFieldA() {
            return fieldA;
        }

        public String getFieldB() {
            return fieldB;
        }

        public String getFieldC() {
            return fieldC;
        }
    }

    private static class TestValidator implements Validator {
        @Override
        public boolean supports(Class<?> clazz) {
            return clazz.equals(TestRequest.class);
        }

        @Override
        public void validate(Object target, Errors errors) {
            ValidationUtils.rejectIfEmpty(errors, "fieldA", "fieldCodeA");
            ValidationUtils.rejectIfEmpty(errors, "fieldB", "fieldCodeB", new Object[]{"arg1", "arg2"}, "unused default");
            ValidationUtils.rejectIfEmpty(errors, "fieldC", "fieldCodeC");
            if (errors.hasErrors()) {
                errors.reject("globalCodeA");
                errors.reject("globalCodeB", new Object[]{"arg1", "arg2"}, "unused default");
                errors.reject("globalCodeC");
            }
        }
    }
}