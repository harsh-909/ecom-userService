package org.ecom.userService.dto;


import lombok.Getter;
import lombok.Setter;
import org.ecom.userService.models.UserRoles;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class UserTokenDto {

    private String userEmail;

    private Date expiryTime;

    private String userRolesList;

    private String token;
}
