package guru.springframework.juniemvc.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://example.com/problems/validation-error"));
        problem.setDetail("One or more fields have invalid values.");

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler({java.util.NoSuchElementException.class, NotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://example.com/problems/not-found"));
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
