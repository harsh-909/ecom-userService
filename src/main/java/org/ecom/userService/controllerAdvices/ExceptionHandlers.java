package org.ecom.userService.controllerAdvices;

import org.ecom.userService.dto.ExceptionDto;
import org.ecom.userService.exceptions.InvalidTokenException;
import org.ecom.userService.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleUserNotFoundException(
            UserNotFoundException exception
    ){
        ExceptionDto exceptionDto = new ExceptionDto();
        exceptionDto.setMessage(exception.getMessage());

        return new ResponseEntity<>(exceptionDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ExceptionDto> handleInvalidTokenException(
            InvalidTokenException exception
    ) {
        ExceptionDto exceptionDto = new ExceptionDto();
        exceptionDto.setMessage(exception.getMessage());

        return new ResponseEntity<>(exceptionDto, HttpStatus.UNAUTHORIZED);
    }
}
