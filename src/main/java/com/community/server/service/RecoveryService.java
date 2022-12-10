package com.community.server.service;

import com.community.server.body.RecoveryBody;
import com.community.server.entity.UserEntity;
import com.community.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.Random;

@Service
public class RecoveryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MailService mailService;

    @Value("${app.resetExpirationInMs}")
    private int resetExpirationInMs;

    public ResponseEntity<?> code(RecoveryBody recoveryBody){

        if(recoveryBody.getUsernameOrEmail() == null){
            return new ResponseEntity<>("Recovery options are not specified.", HttpStatus.BAD_GATEWAY);
        }

        UserEntity userEntity = userRepository.findByUsernameOrEmail(recoveryBody.getUsernameOrEmail(), recoveryBody.getUsernameOrEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User is not found!"));

        userEntity.setRecoveryCode(new Random().ints(48, 122)
                .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
                .mapToObj(i -> (char) i)
                .limit(6)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString().toUpperCase());

        userEntity.setRecoveryCodeValid(new Date(new Date().getTime() + resetExpirationInMs));
        try {
            mailService.sendEmail(userEntity.getEmail(), "Восстановление учётной записи", "Ваш код - " + userEntity.getRecoveryCode());
        } catch (MessagingException e) {
            return new ResponseEntity<>("Unable to send message", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        userRepository.save(userEntity);
        return new ResponseEntity<>("A message with a recovery code has been sent!", HttpStatus.OK);
    }

    public ResponseEntity<?> passwordWithCode(RecoveryBody recoveryBody){
        if(recoveryBody.getUsernameOrEmail() == null || recoveryBody.getNewPassword() == null || recoveryBody.getCode() == null){
            return new ResponseEntity<>("Recovery options are not specified.", HttpStatus.BAD_GATEWAY);
        }

        if(recoveryBody.getCode().length() != 6){
            return new ResponseEntity<>("Wrong code format!", HttpStatus.BAD_REQUEST);
        }

        if(!recoveryBody.getNewPassword().matches("(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$"))
            return new ResponseEntity<>("Wrong password format!", HttpStatus.BAD_REQUEST);

        UserEntity userEntity = userRepository.findByUsernameOrEmail(recoveryBody.getUsernameOrEmail(), recoveryBody.getUsernameOrEmail()).orElseThrow(
                () -> new UsernameNotFoundException("User is not found!"));

        if(!recoveryBody.getCode().toUpperCase().equals(userEntity.getRecoveryCode())){
            return new ResponseEntity<>("Invalid code entered!", HttpStatus.BAD_REQUEST);
        }

        if(userEntity.getRecoveryCodeValid().before(new Date())){
            return new ResponseEntity<>("Code time is up!", HttpStatus.BAD_REQUEST);
        }

        userEntity.setPassword(passwordEncoder.encode(recoveryBody.getNewPassword()));
        userRepository.save(userEntity);

        return new ResponseEntity<>("Password recovered!", HttpStatus.OK);
    }
}
