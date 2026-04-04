package org.ecom.userService.repositories;

import org.ecom.userService.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepositiory extends JpaRepository<User,Long> {

    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmail(String email);
}
