package com.minerva.MinervaDatabase.database.controllers;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.minerva.MinervaDatabase.database.details.UserDetailsImpl;
import com.minerva.MinervaDatabase.database.exceptions.AliasNotFoundException;
import com.minerva.MinervaDatabase.database.models.ERole;
import com.minerva.MinervaDatabase.database.models.RefreshToken;
import com.minerva.MinervaDatabase.database.models.Role;
import com.minerva.MinervaDatabase.database.models.User;
import com.minerva.MinervaDatabase.database.payload.request.LoginRequest;
import com.minerva.MinervaDatabase.database.payload.request.RefreshJwtRequest;
import com.minerva.MinervaDatabase.database.payload.request.RegisterRequest;
import com.minerva.MinervaDatabase.database.payload.response.JwtResponse;
import com.minerva.MinervaDatabase.database.payload.response.MessageResponse;
import com.minerva.MinervaDatabase.database.repository.RefreshTokenRepository;
import com.minerva.MinervaDatabase.database.repository.RoleRepository;
import com.minerva.MinervaDatabase.database.repository.UserRepository;
import com.minerva.MinervaDatabase.database.security.jwt.JwtUtils;
import com.minerva.MinervaDatabase.database.services.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.message.AuthException;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    @Autowired
    AuthenticationManager authManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserDetailsServiceImpl detailsService;

    @Autowired
    JwtUtils jwtUtils;

    private final Map<String, String> refreshStorage = new HashMap<>();

    @PostMapping(path = "/register", consumes = "application/json")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest req) throws AliasNotFoundException {
        if (userRepository.existsByPhone(req.getPhone())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Phone is already in use!"));
        }

        if (userRepository.existsByAlias(req.getAlias())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Alias is already in use!"));
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setAlias(req.getAlias());
        user.setPhone(req.getPhone());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(()->new RuntimeException("Error: Role is not found.")));

        user.setRoles(roles);
        userRepository.save(user);

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getAlias(), req.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwtAccess = jwtUtils.generateJwtToken((UserDetailsImpl) auth.getPrincipal());
        String jwtRefresh = jwtUtils.generateRefreshToken((UserDetailsImpl) auth.getPrincipal());
        refreshTokenRepository.save(new RefreshToken(user.getId(), jwtRefresh));

        return ResponseEntity.ok(
                new JwtResponse(
                        jwtAccess,
                        jwtRefresh
                )
        );
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws AliasNotFoundException {
        Authentication auth;
        UserDetailsImpl u = (UserDetailsImpl) detailsService.loadUserByAliasOrPhone(loginRequest.getAlias());
        auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(u.getAlias(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwtAccess = jwtUtils.generateJwtToken((UserDetailsImpl) auth.getPrincipal());
        String jwtRefresh = jwtUtils.generateRefreshToken((UserDetailsImpl) auth.getPrincipal());
        try {
            RefreshToken refToken = refreshTokenRepository.findByUserId(u.getId()).orElseThrow();
            refToken.setRefreshToken(jwtRefresh);
            refreshTokenRepository.save(refToken);
        } catch (Exception e) {
            refreshTokenRepository.save(new RefreshToken(u.getId(), jwtRefresh));
        }

        return ResponseEntity.ok(
                new JwtResponse(
                        jwtAccess,
                        jwtRefresh
                )
        );
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshJwtRequest request) throws Exception {
        String refreshToken = request.getRefreshToken();
        log.debug(refreshToken);
        if (jwtUtils.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtUtils.getRefreshClaims(refreshToken);
            final String userId = claims.getSubject();
            final RefreshToken saveRefreshToken = refreshTokenRepository.findByUserId(Long.valueOf(userId)).orElseThrow(() -> new Exception("Not found token with userId " + userId));
            if (saveRefreshToken != null && saveRefreshToken.getRefreshToken().equals(refreshToken)) {
                final UserDetailsImpl user = (UserDetailsImpl) detailsService.loadUserById(Long.valueOf(userId));
                final String accessToken = jwtUtils.generateJwtToken(user);
                final String newRefreshToken = jwtUtils.generateRefreshToken(user);
                saveRefreshToken.setRefreshToken(newRefreshToken);
                refreshTokenRepository.save(saveRefreshToken);
                return ResponseEntity.ok(new JwtResponse(accessToken, newRefreshToken));
            }
        }
        throw new AuthException("Невалидный JWT токен: " + refreshToken);
    }

}
