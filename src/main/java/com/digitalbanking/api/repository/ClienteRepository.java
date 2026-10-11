package com.digitalbanking.api.repository;

import com.digitalbanking.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

}
