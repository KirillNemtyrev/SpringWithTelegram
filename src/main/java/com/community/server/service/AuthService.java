package com.community.server.service;

import com.community.server.body.SignInBody;
import com.community.server.body.SignUpBody;
import com.community.server.entity.RoleEntity;
import com.community.server.entity.RoleNameEntity;
import com.community.server.entity.UserEntity;
import com.community.server.exception.AppException;
import com.community.server.repository.RoleRepository;
import com.community.server.repository.UserRepository;
import com.community.server.security.JwtAuthenticationResponse;
import com.community.server.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public ResponseEntity<?> create(SignUpBody signUpBody){

        if(signUpBody.getUsername() == null || signUpBody.getEmail() == null || signUpBody.getName() == null || signUpBody.getPassword() == null){
            return new ResponseEntity<>("Auth options are not specified.", HttpStatus.BAD_GATEWAY);
        }

        if(!signUpBody.getUsername().matches("^[a-zA-Z\\d]+$")) {
            return new ResponseEntity<>("Invalid username!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(signUpBody.getUsername())) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpBody.getEmail())) {
            return new ResponseEntity<>("Email Address already in use!", HttpStatus.BAD_REQUEST);
        }

        if(!signUpBody.getPassword().matches("(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$")) {
            return new ResponseEntity<>("Wrong password format!", HttpStatus.BAD_REQUEST);
        }

        UserEntity userEntity = new UserEntity(
                signUpBody.getName(), signUpBody.getUsername(), signUpBody.getEmail(), passwordEncoder.encode(signUpBody.getPassword()));

        RoleEntity roleEntity = roleRepository.findByName(RoleNameEntity.ROLE_USER).orElseThrow(
                () -> new AppException("User Role not set."));

        userEntity.setRoles(Collections.singleton(roleEntity));

        userRepository.save(userEntity);
        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);

    }

    public ResponseEntity<?> auth(SignInBody signInBody) {

        if(signInBody.getUsername() == null || signInBody.getPassword() == null){
            return new ResponseEntity<>("Auth options are not specified.", HttpStatus.BAD_GATEWAY);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInBody.getUsername(), signInBody.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

}
