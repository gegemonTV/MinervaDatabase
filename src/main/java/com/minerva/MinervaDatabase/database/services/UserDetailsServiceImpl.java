package com.minerva.MinervaDatabase.database.services;

import com.minerva.MinervaDatabase.database.details.UserDetailsImpl;
import com.minerva.MinervaDatabase.database.exceptions.AliasNotFoundException;
import com.minerva.MinervaDatabase.database.models.User;
import com.minerva.MinervaDatabase.database.repository.UserRepository;
import org.hibernate.sql.Alias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Transactional
    public UserDetails loadUserById(Long id) {
        User u = userRepository.findById(id).orElseThrow();
        return UserDetailsImpl.build(u);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByAlias(username)
                .orElseThrow(()->new UsernameNotFoundException("Not found user with alias: " + username));
        return UserDetailsImpl.build(user);
    }

    @Transactional
    public UserDetails loadUserByAlias(String alias) throws AliasNotFoundException {
        User user = userRepository.findByAlias(alias)
                .orElseThrow(() -> new AliasNotFoundException("No such alias!"));
        return loadUserByUsername(alias);
    }

    @Transactional
    public UserDetails loadUserByAliasOrPhone(String s) throws UsernameNotFoundException {
        UserDetails ud;
        try {
            ud = loadUserByAlias(s);
        } catch (AliasNotFoundException e) {
            ud = loadUserByPhone(s);
        }
        return ud;
    }

    @Transactional
    public List<UserDetails> loadUsersByUsername(String username){
        List<User> users = userRepository.findAllByUsername(username)
                .orElse(new ArrayList<>());
        List<UserDetails> details = new ArrayList<>();
        for (User u :
                users) {
            details.add(UserDetailsImpl.build(u));
        }
        return details;
    }

    @Transactional
    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + phone));
        return UserDetailsImpl.build(user);
    }

    public void updateResetPasswordToken(String token, String phone) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        user.setResetPasswordToken(token);
        userRepository.save(user);
    }

    @Transactional
    public User getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token).orElseThrow(()-> new UsernameNotFoundException("Not found user with token " + token));
    }

    public void updatePassword(User user, String newPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(newPassword);
        user.setPassword(encodedPassword);

        user.setResetPasswordToken(null);
        userRepository.save(user);
    }
}
