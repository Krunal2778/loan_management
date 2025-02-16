package com.krunal.loan.controllers;

import com.krunal.loan.common.S3BucketUtils;
import com.krunal.loan.exception.TokenRefreshException;
import com.krunal.loan.models.*;
import com.krunal.loan.payload.request.LoginRequest;
import com.krunal.loan.payload.request.SignupRequest;
import com.krunal.loan.payload.request.TokenRefreshRequest;
import com.krunal.loan.payload.response.JwtResponse;
import com.krunal.loan.payload.response.MessageResponse;
import com.krunal.loan.payload.response.TokenRefreshResponse;
import com.krunal.loan.repository.BlacklistedTokenRepository;
import com.krunal.loan.repository.RoleRepository;
import com.krunal.loan.repository.UserRepository;
import com.krunal.loan.security.jwt.JwtUtils;
import com.krunal.loan.security.services.RefreshTokenService;
import com.krunal.loan.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String ROLE_NOT_FOUND_ERROR = "Error: Role is not found.";
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final S3BucketUtils bucketUtils3;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          RoleRepository roleRepository, PasswordEncoder encoder,
                          JwtUtils jwtUtils, RefreshTokenService refreshTokenService, S3BucketUtils bucketUtils3, BlacklistedTokenRepository blacklistedTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.bucketUtils3 = bucketUtils3;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Authenticating user with username: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = List.copyOf(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        logger.info("User authenticated successfully with username: {}", loginRequest.getUsername());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                userDetails.getUsername(), userDetails.getEmail(), roles));
    }

    @PostMapping("/adduser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Registering user with username: {}", signUpRequest.getUsername());

        if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.getUsername()))) {
            logger.warn("Username {} is already taken!", signUpRequest.getUsername());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequest.getEmail()))) {
            logger.warn("Email {} is already in use!", signUpRequest.getEmail());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }
        if (signUpRequest.getPassword().length() <  6) {
            logger.warn("Password for username {} is too short!", signUpRequest.getUsername());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Password must be at least 6 characters!"));
        }

        String filePath = null;
        if (signUpRequest.getBase64Image() != null) {
            filePath = bucketUtils3.uploadImageToS3Bucket(signUpRequest.getBase64Image());
            if (filePath.equals("Error")) {
                logger.error("Error uploading file to S3 for user with username: {}", signUpRequest.getUsername());
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Uploading file to S3"));
            }
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()), signUpRequest.getPhoneNo(), filePath);
        user.setAddUser(jwtUtils.getLoggedInUserDetails().getId());
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND_ERROR));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND_ERROR));
                        roles.add(adminRole);

                        break;
                    case "ROLE_MANAGER":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                                .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND_ERROR));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND_ERROR));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        logger.info("User registered successfully with username: {}", signUpRequest.getUsername());

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<TokenRefreshResponse> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        logger.info("Refreshing token for refresh token: {}", requestRefreshToken);

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    logger.info("Token refreshed successfully for refresh token: {}", requestRefreshToken);
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> {
                    logger.error("Refresh token is not in database: {}", requestRefreshToken);
                    return new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!");
                });
    }

    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> logoutUser(HttpServletRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (token != null) {
            // Blacklist the token
            blacklistedTokenRepository.deleteAll();
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(token);
            blacklistedTokenRepository.save(blacklistedToken);
        }
        refreshTokenService.deleteByUserId(userId);
        logger.info("User logged out successfully with user ID: {}", userId);
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
}