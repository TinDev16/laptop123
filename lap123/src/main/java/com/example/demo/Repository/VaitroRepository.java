package com.example.demo.Repository;

import com.example.demo.Entity.Vaitro;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaitroRepository extends JpaRepository<Vaitro, Integer> {
    Optional<Vaitro> findByMaVaitro(String maVaitro);
}