package org.ecom.userService.repositories;

import org.ecom.userService.models.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<UserToken,Long> {
    UserToken findByToken(String token);

    UserToken findByTokenAndIsDeleted(String token, boolean isDeleted);

}
