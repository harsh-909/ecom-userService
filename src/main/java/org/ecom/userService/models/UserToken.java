package org.ecom.userService.models;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class UserToken extends BaseModel{

    private String token;

    private String userEmail;

    private Date expiryTime;

    private String userRoles;

}
