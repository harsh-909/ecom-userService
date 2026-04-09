package org.ecom.userService.mapper;

import org.ecom.userService.dto.UserTokenDto;
import org.ecom.userService.models.UserToken;

public class TokenToTokenDto {

    public static UserTokenDto convertUserTokenToTokenDto(UserToken userToken){
        UserTokenDto userTokenDto = new UserTokenDto();
        userTokenDto.setUserEmail(userTokenDto.getUserEmail());
        userTokenDto.setExpiryTime(userToken.getExpiryTime());
        userTokenDto.setUserRolesList(userToken.getUserRoles());
        userTokenDto.setToken(userToken.getToken());
        return userTokenDto;
    }
}
