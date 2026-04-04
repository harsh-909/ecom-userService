package org.ecom.userService.serviceImpl;

import org.ecom.userService.exceptions.UserNotFoundException;
import org.ecom.userService.models.User;
import org.ecom.userService.repositories.UserRepositiory;
import org.ecom.userService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("selfUserService")
public class UserServiceImpl implements UserService {

    private final UserRepositiory userRepositiory;

    @Autowired
    public UserServiceImpl(UserRepositiory userRepositiory){
        this.userRepositiory = userRepositiory;
    }

    @Override
    public String getUserNameById(Long id) {
        Optional<User> userOptional =  userRepositiory.findById(id);
        if(userOptional.isEmpty()) throw new UserNotFoundException("User with id " + id + " is not present");
        User user = userOptional.get();
        return user.getFirstName() + " " + user.getLastName();
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> userOptional =  userRepositiory.findById(id);
        if(userOptional.isEmpty()) throw new UserNotFoundException("User with id " + id + " is not present");
        return userOptional.get();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepositiory.findAll();
    }

    @Override
    public User addNewUser(User user) {
        Optional<User> userOptional = userRepositiory.findByEmail(user.getEmail());
        if(userOptional.isPresent()) throw new UserNotFoundException("User with email " + user.getEmail() + " is already present");
        return userRepositiory.save(user);
    }

    @Override
    public User updateAUser(Long id, User user) {
        Optional<User> userOptional =  userRepositiory.findById(id);
        if(userOptional.isEmpty()) throw new UserNotFoundException("User with id " + id + " is not present");

        if(user.getFirstName() != null) userOptional.get().setFirstName(user.getFirstName());
        if(user.getLastName() != null) userOptional.get().setLastName(user.getLastName());
        if(user.getEmail() != null) userOptional.get().setEmail(user.getEmail());
        if(user.getPhoneNumber() != null) userOptional.get().setPhoneNumber(user.getPhoneNumber());
        if(user.getAddress() != null) userOptional.get().setAddress(user.getAddress());
        return userRepositiory.save(userOptional.get());
    }

    @Override
    public void deleteAUser(Long id) {
        Optional<User> userOptional =  userRepositiory.findById(id);
        if(userOptional.isEmpty()) throw new UserNotFoundException("User with id " + id + " is not present");
        userRepositiory.deleteById(id);
    }
}
