package com.krunal.loan.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "borrowers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrower  {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long borrowerId;

	@Column(length = 20, unique = true)
	private String userAccount;

	@Size(max = 50)
	private String name;

	@Size(max = 50)
	private String fatherName;

	@Size(max = 50)
	@Email
	private String email;

	@Size(min = 10, max = 15)
	private String phoneNo;

	@Size(max = 300)
	private String address;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")  // 💡 Fix
	private LocalDate dob;

	@Transient
	private String dobString;

	@Size(max = 300)
	private String notes;

	@NotNull
	private Long status;

	@Transient
	private String statusName;

	@NotNull
	private Long addUser;

	@Transient
	private String addUserName;

	@Transient
	private Integer noOfLoan;

	@Transient
	private Double totalLoanAmount;



	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime addDate;

	private Long updatedUser;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@UpdateTimestamp
	private LocalDateTime updatedDate;

	@OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<BorrowersFile> borrowersFiles = new HashSet<>();

	@OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Loan> loans;

	public void addBorrowersFile(BorrowersFile borrowersFile) {
		borrowersFiles.add(borrowersFile);
		borrowersFile.setBorrower(this);
	}

	public void removeBorrowersFile(BorrowersFile borrowersFile) {
		borrowersFiles.remove(borrowersFile);
		borrowersFile.setBorrower(null);
	}

	public String getStatusName() {
		BorrowerStatus borrowerStatus = BorrowerStatus.fromCode(this.status);
		return (borrowerStatus != null) ? borrowerStatus.getDisplayName() : "Unknown";
	}

	public String getDobString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		if(dob != null){
			return dob.format(formatter);
		}
		return null;
	}
}