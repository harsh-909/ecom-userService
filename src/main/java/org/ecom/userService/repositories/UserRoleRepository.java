package org.ecom.userService.repositories;

import org.ecom.userService.models.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoles,Long> {

    public UserRoles findByName (String role);
}
