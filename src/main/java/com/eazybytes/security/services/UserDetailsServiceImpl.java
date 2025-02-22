package com.eazybytes.security.services;

import com.eazybytes.model.User;
import com.eazybytes.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional // Used to manage database transaction. Method level, ensures it follows ACID properties
    // If an exception occurs, the transaction is rolled back, preventing partial updates. Only committed
    // when everything runs successfully. Helps prevent data corruption by rolling

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);


    }
}

// @Transactional ->Ensures database operations inside loadUserByUsername() are completed in one transaction.
// Prevents LazyInitializationException when fetching related entities (like roles) in Hibernate.
// Convert User into UserDetails
// UserDetailsImpl.build(user) is a factory method that converts a User entity into a UserDetailsImpl object.
// UserDetailsImpl is our custom implementation of UserDetails.
