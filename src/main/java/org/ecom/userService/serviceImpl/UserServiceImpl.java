package org.ecom.userService.serviceImpl;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.core.instrument.config.validate.Validated;
import org.ecom.userService.dto.UserLoginDto;
import org.ecom.userService.dto.UserSignUpDto;
import org.ecom.userService.dto.UserTokenDto;
import org.ecom.userService.exceptions.InvalidTokenException;
import org.ecom.userService.exceptions.UserNotFoundException;
import org.ecom.userService.mapper.TokenToTokenDto;
import org.ecom.userService.models.User;
import org.ecom.userService.models.UserRoles;
import org.ecom.userService.models.UserToken;
import org.ecom.userService.repositories.TokenRepository;
import org.ecom.userService.repositories.UserRepositiory;
import org.ecom.userService.repositories.UserRoleRepository;
import org.ecom.userService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Primary
@Service("selfUserService")
public class UserServiceImpl implements UserService {

    private final UserRepositiory userRepositiory;
    private final TokenRepository tokenRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    public UserServiceImpl(UserRepositiory userRepositiory,
                           TokenRepository tokenRepository,
                           UserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder){
        this.userRepositiory = userRepositiory;
        this.tokenRepository = tokenRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = HexFormat.of().parseHex(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
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

    @Override
    public UserTokenDto login(UserLoginDto userLoginDto) {
        String userEmail = userLoginDto.getEmail();

        Optional<User> userOptional = userRepositiory.findByEmail(userEmail);
        if(userOptional.isEmpty()) throw new UserNotFoundException("Incorrect User Email or password");

        String bcryptEncodedPassword = userOptional.get().getPassword();
        if(!passwordEncoder.matches(userLoginDto.getPassword(), bcryptEncodedPassword))
            throw new UserNotFoundException("Incorrect User Email or password");

        String roles = String.join(",", userOptional.get().getUserRolesList().stream().map(UserRoles::getName).toList());
        Date expiry = new Date(System.currentTimeMillis() + 3600000 * 24);

        String jwt = Jwts.builder()
                .subject(userEmail)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();

        UserToken userToken = new UserToken();
        userToken.setToken(jwt);
        userToken.setUser(userOptional.get());
        tokenRepository.save(userToken);

        return TokenToTokenDto.convertUserTokenToTokenDto(userToken);
    }

    @Override
    public void logout(String token) {
        if (token.startsWith("Bearer ")) token = token.substring(7);
        Optional<UserToken> userTokenOptional = Optional.ofNullable(tokenRepository.findByTokenAndDeleted(token, false));
        if(userTokenOptional.isEmpty()) throw new UserNotFoundException("Invalid token");
        UserToken userToken = userTokenOptional.get();
        userToken.setDeleted(true);
        tokenRepository.save(userToken);
    }

    @Override
    public UserTokenDto validateToken(String token) throws InvalidTokenException {
        if (token.startsWith("Bearer ")) token = token.substring(7);

        // 1. Verify JWT signature and expiry
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e) {
            throw new InvalidTokenException("Token not valid");
        }

        // 2. Check token hasn't been logged out (soft-deleted in DB)
        Optional<UserToken> userTokenOptional = Optional.ofNullable(tokenRepository.findByTokenAndDeleted(token, false));
        return userTokenOptional.map(TokenToTokenDto::convertUserTokenToTokenDto).orElse(null);
    }

    @Override
    public void signUp(UserSignUpDto userSignUpDto) {
        Optional<User> userOptional = userRepositiory.findByEmail(userSignUpDto.getEmail());
        if(userOptional.isPresent()) throw new UserNotFoundException("User with email " + userSignUpDto.getEmail() + " is already present");
        User user = new User();
        user.setEmail(userSignUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(userSignUpDto.getPassword()));
        user.setFirstName(userSignUpDto.getFirstName());
        user.setLastName(userSignUpDto.getLastName());
        user.setPhoneNumber(userSignUpDto.getPhoneNumber());
        user.setAddress(userSignUpDto.getAddress());
        user.setVerified(false);
        List<UserRoles> userRolesList = new ArrayList<>();
        if(!userSignUpDto.getRole().isEmpty()){
            Optional<UserRoles> userRolesOptional = Optional.ofNullable(userRoleRepository.findByName(userSignUpDto.getRole()));
            if(userRolesOptional.isEmpty()) userRoleRepository.save(new UserRoles(userSignUpDto.getRole()));
            userRolesList.add(userRoleRepository.findByName(userSignUpDto.getRole()));
        }
        user.setUserRolesList(userRolesList);
        userRepositiory.save(user);
    }
}
