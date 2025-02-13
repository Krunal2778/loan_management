package com.krunal.loan.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "borrowers",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = "user_account"),
				@UniqueConstraint(columnNames = "email")
		})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrower {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long borrowerId;

	@NotBlank
	@Size(max = 20)
	private String userAccount;

	@Size(max = 50)
	private String name;

	@Size(max = 50)
	private String fatherName;

	@Size(max = 50)
	@Email
	private String email;

	@Size(max = 15)
	private String phoneNo;

	@Size(max = 300)
	private String address;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private Date dob;

	@Size(max = 300)
	private String notes;

	@Column(nullable = false)
	private Long status;

	@Transient
	private String statusName;

	@Column(nullable = false)
	private Long addUser;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	private Date addDate;

	private Long updatedUser;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDate;

	@OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<BorrowersFile> borrowersFiles = new HashSet<>();
}