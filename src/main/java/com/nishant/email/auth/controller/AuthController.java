package com.nishant.email.auth.controller;

import com.nishant.email.auth.io.AuthRequest;
import com.nishant.email.auth.io.AuthResponse;
import com.nishant.email.auth.io.ResetPasswordRequest;
import com.nishant.email.auth.service.AppUserDetailsService;
import com.nishant.email.auth.service.UserService;
import com.nishant.email.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
// injecting this to send reset otp email
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request){
        try{
            authenticate(request.getEmail(), request.getPassword());
    //pass this userdetails to jwt to create jwt token (util class)
            final UserDetails userDetails = appUserDetailsService.loadUserByUsername(request.getEmail());
            final String jwtToken = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie=ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 60 * 10) // 10 hours
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.ok()
                    .header("Set-Cookie", cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken));

        } catch (BadCredentialsException ex){
            Map<String, Object> error=new HashMap<>();
            error.put("error", true);
            error.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (DisabledException ex){
            Map<String, Object> error=new HashMap<>();
            error.put("error", true);
            error.put("message", "Account disabled");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }catch (Exception ex){
            Map<String, Object> error=new HashMap<>();
            error.put("error", true);
            error.put("message", "Authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return ResponseEntity.ok(email != null && !email.isEmpty());
    }

    @PostMapping("/reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        try{
            userService.sendResetOtp(email);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset OTP email", e);
        }
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        try{
            userService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset password", e);
        }
    }

    @PostMapping("/send-otp")
    public void sendVerifyOtp(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        try{
            userService.sendOtp(email);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP to email", e);
        }

    }

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String, Object> request,
                            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        if(request.get("otp") == null || request.get("otp").toString().isEmpty()) {
            throw new IllegalArgumentException("OTP is required");
        }
        try{
            userService.verifyOtp(email, request.get("otp").toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify email", e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response){
        ResponseCookie cookie=ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // set to true in production with HTTPS
                .path("/")
                .maxAge(0) // expire the cookie
                .sameSite("Strict")
                .build();
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body("Logged out successfully");
    }

}
