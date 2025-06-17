package exercise.Authentication.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import exercise.Authentication.dtos.AuthResponseDTO;
import exercise.Authentication.dtos.LoginRequestDTO;
import exercise.Authentication.dtos.RegisterRequestDTO;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import exercise.User.services.UserService;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        requestDTO.setUsername(
                (requestDTO.getUsername() == null) ? userRepo.findByEmail(requestDTO.getEmail()).getUsername()
                        : requestDTO.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.getUsername(), requestDTO.getPassword()));

        if (authentication.isAuthenticated()) {
            String accessToken = "Bearer " + jwtService.generateAccessToken(requestDTO.getUsername());
            String refreshToken = "Bearer " + jwtService.generateRefreshToken(requestDTO.getUsername());

            User user = (User) authentication.getPrincipal();

            AuthResponseDTO response = new AuthResponseDTO(user, accessToken, refreshToken);

            return response;
        } else {
            throw new UsernameNotFoundException("Invalid username-email or password");
        }
    }

    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {
        // TO DO check email pattern
        User user = new User(null, requestDTO.getUsername(), requestDTO.getEmail(),
                passwordEncoder.encode(requestDTO.getPassword()));
        userRepo.save(user);
        String accessToken = "Bearer " + jwtService.generateAccessToken(requestDTO.getUsername());
        String refreshToken = "Bearer " + jwtService.generateRefreshToken(requestDTO.getUsername());

        AuthResponseDTO response = new AuthResponseDTO(user, accessToken, refreshToken);

        return response;
    }

    public String guest(String username) {
        User user = new User(null, username, null, null);
        userRepo.save(user);
        String token = jwtService.generateToken(username, null, null);

        return "Bearer " + token;
    }

    public AuthResponseDTO refreshAccessToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            User user = userRepo.findByUsername(username);
            UserDetails userDetails = userService.loadUserByUsername(username);
            boolean isValid = jwtService.validateToken(refreshToken, userDetails);
            String tokenType = jwtService.extractClaim(refreshToken, claims -> claims.get("tokenType", String.class));
            boolean isRefreshToken = tokenType.equals("refresh");

            if (isValid && isRefreshToken) {
                String newAccessToken = "Bearer " + jwtService.generateAccessToken(username);
                String newRefreshToken = "Bearer " + jwtService.generateRefreshToken(username);
                AuthResponseDTO response = new AuthResponseDTO(user, newAccessToken, newRefreshToken);
                return response;
            } else {
                throw new RuntimeException("Invalid refresh token");
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
