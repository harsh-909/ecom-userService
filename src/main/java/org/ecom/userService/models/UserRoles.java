package org.ecom.userService.models;


import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class UserRoles extends BaseModel{

    public UserRoles(){}

    public UserRoles(String name){
        this.name = name;
    }

    private String name;
    @ManyToMany(mappedBy = "userRolesList")
    private List<User> users;
}
