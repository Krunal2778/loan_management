package com.krunal.loan.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "borrowers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrower {
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

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date dob;

	@Size(max = 300)
	private String notes;

	@NotNull
	private Long status;

	@Transient
	private String statusName;

	@NotNull
	private Long addUser;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	private Date addDate;

	private Long updatedUser;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDate;

	@OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<BorrowersFile> borrowersFiles = new HashSet<>();

	public void addBorrowersFile(BorrowersFile borrowersFile) {
		borrowersFiles.add(borrowersFile);
		borrowersFile.setBorrower(this);
	}

	public void removeBorrowersFile(BorrowersFile borrowersFile) {
		borrowersFiles.remove(borrowersFile);
		borrowersFile.setBorrower(null);
	}
}