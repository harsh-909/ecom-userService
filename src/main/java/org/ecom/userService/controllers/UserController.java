package org.ecom.userService.controllers;

import org.ecom.userService.dto.ExceptionDto;
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
    public ResponseEntity<ExceptionDto> deleteAUser(@PathVariable("id") Long id) {
         userService.deleteAUser(id);
         return new ResponseEntity<>(new ExceptionDto("user deleted successfully"),HttpStatus.OK);

     }
}

