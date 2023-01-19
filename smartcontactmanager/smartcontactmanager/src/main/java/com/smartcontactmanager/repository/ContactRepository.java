package com.smartcontactmanager.repository;

import com.smartcontactmanager.entities.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Integer> {

//    pegination
    @Query("from Contact as c where c.user.id =:userId")

//    This pageable object has two properties
//    1. page number
//    2. number of content per page

    public Page<Contact> findContactByMyUser(@Param("userId")int userId , Pageable pageable);

}
