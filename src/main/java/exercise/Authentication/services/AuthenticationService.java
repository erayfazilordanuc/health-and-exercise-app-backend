package exercise.Authentication.services;

import java.util.Objects;

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
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;
import exercise.User.mappers.UserMapper;
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
        requestDTO.setUsername(requestDTO.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.getUsername(), requestDTO.getPassword()));

        if (authentication.isAuthenticated()) {
            String accessToken = "Bearer " + jwtService.generateAccessToken(requestDTO.getUsername());
            String refreshToken = "Bearer " + jwtService.generateRefreshToken(requestDTO.getUsername());

            User user = (User) authentication.getPrincipal();

            UserDTO userDTO = new UserDTO(user);
            AuthResponseDTO response = new AuthResponseDTO(userDTO, accessToken, refreshToken);

            return response;
        } else {
            throw new UsernameNotFoundException("Invalid username-email or password");
        }
    }

    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {
        // TO DO check email pattern
        String role = "ROLE_USER";

        if (Objects.equals(requestDTO.getUsername(), "erayfazilordanuc"))
            role = "ROLE_ADMIN";

        User user = new User(null, requestDTO.getUsername(), requestDTO.getEmail(), requestDTO.getFullName(),
                passwordEncoder.encode(requestDTO.getPassword()), role);
        userRepo.save(user);

        String accessToken = "Bearer " + jwtService.generateAccessToken(requestDTO.getUsername());
        String refreshToken = "Bearer " + jwtService.generateRefreshToken(requestDTO.getUsername());

        UserDTO userDTO = new UserDTO(user);
        AuthResponseDTO response = new AuthResponseDTO(userDTO, accessToken, refreshToken);

        return response;
    }

    public AuthResponseDTO refreshAccessToken(String refreshToken) {
        try {
            refreshToken = refreshToken.replaceFirst("^Bearer\\s+", "");
            String username = jwtService.extractUsername(refreshToken);
            User user = userRepo.findByUsername(username);
            UserDTO userDTO = new UserDTO(user);
            UserDetails userDetails = userService.loadUserByUsername(username);
            boolean isValid = jwtService.validateToken(refreshToken, userDetails, "refresh");

            if (isValid) {
                String newAccessToken = "Bearer " + jwtService.generateAccessToken(username);
                String newRefreshToken = "Bearer " + jwtService.generateRefreshToken(username);
                AuthResponseDTO response = new AuthResponseDTO(userDTO, newAccessToken, newRefreshToken);
                return response;
            } else {
                throw new RuntimeException("Invalid refresh token");
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
