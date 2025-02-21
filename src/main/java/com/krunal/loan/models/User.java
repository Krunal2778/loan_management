package com.krunal.loan.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = "username"),
				@UniqueConstraint(columnNames = "email")
		})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 20)
	private String username;

	@Size(max = 50)
	private String name;

	@Size(max = 50)
	@Email
	private String email;

	@NotBlank
	@Size(max = 120)
	private String password;

	@Size(max = 500)
	private String filePath;

	@Size(max = 15)
	private String phoneNo;

	@Column(nullable = false)
	private Long status;

	@Transient
	private String statusName;

	@Transient
	private String base64Image;

	@Size(max = 10)
	private String partnerId;

	@Column(nullable = false)
	private Long addUser;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	private Date addDate;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	public User(String username, String email, String password, String phoneNo,String filePath, String name) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.phoneNo = phoneNo;
		this.status = UsersStatus.ACTIVE.getCode();
		this.addDate = new Date();
		this.filePath = filePath;
		this.name =name;
	}
	public String getStatusName() {
		UsersStatus usersStatus = UsersStatus.fromCode(this.status);
		return (usersStatus != null) ? usersStatus.getDisplayName() : "Unknown";
	}
}