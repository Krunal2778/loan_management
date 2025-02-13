package com.krunal.loan.controllers;

import com.krunal.loan.common.S3BucketUtils;
import com.krunal.loan.models.ERole;
import com.krunal.loan.models.Role;
import com.krunal.loan.models.User;
import com.krunal.loan.models.UserStatus;
import com.krunal.loan.payload.request.UpdateRoleRequest;
import com.krunal.loan.payload.response.MessageResponse;
import com.krunal.loan.repository.RoleRepository;
import com.krunal.loan.repository.UserRepository;
import com.krunal.loan.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/role")
public class UserRoleController {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final UserStatusRepository userStatusRepository;
	private final S3BucketUtils bucketUtils3;

	@Autowired
	public UserRoleController(RoleRepository roleRepository, UserRepository userRepository, UserStatusRepository userStatusRepository, S3BucketUtils bucketUtils3) {
	    this.roleRepository = roleRepository;
	    this.userRepository = userRepository;
        this.userStatusRepository = userStatusRepository;
        this.bucketUtils3 = bucketUtils3;
    }

	@GetMapping("/rolelist")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<Role>> getRoleList() {
		try {
			List<Role> roles = this.roleRepository.findAll();
			List<Role> activeRoles = roles.stream()
					.filter(role -> role.getStatus() == 1)
					.toList();
			return new ResponseEntity<>(activeRoles, HttpStatus.OK);
		} catch (Exception e) {
			throw new RuntimeException("Error: Role list not found.");
		}

	}

	@GetMapping("/userlist")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<User>> getUserList() {
		try {
			List<User> users = this.userRepository.findAll();
			users.forEach(user -> {
				UserStatus user3 = this.userStatusRepository.findById(user.getStatus())
			            .orElseThrow(() -> new RuntimeException("Error: User status not found."));
			    user.setStatusName(user3.getStatusName());
				if (user.getFilePath() != null) {
					try {
						user.setBase64Image(this.bucketUtils3.getFileFromS3(user.getFilePath()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return new ResponseEntity<>(users, HttpStatus.OK);
		} catch (Exception e) {
			throw new RuntimeException("Error: User list not found.");
		}

	}

	@PutMapping("/updateuser")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> updateUser(  @Valid @RequestBody UpdateRoleRequest user) {
		try {

			Optional<User> userOptional = this.userRepository.findByUsername(user.getUsername());
			if (userOptional.isPresent()) {
				User users = userOptional.get();
				users.setEmail(user.getEmail());
				users.setPhoneNo(user.getPhoneNo());
				String filePath = null;
				if (user.getBase64Image() != null){
					filePath = bucketUtils3.uploadImageToS3Bucket(user.getBase64Image());
					if (filePath.equals("Error")) {
						throw new RuntimeException("Error: Uploading file to S3");
					}
					users.setFilePath(filePath);
				}
				Set<Role> roles = new HashSet<>();
				Role userRole = switch (user.getRole()) {
                    case "ROLE_ADMIN" ->
                            roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    case "ROLE_MANAGER" ->
                            roleRepository.findByName(ERole.ROLE_MANAGER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    case "ROLE_USER" ->
                            roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    default -> null;
                };
                roles.add(userRole);
				users.setRoles(roles);
				return new ResponseEntity<>(this.userRepository.save(users), HttpStatus.OK);
			} else {
				throw new RuntimeException("Error: User not found.");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error: User  not found.");
		}

	}

	@PutMapping("/deleteuser/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> deleteUser(  @Valid @PathVariable Long id) {
		try {

			Optional<User> userOptional = this.userRepository.findById(id);
			if (userOptional.isPresent()) {
				User users = userOptional.get();
				users.setStatus(0L);
				return new ResponseEntity<>(this.userRepository.save(users), HttpStatus.OK);
			} else {
				throw new RuntimeException("Error: User not found.");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error: User  not found.");
		}

	}

	@PutMapping("/inactivateduser/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> inactivatedUser(  @Valid @PathVariable Long id) {
		try {

			Optional<User> userOptional = this.userRepository.findById(id);
			if (userOptional.isPresent()) {
				User users = userOptional.get();
				users.setStatus(2L);
				return new ResponseEntity<>(this.userRepository.save(users), HttpStatus.OK);
			} else {
				throw new RuntimeException("Error: User not found.");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error: User  not found.");
		}

	}

	@PutMapping("/activateduser/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> activatedUser(  @Valid @PathVariable Long id) {
		try {

			Optional<User> userOptional = this.userRepository.findById(id);
			if (userOptional.isPresent()) {
				User users = userOptional.get();
				users.setStatus(1L);
				return new ResponseEntity<>(this.userRepository.save(users), HttpStatus.OK);
			} else {
				throw new RuntimeException("Error: User not found.");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error: User  not found.");
		}

	}



	@GetMapping("/userdetails/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> getUserDetailsById(@PathVariable Long id) {
		Optional<User> userOptional = this.userRepository.findById(id);
		if (userOptional.isPresent()) {
			userOptional.ifPresent(user -> {
			    UserStatus userStatus = this.userStatusRepository.findById(user.getStatus())
			            .orElseThrow(() -> new RuntimeException("Error: User status not found."));
			    user.setStatusName(userStatus.getStatusName());
				if (user.getFilePath() != null) {
					try {
						user.setBase64Image(this.bucketUtils3.getFileFromS3(user.getFilePath()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
		} else {
			throw new RuntimeException("Error: User not found.");
		}
	}

}
