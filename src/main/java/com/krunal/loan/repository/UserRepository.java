package com.krunal.loan.repository;

import com.krunal.loan.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.partnerId = :partnerId WHERE u.id = :id")
  void updatePartnerIdById(Long id, String partnerId);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
  void updatePasswordById(Long id, String password);

}
