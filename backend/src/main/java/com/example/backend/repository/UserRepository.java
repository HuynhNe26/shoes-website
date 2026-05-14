package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            select u from User u
            join u.membership m
            where upper(m.memberName) in :memberNames
            and u.email is not null
            """)
    List<User> findByMembershipNames(@Param("memberNames") Collection<String> memberNames);
}
