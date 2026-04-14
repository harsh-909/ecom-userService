package org.ecom.userService.mapper;

import org.ecom.userService.dto.UserDto;
import org.ecom.userService.dto.UserTokenDto;
import org.ecom.userService.models.User;
import org.ecom.userService.models.UserRoles;
import org.ecom.userService.models.UserToken;

import java.util.List;

public class TokenToTokenDto {

    public static UserTokenDto convertUserTokenToTokenDto(UserToken userToken){
        UserTokenDto userTokenDto = new UserTokenDto();
        userTokenDto.setToken(userToken.getToken());
        userTokenDto.setUserDto(convertUserToUserDto(userToken.getUser()));
        return userTokenDto;
    }

    private static UserDto convertUserToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setAddress(user.getAddress());
        userDto.setVerified(user.isVerified());
        List<String> roles = user.getUserRolesList().stream()
                .map(UserRoles::getName)
                .toList();
        userDto.setRoles(roles);
        return userDto;
    }
}
