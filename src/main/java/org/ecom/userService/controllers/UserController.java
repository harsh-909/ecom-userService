package org.ecom.userService.controllers;

import org.ecom.userService.dto.SuccessDto;
import org.ecom.userService.dto.UserLoginDto;
import org.ecom.userService.dto.UserSignUpDto;
import org.ecom.userService.dto.UserTokenDto;
import org.ecom.userService.models.User;

import org.ecom.userService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(@Qualifier("selfUserService") UserService userService){
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(userService.getUserById(id),HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<User> addNewUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.addNewUser(user),HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateAUser(@PathVariable("id") Long id, @RequestBody User user) {
        return new ResponseEntity<>(userService.updateAUser(id,user),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteAUser(@PathVariable("id") Long id) {
         userService.deleteAUser(id);
         return new ResponseEntity<>(new SuccessDto("user deleted successfully"), HttpStatus.OK);
    }

    @GetMapping("/login")
    public ResponseEntity<UserTokenDto> Login(@RequestBody UserLoginDto userLoginDto){
        return new ResponseEntity<>(userService.login(userLoginDto), HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<SuccessDto> logout(@RequestHeader("Authorization") String token){
        userService.logout(token);
        return new ResponseEntity<>(new SuccessDto("User logged out successfully"), HttpStatus.OK);
    }

    @PostMapping("/signUp")
    public ResponseEntity<SuccessDto> signUp(@RequestBody UserSignUpDto userSignUpDto){
        userService.signUp(userSignUpDto);
        return new ResponseEntity<>(new SuccessDto("User signed up successfully"), HttpStatus.OK);
    }

    @GetMapping("/validateToken")
    public ResponseEntity<SuccessDto> validateToken(@RequestHeader("Authorization") String token){
        boolean isValid = userService.validateToken(token);
        if(isValid) return new ResponseEntity<>(new SuccessDto("Token is valid"), HttpStatus.OK);
        else return new ResponseEntity<>(new SuccessDto("Token is invalid"), HttpStatus.UNAUTHORIZED);
    }
}

