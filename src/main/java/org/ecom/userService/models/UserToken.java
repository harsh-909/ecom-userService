package org.ecom.userService.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserToken extends BaseModel{

    @Column(length = 2048)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
