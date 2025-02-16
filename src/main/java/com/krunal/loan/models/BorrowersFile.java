package com.krunal.loan.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "borrowers_file")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BorrowersFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "borrower_id", nullable = false)
	private Long borrowerId;

	@NotBlank
	private String filePath;

	@Column(length = 10)
	private String fileType;

	@Column(length = 1)
	private int status = 1;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "borrower_id", insertable = false, updatable = false)
	@JsonIgnore
	private Borrower borrower;
}