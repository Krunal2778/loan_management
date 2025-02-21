package com.krunal.loan.controllers;

import com.krunal.loan.common.S3BucketUtils;
import com.krunal.loan.exception.*;
import com.krunal.loan.models.*;
import com.krunal.loan.payload.request.ChangePasswordRequest;
import com.krunal.loan.payload.request.UpdateRoleRequest;
import com.krunal.loan.payload.response.MessageResponse;
import com.krunal.loan.repository.RoleRepository;
import com.krunal.loan.repository.UserRepository;
import com.krunal.loan.repository.UserStatusRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/role")
public class UserRoleController {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);
    private static final String USER_NOT_FOUND_ERROR = "Error: User not found.";
    private static final String USER_NOT_FOUND_WITH_ID = "User not found with ID: {}";
    private static final String ROLE_NOT_FOUND_ERROR = "Error: Role is not found.";
    private static final String USER_STATUS_NOT_FOUND_ERROR = "Error: User status not found.";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final S3BucketUtils bucketUtils3;
    private final PasswordEncoder encoder;

    @Autowired
    public UserRoleController(RoleRepository roleRepository, UserRepository userRepository, UserStatusRepository userStatusRepository, S3BucketUtils bucketUtils3, PasswordEncoder encoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userStatusRepository = userStatusRepository;
        this.bucketUtils3 = bucketUtils3;
        this.encoder = encoder;
    }

    @GetMapping("/rolelist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getRoleList() {
        logger.info("Fetching role list");
        try {
            List<Role> roles = this.roleRepository.findAll();
            List<Role> activeRoles = roles.stream().filter(role -> role.getStatus() == 1).toList();
            return new ResponseEntity<>(activeRoles, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching role list", e);
            throw new UserListNotFoundException("Error: Role list not found.");
        }
    }

    @GetMapping("/userlist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUserList() {
        logger.info("Fetching user list");
        try {
            List<User> users = this.userRepository.findAll().stream().filter(user -> user.getStatus() != 0).toList();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching user list", e);
            throw new UserListNotFoundException("Error: User list not found.");
        }
    }

    @PutMapping("/update-user")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<User> updateUser(@Valid @RequestBody UpdateRoleRequest user) {
        logger.info("Updating user with username: {}", user.getUsername());
        try {
            Optional<User> userOptional = this.userRepository.findByUsername(user.getUsername());
            if (userOptional.isPresent()) {
                User users = userOptional.get();
                users.setEmail(user.getEmail());
                users.setPhoneNo(user.getPhoneNo());
                users.setStatus(user.getStatus());
                users.setName(user.getName());
                String filePath = null;
                if (user.getBase64Image() != null) {
                    filePath = bucketUtils3.uploadImageToS3Bucket(user.getBase64Image());
                    if (filePath.equals("Error")) {
                        logger.error("Error uploading file to S3 for user: {}", user.getUsername());
                        throw new FileUploadException("Error: Uploading file to S3");
                    }
                    users.setFilePath(filePath);
                }
                Set<Role> roles = new HashSet<>();
                Role userRole = switch (user.getRole()) {
                    case "ROLE_ADMIN" ->
                            roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_ERROR));
                    case "ROLE_MANAGER" ->
                            roleRepository.findByName(ERole.ROLE_MANAGER).orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_ERROR));
                    case "ROLE_USER" ->
                            roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RoleNotFoundException(ROLE_NOT_FOUND_ERROR));
                    default -> null;
                };
                roles.add(userRole);
                users.setRoles(roles);
                return new ResponseEntity<>(this.userRepository.save(users), HttpStatus.OK);
            } else {
                logger.warn(USER_NOT_FOUND_WITH_ID, user.getUsername());
                throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
            }
        } catch (Exception e) {
            logger.error("Error updating user with username: {}", user.getUsername(), e);
            throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
        }
    }


    @PutMapping("/activated-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> activatedUser(@Valid @PathVariable Long id) {
        logger.info("Activating user with ID: {}", id);
        try {
            Optional<User> userOptional = this.userRepository.findById(id);
            if (userOptional.isPresent()) {
                User users = userOptional.get();
                users.setStatus(UsersStatus.ACTIVE.getCode());
                return new ResponseEntity<>(this.userRepository.save(users), HttpStatus.OK);
            } else {
                logger.warn(USER_NOT_FOUND_WITH_ID, id);
                throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
            }
        } catch (Exception e) {
            logger.error("Error activating user with ID: {}", id, e);
            throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
        }
    }

    @GetMapping("/user-details/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserDetailsById(@PathVariable Long id) {
        logger.info("Fetching user details for ID: {}", id);
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            userOptional.ifPresent(user -> {
                if (user.getFilePath() != null) {
                    try {
                        user.setBase64Image(this.bucketUtils3.getFileFromS3(user.getFilePath()));
                    } catch (Exception e) {
                        logger.error("Error fetching file from S3 for user: {}", user.getUsername(), e);
                    }
                }
            });
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        } else {
            logger.warn(USER_NOT_FOUND_WITH_ID, id);
            throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
        }
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> changePassword( @RequestBody ChangePasswordRequest changePassword) {
        logger.info("Changing password for user with ID: {}", changePassword.getId());

        if (changePassword.getNewPassword().length() < 6) {
            logger.warn("Password for username {} is too short!", changePassword.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Password must be at least 6 characters!"));
        }

        try {
            Optional<User> userOptional = this.userRepository.findById(changePassword.getId());
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                user.setPassword(encoder.encode(changePassword.getNewPassword()));
                this.userRepository.save(user);
                return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
            } else {
                logger.warn(USER_NOT_FOUND_WITH_ID, changePassword.getId());
                throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
            }
        } catch (Exception e) {
            logger.error("Error changing password for user with ID: {}", changePassword.getId(), e);
            throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
        }
    }
}