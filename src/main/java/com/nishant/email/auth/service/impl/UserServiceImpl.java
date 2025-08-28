package com.nishant.email.auth.service.impl;

import com.nishant.email.auth.entities.UserEntity;
import com.nishant.email.auth.io.UserRequest;
import com.nishant.email.auth.io.UserResponse;
import com.nishant.email.auth.repository.UserRepository;
import com.nishant.email.auth.service.EmailService;
import com.nishant.email.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//sned reset otp email
    private final EmailService emailService;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
       UserEntity newUser =convertToUserEntity(userRequest);
       if(!userRepository.existsByEmail(userRequest.getEmail())){
           userRepository.save(newUser);
           return convertToUserResponse(newUser);
       }
       throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public UserResponse getUser(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return convertToUserResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        //Generate 6 digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        //calculate expiry time (5 minutes from now)
        long expiryTime=System.currentTimeMillis() + (5 * 60 * 1000);

        //update the user
        existingUser.setResetOtp(otp);
        existingUser.setResetOtpExpiredAt(expiryTime);

        //save into the DB
        userRepository.save(existingUser);

        try{
            //TODO: send the reset otp email
            emailService.sendResetOtpEmail(existingUser.getEmail(), otp);

        }catch (Exception e){
            throw new RuntimeException("Failed to send reset OTP email", e);
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Check if OTP is valid
        if(existingUser.getResetOtp() ==null || !existingUser.getResetOtp().equals(otp)){
            throw new RuntimeException("Invalid OTP");
        }
        // Check if OTP has expired
        if(System.currentTimeMillis() > existingUser.getResetOtpExpiredAt()){
            throw new RuntimeException("OTP has expired");
        }
        // Update the password
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null); // Clear the OTP after successful reset
        existingUser.setResetOtpExpiredAt(0L); // Clear the expiry time

        userRepository.save(existingUser);

    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Generate 6 digit OTP
        if(existingUser.getIsAccountVerified() !=null && existingUser.getIsAccountVerified()){
            return;
        }
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        //calculate expiry time (24 hours from the time generated)
        long expiryTime=System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        //update the user entity
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpiredAt(expiryTime);

        //save it into DB
        userRepository.save(existingUser);
        try{
            emailService.sendOtpEmail(existingUser.getEmail(), otp);
        }catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if(existingUser.getVerifyOtp() ==null || !existingUser.getVerifyOtp().equals(otp)){
            throw new RuntimeException("Invalid OTP");
        }

        // Check if OTP has expired
        if(System.currentTimeMillis() > existingUser.getVerifyOtpExpiredAt()){
            throw new RuntimeException("OTP has expired");
        }

        // Mark the account as verified
        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null); // Clear the OTP after successful verification
        existingUser.setVerifyOtpExpiredAt(0L); // Clear the expiry time

        // Save the updated user entity
        userRepository.save(existingUser);
    }

    private UserEntity convertToUserEntity(UserRequest userRequest) {
        return UserEntity.builder()
                .email(userRequest.getEmail())
                .userId(java.util.UUID.randomUUID().toString())
                .name(userRequest.getName())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpiredAt(0L)
                .verifyOtp(null)
                .verifyOtpExpiredAt(0L)
                .resetOtp(null)
                .build();
    }

    private UserResponse convertToUserResponse(UserEntity newUser) {
        return UserResponse.builder()
                .name(newUser.getName())
                .email(newUser.getEmail())
                .userId(newUser.getUserId())
                .isAccountVerified(newUser.getIsAccountVerified())
                .build();
    }
}
