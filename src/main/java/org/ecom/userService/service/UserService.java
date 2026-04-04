package org.ecom.userService.service;

import org.ecom.userService.models.User;

import java.util.List;

public interface UserService {

    public String getUserNameById(Long id);

    public User getUserById(Long id);

    public List<User> getAllUsers();

    public User addNewUser(User user);

    public User updateAUser(Long id, User user);

    public void deleteAUser(Long id);

}