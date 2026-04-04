package org.ecom.userService.models;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User extends BaseModel{

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
}
