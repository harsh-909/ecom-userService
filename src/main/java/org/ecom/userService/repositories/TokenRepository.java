package org.ecom.userService.repositories;

import org.ecom.userService.models.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<UserToken,Long> {
    UserToken findByToken(String token);

    @Query("SELECT ut FROM UserToken ut JOIN FETCH ut.user u JOIN FETCH u.userRolesList WHERE ut.token = :token AND ut.deleted = :deleted")
    UserToken findByTokenAndDeleted(@Param("token") String token, @Param("deleted") boolean deleted);

}
