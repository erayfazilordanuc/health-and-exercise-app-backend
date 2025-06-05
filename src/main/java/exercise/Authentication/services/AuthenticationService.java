package exercise.Authentication.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import exercise.Authentication.dtos.AuthResponseDTO;
import exercise.Authentication.dtos.LoginRequestDTO;
import exercise.Authentication.dtos.RegisterRequestDTO;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        requestDTO.setUsername((requestDTO.getUsername() == null) ? userRepo.findByEmail(requestDTO.getEmail()).getUsername() : requestDTO.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.getUsername(), requestDTO.getPassword())
        );

        if (authentication.isAuthenticated()) {
            String accessToken = "Bearer " + jwtService.generateToken(requestDTO.getUsername(), requestDTO.getPassword());

            User user = (User) authentication.getPrincipal();

            AuthResponseDTO response = new AuthResponseDTO(user, accessToken);

            return response;
        } else {
            throw new UsernameNotFoundException("Invalid username-email or password");
        }
    }

    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {
        // TO DO check email pattern
        User user = new User(null, requestDTO.getUsername(), requestDTO.getEmail(), passwordEncoder.encode(requestDTO.getPassword()));
        userRepo.save(user);
        String accessToken = "Bearer " + jwtService.generateToken(requestDTO.getUsername(), requestDTO.getPassword());

        AuthResponseDTO response = new AuthResponseDTO(user, accessToken);

        return response;
    }

    public String guest(String username) {
        User user = new User(null, username, null, null);
        userRepo.save(user);
        String token = jwtService.generateToken(username, null);

        return "Bearer " + token;
    }
}
