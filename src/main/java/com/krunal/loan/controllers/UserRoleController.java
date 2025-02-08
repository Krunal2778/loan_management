package com.krunal.loan.controllers;

import com.krunal.loan.models.ERole;
import com.krunal.loan.models.Role;
import com.krunal.loan.models.User;
import com.krunal.loan.payload.request.UpdateRoleRequest;
import com.krunal.loan.repository.RoleRepository;
import com.krunal.loan.repository.UserRepository;
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

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/role")
public class UserRoleController {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;

	@Autowired
	public UserRoleController(RoleRepository roleRepository, UserRepository userRepository) {
	    this.roleRepository = roleRepository;
	    this.userRepository = userRepository;
	}

	@GetMapping("/rolelist")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<Role>> getRoleList() {
		try {
			return new ResponseEntity<>(this.roleRepository.findAll(), HttpStatus.OK);
		} catch (Exception e) {
			throw new RuntimeException("Error: Role list not found.");
		}

	}

	@GetMapping("/userlist")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<User>> getUserList() {
		try {
			return new ResponseEntity<>(this.userRepository.findAll(), HttpStatus.OK);
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
				Set<Role> roles = new HashSet<>();
				Role userRole = switch (user.getRole()) {
                    case "ROLE_ADMIN" ->
                            roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    case "ROLE_MODERATOR" ->
                            roleRepository.findByName(ERole.ROLE_MODERATOR).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
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

	@GetMapping("/userdetails/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> getUserDetailsById(@PathVariable Long id) {
		Optional<User> userOptional = this.userRepository.findById(id);
		if (userOptional.isPresent()) {
			return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
		} else {
			throw new RuntimeException("Error: User not found.");
		}
	}

}
