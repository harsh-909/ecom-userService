package org.ecom.userService.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuccessDto {

    public SuccessDto(){    }

    public SuccessDto(String message){
        this.message = message;
    }

    private String message;
    private String status;
}
