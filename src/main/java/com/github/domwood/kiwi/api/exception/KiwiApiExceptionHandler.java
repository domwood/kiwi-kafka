package com.github.domwood.kiwi.api.exception;

import com.github.domwood.kiwi.data.error.ApiError;
import com.github.domwood.kiwi.data.error.ImmutableApiError;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class KiwiApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final Integer MAX_DEPTH = 30;

    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<ApiError> handleRuntimeError(RuntimeException ex, WebRequest request) {
        Throwable rootCause = discoverCause(ex, 0);

        if(rootCause instanceof UnknownTopicOrPartitionException){
            return handleUnknownTopicError((UnknownTopicOrPartitionException)rootCause, request);
        }

        ApiError error = ImmutableApiError.builder()
                .error(ex.getClass().getName())
                .rootCause(rootCause.getClass().getName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnknownTopicOrPartitionException.class)
    public final ResponseEntity<ApiError> handleUnknownTopicError(UnknownTopicOrPartitionException ex, WebRequest request) {

        ApiError error = ImmutableApiError.builder()
                .error(ex.getClass().getName())
                .rootCause(ex.getClass().getName())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    private Throwable discoverCause(Throwable ex, int depth){
        if(depth >= MAX_DEPTH){
            return ex;
        }
        if(ex.getCause() == null){
            return ex;
        }
        else if(ex.getCause().getClass().getName().equals(ex.getClass().getName())){
            return ex;
        }
        else{
            return this.discoverCause(ex.getCause(), depth++);
        }
    }
}
