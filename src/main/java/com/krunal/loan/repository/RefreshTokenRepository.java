package com.krunal.loan.repository;

import com.krunal.loan.models.RefreshToken;
import com.krunal.loan.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByUser(User user);
  Optional<RefreshToken> findByToken(String token);
  int deleteByUser(User user);
}
