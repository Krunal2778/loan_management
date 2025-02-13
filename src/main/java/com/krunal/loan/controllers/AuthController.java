package com.krunal.loan.controllers;

import com.krunal.loan.common.S3BucketUtils;
import com.krunal.loan.exception.TokenRefreshException;
import com.krunal.loan.models.ERole;
import com.krunal.loan.models.RefreshToken;
import com.krunal.loan.models.Role;
import com.krunal.loan.models.User;
import com.krunal.loan.payload.request.LoginRequest;
import com.krunal.loan.payload.request.SignupRequest;
import com.krunal.loan.payload.request.TokenRefreshRequest;
import com.krunal.loan.payload.response.JwtResponse;
import com.krunal.loan.payload.response.MessageResponse;
import com.krunal.loan.payload.response.TokenRefreshResponse;
import com.krunal.loan.repository.RoleRepository;
import com.krunal.loan.repository.UserRepository;
import com.krunal.loan.security.jwt.JwtUtils;
import com.krunal.loan.security.services.RefreshTokenService;
import com.krunal.loan.security.services.UserDetailsImpl;
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

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final String ROLE_NOT_FOUND_ERROR = "Error: Role is not found.";
  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder encoder;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;
  private final S3BucketUtils bucketUtils3;

  @Autowired
  public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                        RoleRepository roleRepository, PasswordEncoder encoder,
                        JwtUtils jwtUtils, RefreshTokenService refreshTokenService, S3BucketUtils bucketUtils3) {
      this.authenticationManager = authenticationManager;
      this.userRepository = userRepository;
      this.roleRepository = roleRepository;
      this.encoder = encoder;
      this.jwtUtils = jwtUtils;
      this.refreshTokenService = refreshTokenService;
      this.bucketUtils3 = bucketUtils3;
  }

  @PostMapping("/signin")
  public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    String jwt = jwtUtils.generateJwtToken(userDetails);

    List<String> roles = List.copyOf(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

    RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

    return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
        userDetails.getUsername(), userDetails.getEmail(), roles));
  }

  @PostMapping("/adduser")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.getUsername()))) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequest.getEmail()))) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }
    if (signUpRequest.getPassword().length() < 6) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Password must be at least 6 characters!"));
    }

    String filePath = null;
    if (signUpRequest.getBase64Image() != null){
      filePath = bucketUtils3.uploadImageToS3Bucket(signUpRequest.getBase64Image());
      if (filePath.equals("Error")) {
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

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }
               
  @PostMapping("/refreshtoken")
  public ResponseEntity<TokenRefreshResponse> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
    String requestRefreshToken = request.getRefreshToken();

    return refreshTokenService.findByToken(requestRefreshToken)
        .map(refreshTokenService::verifyExpiration)
        .map(RefreshToken::getUser)
        .map(user -> {
          String token = jwtUtils.generateTokenFromUsername(user.getUsername());
          return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
        })
        .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
            "Refresh token is not in database!"));
  }
  
  @PostMapping("/signout")
  public ResponseEntity<MessageResponse> logoutUser() {
    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Long userId = userDetails.getId();
    refreshTokenService.deleteByUserId(userId);
    return ResponseEntity.ok(new MessageResponse("Log out successful!"));
  }


}
