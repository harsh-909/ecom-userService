package org.ecom.userService.service;

import org.ecom.userService.exceptions.UserNotFoundException;
import org.ecom.userService.models.CustomGrantedAuthority;
import org.ecom.userService.models.CustomUserDetails;
import org.ecom.userService.models.User;
import org.ecom.userService.repositories.UserRepositiory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepositiory userRepositiory;

    public CustomUserDetailsService(UserRepositiory userRepositiory){
        this.userRepositiory = userRepositiory;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional =  userRepositiory.findByEmail(username);
        if(userOptional.isEmpty()){
            throw new UserNotFoundException("User with email " + username + " is not present");
        }
        return new CustomUserDetails(userOptional.get());
    }
}