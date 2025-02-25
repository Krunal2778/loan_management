INSERT INTO taxpayers.user_status (status_id,status_name,status_type) VALUES
	 (0,'Suspended','USER'),
	 (1,'Active','USER'),
	 (2,'In-Active','USER'),
	 (1,'Active','BORROWER'),
	 (3,'Defaulter','BORROWER'),
	 (0,'Suspended','BORROWER'),
	 (1,'Approved','EMI'),
	 (2,'Pending','EMI'),
	 (3,'Failed','EMI'),
	 (4,'Foreclosed','EMI');
INSERT INTO taxpayers.user_status (status_id,status_name,status_type) VALUES
	 (1,'Approved','LOAN'),
	 (2,'Pending','LOAN'),
	 (3,'Rejected','LOAN'),
	 (4,'Defaulter','LOAN');
