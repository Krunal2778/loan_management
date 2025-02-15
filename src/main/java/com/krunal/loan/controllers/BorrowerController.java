package com.krunal.loan.controllers;

import com.krunal.loan.common.S3BucketUtils;
import com.krunal.loan.exception.BorrowerNotFoundException;
import com.krunal.loan.exception.FileUploadException;
import com.krunal.loan.exception.UserStatusNotFoundException;
import com.krunal.loan.models.Borrower;
import com.krunal.loan.models.BorrowersFile;
import com.krunal.loan.models.UserStatus;
import com.krunal.loan.payload.request.BorrowerRequest;
import com.krunal.loan.payload.response.MessageResponse;
import com.krunal.loan.repository.BorrowerRepository;
import com.krunal.loan.repository.BorrowersFileRepository;
import com.krunal.loan.repository.UserStatusRepository;
import com.krunal.loan.security.jwt.JwtUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/borrower")
public class BorrowerController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowerController.class);
    private static final String USER_STATUS_NOT_FOUND_ERROR = "Error: User status not found.";
    private static final String BORROWER_STATUS_TYPE = "BORROWER";
    private static final String BORROWER_NOT_FOUND_LOG = "Borrower with ID {} not found";
    private static final String BORROWER_NOT_FOUND_ERROR = "Error: Borrower not found";

    private final BorrowerRepository borrowerRepository;
    private final JwtUtils jwtUtils;
    private final S3BucketUtils bucketUtils3;
    private final UserStatusRepository userStatusRepository;
    private final BorrowersFileRepository borrowersFileRepository;

    public BorrowerController(BorrowerRepository borrowerRepository, JwtUtils jwtUtils, S3BucketUtils bucketUtils3, UserStatusRepository userStatusRepository, BorrowersFileRepository borrowersFileRepository) {
        this.borrowerRepository = borrowerRepository;
        this.jwtUtils = jwtUtils;
        this.bucketUtils3 = bucketUtils3;
        this.userStatusRepository = userStatusRepository;
        this.borrowersFileRepository = borrowersFileRepository;
    }

    @PostMapping("/addborrower")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<MessageResponse> registerBorrower(@Valid @RequestBody BorrowerRequest borrowerRequest) {
        logger.info("Registering borrower with email: {}", borrowerRequest.getEmail());

        if (Boolean.TRUE.equals(borrowerRepository.existsByEmail(borrowerRequest.getEmail()))) {
            logger.warn("Email {} is already in use!", borrowerRequest.getEmail());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        Set<String> stringBase64Image = borrowerRequest.getBase64Image();
        Set<BorrowersFile> base64Images = new HashSet<>();

        Borrower borrower = getBorrower(borrowerRequest);
        borrower.setStatus(1L); // 1 for active
        borrower.setAddUser(jwtUtils.getLoggedInUserDetails().getId());

        // Set the userAccount using a temporary value
        borrower.setUserAccount("TEMP");

        borrower = borrowerRepository.save(borrower); // Save the borrower to generate the borrowerId
        logger.info("Borrower saved with ID: {}", borrower.getBorrowerId());

        if (stringBase64Image == null) {
            logger.warn("No image provided for borrower with email: {}", borrowerRequest.getEmail());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No image provided"));
        } else {
            Borrower finalBorrower = borrower;
            stringBase64Image.forEach(base64Image -> {
                try {
                    // Validate Base64 string
                    Base64.getDecoder().decode(base64Image);

                    BorrowersFile borrowersFile = new BorrowersFile();
                    String filePath = bucketUtils3.uploadImageToS3Bucket(base64Image);
                    if (filePath.equals("Error")) {
                        logger.error("Error uploading file to S3 for borrower with email: {}", borrowerRequest.getEmail());
                        throw new FileUploadException("Error: Uploading file to S3");
                    }

                    borrowersFile.setFilePath(filePath);
                    borrowersFile.setFileType("Borrower");
                    borrowersFile.setStatus(1);
                    borrowersFile.setBorrowerId(finalBorrower.getBorrowerId()); // Set the borrowerId
                    base64Images.add(borrowersFile);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid Base64 input for borrower with email: {}", borrowerRequest.getEmail(), e);
                    throw new FileUploadException("Error: Invalid Base64 input");
                }
            });
        }

        if (!base64Images.isEmpty()) {
            base64Images.forEach(borrower::addBorrowersFile);
        }

        // Set the userAccount using the borrowerId formatted to 7 digits
        borrower.setUserAccount(String.format("OD-%07d", borrower.getBorrowerId()));
        borrowerRepository.save(borrower); // Save the borrower again with the updated userAccount
        logger.info("Borrower user account set to: {}", borrower.getUserAccount());

        return ResponseEntity.ok(new MessageResponse("Borrower registered successfully!"));
    }

    @PutMapping("/updateborrower/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateBorrower(@PathVariable Long id, @Valid @RequestBody BorrowerRequest borrowerRequest) {
        logger.info("Updating borrower with ID: {}", id);

        Optional<Borrower> borrowerOptional = borrowerRepository.findById(id);
        if (borrowerOptional.isEmpty()) {
            logger.warn(BORROWER_NOT_FOUND_LOG, id);
            throw new BorrowerNotFoundException(BORROWER_NOT_FOUND_ERROR);
        }

        Borrower borrower = borrowerOptional.get();
        borrower.setName(borrowerRequest.getName());
        borrower.setFatherName(borrowerRequest.getFatherName());
        borrower.setEmail(borrowerRequest.getEmail());
        borrower.setPhoneNo(borrowerRequest.getPhoneNo());
        borrower.setAddress(borrowerRequest.getAddress());
        borrower.setNotes(borrowerRequest.getNotes());
        borrower.setDob(borrowerRequest.getDob());

        Set<BorrowersFile> base64Images = getBorrowersFiles(id, borrowerRequest);

        borrower.setStatus(1L); // 1 for active
        borrower.setUpdatedUser(jwtUtils.getLoggedInUserDetails().getId());
        if (!base64Images.isEmpty()) {
            borrower.setBorrowersFiles(base64Images);
            borrowersFileRepository.deleteByBorrowerId(id);
        }

        borrowerRepository.save(borrower);
        logger.info("Borrower with ID {} updated successfully", id);

        return ResponseEntity.ok(new MessageResponse("Borrower updated successfully!"));
    }

    private Set<BorrowersFile> getBorrowersFiles(Long id, BorrowerRequest borrowerRequest) {
        Set<String> stringBase64Image = borrowerRequest.getBase64Image();
        Set<BorrowersFile> base64Images = new HashSet<>();

        if (stringBase64Image != null) {
            stringBase64Image.forEach(base64Image -> {
                BorrowersFile borrowersFile = new BorrowersFile();
                String filePath = bucketUtils3.uploadImageToS3Bucket(base64Image);
                if (filePath.equals("Error")) {
                    logger.error("Error uploading file to S3 for borrower with ID: {}", id);
                    throw new FileUploadException("Error: Uploading file to S3");
                }
                borrowersFile.setBorrowerId(id);
                borrowersFile.setFilePath(filePath);
                borrowersFile.setFileType("Borrower");
                borrowersFile.setStatus(1);
                base64Images.add(borrowersFile);
            });
        }
        return base64Images;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Borrower> getBorrowerById(@PathVariable Long id) {
        logger.info("Fetching borrower with ID: {}", id);

        Optional<Borrower> borrowerOptional = borrowerRepository.findById(id);
        if (borrowerOptional.isEmpty()) {
            logger.warn(BORROWER_NOT_FOUND_LOG, id);
            throw new BorrowerNotFoundException(BORROWER_NOT_FOUND_ERROR);
        }

        borrowerOptional.ifPresent(borrower -> {
            UserStatus userStatus = this.userStatusRepository.findByIdAndStatusType(borrower.getStatus(), BORROWER_STATUS_TYPE)
                    .orElseThrow(() -> new UserStatusNotFoundException(USER_STATUS_NOT_FOUND_ERROR));
            borrower.setStatusName(userStatus.getStatusName());
            if (borrower.getBorrowersFiles() != null) {
                borrowerOptional.get().getBorrowersFiles().forEach(borrowersFile -> {
                    try {
                        borrowersFile.setFilePath(this.bucketUtils3.getFileFromS3(borrowersFile.getFilePath()));
                    } catch (Exception e) {
                        logger.error("Error fetching file from S3 for borrower: {}", borrowerOptional.get().getUserAccount(), e);
                    }
                });
            }
        });
        return new ResponseEntity<>(borrowerOptional.get(), HttpStatus.OK);

    }

    @GetMapping("/borrowerlist")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Borrower>> getAllBorrowers() {
        logger.info("Fetching all borrowers");
        List<Borrower> borrowers = borrowerRepository.findAll();
        borrowers.forEach(borrower -> {
            UserStatus userStatus = this.userStatusRepository.findByIdAndStatusType(borrower.getStatus(), BORROWER_STATUS_TYPE)
                    .orElseThrow(() -> new UserStatusNotFoundException(USER_STATUS_NOT_FOUND_ERROR));
            borrower.setStatusName(userStatus.getStatusName());
        });
        return ResponseEntity.ok(borrowers);
    }


    @GetMapping("/defaulterslist")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Borrower>> getDefaulterBorrowers() {
        logger.info("Fetching all borrowers with defaulter status");

        List<Borrower> defaulterBorrowers = borrowerRepository.findByStatus(3L);
        defaulterBorrowers.forEach(borrower -> {
            UserStatus userStatus = this.userStatusRepository.findByIdAndStatusType(borrower.getStatus(), BORROWER_STATUS_TYPE)
                    .orElseThrow(() -> new UserStatusNotFoundException(USER_STATUS_NOT_FOUND_ERROR));
            borrower.setStatusName(userStatus.getStatusName());
        });
        return ResponseEntity.ok(defaulterBorrowers);
    }


    @PutMapping("/{id}/suspended")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> suspendedBorrower(@PathVariable Long id) {
        logger.info("Disapproving borrower with ID: {}", id);

        Optional<Borrower> borrowerOptional = borrowerRepository.findById(id);
        if (borrowerOptional.isEmpty()) {
            logger.warn(BORROWER_NOT_FOUND_LOG, id);
            throw new BorrowerNotFoundException(BORROWER_NOT_FOUND_ERROR);
        }

        Borrower borrower = borrowerOptional.get();
        borrower.setStatus(0L); // 0 for Suspended
        borrower.setUpdatedUser(jwtUtils.getLoggedInUserDetails().getId());

        borrowerRepository.save(borrower);
        logger.info("Borrower with ID {} disapproved successfully", id);

        return ResponseEntity.ok(new MessageResponse("Borrower approved successfully!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteBorrower(@PathVariable Long id) {
        logger.info("Deleting borrower with ID: {}", id);

        Optional<Borrower> borrowerOptional = borrowerRepository.findById(id);
        if (borrowerOptional.isEmpty()) {
            logger.warn(BORROWER_NOT_FOUND_LOG, id);
            throw new BorrowerNotFoundException(BORROWER_NOT_FOUND_ERROR);
        }

        borrowerRepository.deleteById(id);
        logger.info("Borrower with ID {} deleted successfully", id);

        return ResponseEntity.ok(new MessageResponse("Borrower deleted successfully!"));
    }

    @PutMapping("/{id}/defaulter")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> markBorrowerAsDefaulter(@PathVariable Long id) {
        logger.info("Marking borrower with ID: {} as defaulter", id);

        Optional<Borrower> borrowerOptional = borrowerRepository.findById(id);
        if (borrowerOptional.isEmpty()) {
            logger.warn(BORROWER_NOT_FOUND_LOG, id);
            throw new BorrowerNotFoundException(BORROWER_NOT_FOUND_ERROR);
        }

        Borrower borrower = borrowerOptional.get();
        borrower.setStatus(3L); // 3 for defaulter
        borrower.setUpdatedUser(jwtUtils.getLoggedInUserDetails().getId());

        borrowerRepository.save(borrower);
        logger.info("Borrower with ID {} marked as defaulter successfully", id);

        return ResponseEntity.ok(new MessageResponse("Borrower marked as defaulter successfully!"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Borrower>> searchBorrowers(@RequestParam String keyword) {
        logger.info("Searching borrowers with keyword: {}", keyword);

        List<Borrower> borrowers = borrowerRepository.searchBorrowers(keyword);
        borrowers.forEach(borrower -> {
            UserStatus userStatus = this.userStatusRepository.findByIdAndStatusType(borrower.getStatus(), BORROWER_STATUS_TYPE)
                    .orElseThrow(() -> new UserStatusNotFoundException(USER_STATUS_NOT_FOUND_ERROR));
            borrower.setStatusName(userStatus.getStatusName());
        });
        return ResponseEntity.ok(borrowers);
    }

    private Borrower getBorrower(BorrowerRequest borrowerRequest) {
        Borrower borrower = new Borrower();
        borrower.setName(borrowerRequest.getName());
        borrower.setFatherName(borrowerRequest.getFatherName());
        borrower.setEmail(borrowerRequest.getEmail());
        borrower.setPhoneNo(borrowerRequest.getPhoneNo());
        borrower.setAddress(borrowerRequest.getAddress());
        borrower.setNotes(borrowerRequest.getNotes());
        borrower.setDob(borrowerRequest.getDob());
        return borrower;
    }
}