package com.nishant.email.auth.service;

import com.nishant.email.auth.io.UserRequest;
import com.nishant.email.auth.io.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    UserResponse getUser(String email);

    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String newPassword);

    void sendOtp(String email);

    void verifyOtp(String email, String otp);

}
