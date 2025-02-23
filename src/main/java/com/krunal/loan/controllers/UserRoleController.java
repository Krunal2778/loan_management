package com.krunal.loan.controllers;

import com.krunal.loan.common.S3BucketUtils;
import com.krunal.loan.exception.*;
import com.krunal.loan.models.*;
import com.krunal.loan.payload.request.ChangePasswordRequest;
import com.krunal.loan.payload.request.UpdateRoleRequest;
import com.krunal.loan.payload.response.ContributorSummary;
import com.krunal.loan.payload.response.MessageResponse;
import com.krunal.loan.payload.response.PartnerDetail;
import com.krunal.loan.payload.response.PartnerList;
import com.krunal.loan.repository.LoanContributorRepository;
import com.krunal.loan.repository.LoanRepository;
import com.krunal.loan.repository.RoleRepository;
import com.krunal.loan.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/role")
public class UserRoleController {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);
    private static final String USER_NOT_FOUND_ERROR = "Error: User not found.";
    private static final String USER_NOT_FOUND_WITH_ID = "User not found with ID: {}";
    private static final String ROLE_NOT_FOUND_ERROR = "Error: Role is not found.";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final S3BucketUtils bucketUtils3;
    private final PasswordEncoder encoder;
    private final LoanContributorRepository contributorRepository;
    private final LoanRepository loanRepository;

    @Autowired
    public UserRoleController(RoleRepository roleRepository, UserRepository userRepository, S3BucketUtils bucketUtils3, PasswordEncoder encoder, LoanContributorRepository contributorRepository, LoanRepository loanRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.bucketUtils3 = bucketUtils3;
        this.encoder = encoder;
        this.contributorRepository = contributorRepository;
        this.loanRepository = loanRepository;
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
    public ResponseEntity<PartnerList> getUserList() {
        logger.info("Fetching user list");
        PartnerList partnerList = new PartnerList();
        try {
            List<User> users = this.userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<User> activeUsers = new ArrayList<>();
            for (User user : users) {
                Date date = user.getAddDate();
                String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                user.setJoinDate(formattedDate);
                activeUsers.add(user);

                ContributorSummary contributorSummary = getContributorSummary(user.getId());
                if (contributorSummary != null) {
                    user.setInvestedAmount(contributorSummary.getInvestedAmount());
                    user.setNetProfitAmount(contributorSummary.getNetProfitAmount());
                    user.setTotalAmount(contributorSummary.getTotalAmount());
                    user.setNoOfLoanInvested(contributorSummary.getNoOfLoans());
                } else {
                    user.setInvestedAmount(0.0);
                    user.setNetProfitAmount(0.0);
                    user.setTotalAmount(0.0);
                    user.setNoOfLoanInvested(0L);
                }
            }
            partnerList.setUserList(activeUsers);

            return new ResponseEntity<>(partnerList, HttpStatus.OK);
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
                Set<String> strRoles = user.getRole();
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

    @DeleteMapping("/delete-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        try {
            Optional<User> userOptional = this.userRepository.findById(id);
            if (userOptional.isPresent()) {
                this.userRepository.deleteById(id);
                return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
            } else {
                logger.warn(USER_NOT_FOUND_WITH_ID, id);
                throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
            }
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}", id, e);
            throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
        }
    }
    @GetMapping("/user-details/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartnerDetail> getUserDetailsById(@PathVariable Long id) {
        logger.info("Fetching user details for ID: {}", id);
        PartnerDetail partnerDetail =new PartnerDetail();
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            userOptional.ifPresent(user -> {
                Date date = user.getAddDate();
                String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(date);
                user.setJoinDate(formattedDate);

                if (user.getFilePath() != null) {
                    try {
                        user.setBase64Image(this.bucketUtils3.getFileFromS3(user.getFilePath()));
                    } catch (Exception e) {
                        logger.error("Error fetching file from S3 for user: {}", user.getUsername(), e);
                    }
                }
                ContributorSummary contributorSummary = getContributorSummary(user.getId());
                if (contributorSummary != null) {
                    user.setInvestedAmount(contributorSummary.getInvestedAmount());
                    user.setNetProfitAmount(contributorSummary.getNetProfitAmount());
                    user.setTotalAmount(contributorSummary.getTotalAmount());
                    user.setNoOfLoanInvested(contributorSummary.getNoOfLoans());
                } else {
                    user.setInvestedAmount(0.0);
                    user.setNetProfitAmount(0.0);
                    user.setTotalAmount(0.0);
                    user.setNoOfLoanInvested(0L);
                }
            });

            partnerDetail.setUser(userOptional.get());

            List<LoanContributor> contributorList = contributorRepository.findByContributorId(id);
            contributorList.forEach(lb -> loanRepository.findById(lb.getLoanId()).ifPresent(loan -> {
                lb.setLoanAccount(loan.getLoanAccount());
                lb.setLoanDuration(loan.getLoanDuration());
            }));
            partnerDetail.setContributorList(contributorList);
            return new ResponseEntity<>(partnerDetail, HttpStatus.OK);
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

    public ContributorSummary getContributorSummary(Long contributorId) {
        List<Object[]> results = contributorRepository.findContributorSummaryByContributorId(contributorId);
        if (results != null && !results.isEmpty()) {
            Object[] result = results.getFirst();
            Double investedAmount = result[0] != null ? ((Number) result[0]).doubleValue() : 0.0;
            Double netProfitAmount = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
            Long noOfLoans = result[2] != null ? ((Number) result[2]).longValue() : 0L;
            Double totalAmount = investedAmount + netProfitAmount;

            return new ContributorSummary(investedAmount, netProfitAmount, totalAmount, noOfLoans);
        }
        return null;
    }
}