package com.smartcontactmanager.repository;

import com.smartcontactmanager.entities.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<MyUser,Integer>{

    @Query("select u from MyUser u where u.email = :email")
    public MyUser getMyUserByMyUserName(@Param("email") String email);
}
