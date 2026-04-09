package org.ecom.userService.service;

import org.ecom.userService.dto.UserLoginDto;
import org.ecom.userService.dto.UserSignUpDto;
import org.ecom.userService.dto.UserTokenDto;
import org.ecom.userService.models.User;
import org.ecom.userService.models.UserToken;

import java.util.List;

public interface UserService {

    public String getUserNameById(Long id);

    public User getUserById(Long id);

    public List<User> getAllUsers();

    public User addNewUser(User user);

    public User updateAUser(Long id, User user);

    public void deleteAUser(Long id);

    public UserTokenDto login(UserLoginDto userLoginDto);

    public void logout(String token);

    public void signUp(UserSignUpDto userSignUpDto);

    public boolean validateToken(String token);

}