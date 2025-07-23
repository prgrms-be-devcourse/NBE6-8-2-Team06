package com.back.domain.user.user.service;

import com.back.domain.user.user.Repository.UserRepository;
import com.back.domain.user.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    public User join(String email, String name, String password){
        User user = new User(email, name, password);
        return userRepository.save(user);
    }
    public Optional<User> findByEmail(String email){ return userRepository.findByEmail(email); }
}
