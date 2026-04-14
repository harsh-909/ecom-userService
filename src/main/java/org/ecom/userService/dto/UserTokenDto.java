package org.ecom.userService.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTokenDto {

    private String token;

    private UserDto userDto;
}
