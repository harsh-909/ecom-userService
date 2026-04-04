package org.ecom.userService.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionDto {

    public ExceptionDto(){    }

    public ExceptionDto(String message){
        this.message = message;
    }

    private String message;
    private String status;
}
