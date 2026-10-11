package com.digitalbanking.api.repository;

import com.digitalbanking.api.entity.Conta;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    boolean existsByNumero(String numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id = :id")
    Optional<Conta> findByIdWithLock(@Param("id") Long id);

}
