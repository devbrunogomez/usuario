package com.brunogomes.usuario.infrastructure.repository;


import com.brunogomes.usuario.infrastructure.entity.Enderecos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnderecoRepository extends JpaRepository<Enderecos, Long> {
}
