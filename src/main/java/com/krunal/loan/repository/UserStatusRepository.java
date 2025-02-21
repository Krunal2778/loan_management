package com.krunal.loan.repository;

import com.krunal.loan.models.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
//    @Query("SELECT us FROM UserStatus us WHERE us.statusId = :statusId AND us.statusType = :statusType")
//    Optional<UserStatus> findByIdAndStatusType(@Param("statusId") Long statusId, @Param("statusType") String statusType);
}
