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

    private final org.springframework.context.MessageSource messageSource;

    GlobalExceptionHandler(org.springframework.context.MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        String title = messageSource.getMessage("error.validation", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
        String detail = messageSource.getMessage("error.validation.detail", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
        problem.setTitle(title);
        problem.setType(URI.create("https://example.com/problems/validation-error"));
        problem.setDetail(detail);

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
        String title = messageSource.getMessage("error.notFound", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
        problem.setTitle(title);
        problem.setType(URI.create("https://example.com/problems/not-found"));
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler({IllegalStateException.class})
    ProblemDetail handleConflict(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        String title = messageSource.getMessage("error.conflict", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
        problem.setTitle(title);
        problem.setType(URI.create("https://example.com/problems/conflict"));
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        String title = messageSource.getMessage("error.internal", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
        String detail = messageSource.getMessage("error.internal.detail", null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
        problem.setTitle(title);
        problem.setType(URI.create("https://example.com/problems/internal-error"));
        problem.setDetail(detail);
        return problem;
    }
}
