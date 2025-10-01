package com.example.demo.Repository;

import com.example.demo.Entity.NCC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NCCRepository extends JpaRepository<NCC, Integer> {

}
