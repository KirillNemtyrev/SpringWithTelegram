package com.community.server.service;

import com.community.server.dto.ProfileDto;
import com.community.server.entity.UserEntity;
import com.community.server.repository.UserRepository;
import com.community.server.security.JwtAuthenticationFilter;
import com.community.server.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    public ProfileDto getProfile(HttpServletRequest httpServletRequest){
        Long user = jwtTokenProvider.getUserIdFromJWT(jwtAuthenticationFilter.getJwtFromRequest(httpServletRequest));

        UserEntity userEntity = userRepository.findById(user).orElseThrow(
                () -> new UsernameNotFoundException("User is not found!"));

        return new ProfileDto(userEntity.getUsername(), userEntity.getBalance());
    }

}
