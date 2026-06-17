package com.fpo.vendas.repository;

import com.fpo.vendas.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    Optional<Client> findByCpf(String cpf);
}
