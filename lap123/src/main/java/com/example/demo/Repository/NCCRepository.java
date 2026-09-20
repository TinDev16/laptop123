package com.example.demo.Repository;

import com.example.demo.Entity.NCC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface NCCRepository extends JpaRepository<NCC, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from NCC item where item.MANCC = :id")
    Optional<NCC> findForDeletion(@Param("id") int id);

}
