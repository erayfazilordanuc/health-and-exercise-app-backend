package exercise.Authentication.services;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import exercise.Authentication.dtos.AuthResponseDTO;
import exercise.Authentication.dtos.LoginRequestDTO;
import exercise.Authentication.dtos.NewPasswordDTO;
import exercise.Authentication.dtos.RegisterRequestDTO;
import exercise.Authentication.dtos.ResetPasswordDTO;
import exercise.Authentication.dtos.TwoStepLoginRequestDTO;
import exercise.Authentication.dtos.TwoStepRegisterRequestDTO;
import exercise.Common.email.entities.EmailDetails;
import exercise.Common.email.services.EmailService;
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

    @Autowired
    private EmailService emailService;

    @Autowired
    private CacheManager cacheManager;

    private Cache cache() {
        return cacheManager.getCache("codes");
    }

    @Value("${auth.admin.usernames}")
    private Set<String> adminUsernames;

    @Value("${auth.admin.emails}")
    private Set<String> adminEmails;

    public AuthResponseDTO loginUserAndGenerateAuthResponseDTO(LoginRequestDTO loginDTO) {
        loginDTO.setUsername(loginDTO.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        if (authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();

            String accessToken = "Bearer " + jwtService.generateAccessToken(user);
            String refreshToken = "Bearer " + jwtService.generateRefreshToken(user);

            UserDTO userDTO = new UserDTO(user);
            AuthResponseDTO response = new AuthResponseDTO(userDTO, accessToken, refreshToken);

            return response;
        } else {
            throw new UsernameNotFoundException("Invalid username-email or password");
        }
    }

    public AuthResponseDTO login(LoginRequestDTO loginDTO) {
        if (adminUsernames.contains(loginDTO.getUsername()))
            throw new RuntimeException("You can not login as admin with this method");
        AuthResponseDTO response = loginUserAndGenerateAuthResponseDTO(loginDTO);
        return response;
    }

    public AuthResponseDTO registerUserAndGenerateAuthResponseDTO(RegisterRequestDTO registerDTO,
            String role) {
        User user = new User(null, registerDTO.getUsername(), registerDTO.getEmail(),
                registerDTO.getFullName(), registerDTO.getBirthDate(),
                passwordEncoder.encode(registerDTO.getPassword()), registerDTO.getGender(), null, null, role, null,
                registerDTO.getTheme(), "non");
        userRepo.save(user);

        String accessToken = "Bearer " +
                jwtService.generateAccessToken(user);
        String refreshToken = "Bearer " +
                jwtService.generateRefreshToken(user);

        UserDTO userDTO = new UserDTO(user);
        AuthResponseDTO responseDTO = new AuthResponseDTO(userDTO, accessToken, refreshToken);

        return responseDTO;
    }

    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {
        if (adminUsernames.contains(requestDTO.getUsername()))
            throw new RuntimeException("This username is unselectable");
        AuthResponseDTO response = registerUserAndGenerateAuthResponseDTO(requestDTO, "ROLE_USER");
        return response;
    }

    public AuthResponseDTO loginAdmin(TwoStepLoginRequestDTO requestDTO, Locale locale) {
        LoginRequestDTO loginDTO = requestDTO.getLoginDTO();
        User user = userRepo.findByUsername(loginDTO.getUsername());

        if (user.getRole().equals("ROLE_ADMIN")) {

            if (passwordEncoder.matches(requestDTO.getLoginDTO().getPassword(), user.getPassword())) {

                if (Objects.isNull(requestDTO.getCode())) {
                    String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
                    cache().put(loginDTO.getUsername(), code);
                    EmailDetails email = new EmailDetails(user.getEmail(), code,
                            locale.getLanguage().equals("tr") ? "Giriş Doğrulama Kodu" : "Login Validation Code", null);
                    emailService.sendSimpleMail(email);
                    return null;
                } else {
                    String cachedCode = cache().get(loginDTO.getUsername(), String.class);
                    if (requestDTO.getCode().equals(cachedCode)) {
                        AuthResponseDTO response = loginUserAndGenerateAuthResponseDTO(loginDTO);
                        return response;
                    }
                }
            }
        }

        throw new BadCredentialsException("Invalid admin credentials");
    }

    public AuthResponseDTO registerAdmin(TwoStepRegisterRequestDTO requestDTO, Locale locale) {
        RegisterRequestDTO registerDTO = requestDTO.getRegisterDTO();

        // if (!adminUsernames.contains(registerDTO.getUsername()))
        // throw new RuntimeException("This username is not valid for an admin");

        if (!registerDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("This emial is not authorized for admin account regsitration");
        }

        if (adminEmails.contains(registerDTO.getEmail())) {
            if (Objects.isNull(requestDTO.getCode())) {
                String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
                cache().put(registerDTO.getUsername(), code);
                EmailDetails email = new EmailDetails(registerDTO.getEmail(), code,
                        locale.getLanguage().equals("tr") ? "Hesap Oluşturma Doğrulama Kodu"
                                : "Registration Validation Code",
                        null);
                emailService.sendSimpleMail(email);
                return null;
            } else {
                String cachedCode = cache().get(registerDTO.getUsername(), String.class);
                if (requestDTO.getCode().equals(cachedCode)) {
                    AuthResponseDTO response = registerUserAndGenerateAuthResponseDTO(registerDTO, "ROLE_ADMIN");
                    return response;
                }
            }
        }

        throw new BadCredentialsException("Invalid admin credentials");
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
                String newAccessToken = "Bearer " + jwtService.generateAccessToken(user);
                String newRefreshToken = "Bearer " + jwtService.generateRefreshToken(user);
                AuthResponseDTO response = new AuthResponseDTO(userDTO, newAccessToken, newRefreshToken);
                return response;
            } else {
                throw new RuntimeException("Invalid refresh token");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void sendForgotPasswordCode(String email, Locale locale) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (!optionalUser.isPresent())
            throw new RuntimeException("Email not found");

        User user = optionalUser.get();

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        EmailDetails emailObject = new EmailDetails(user.getEmail(), code,
                locale.getLanguage().equals("tr") ? "Şifre Değiştirme Kodu" : "Password Reset Code", null);
        emailService.sendSimpleMail(emailObject);

        cache().put(email, code);
    }

    public String validateForgotPasswordCode(ResetPasswordDTO dto, User user) {
        String cachedCode = cache().get(dto.getEmail(), String.class);
        if (!dto.getCode().equals(cachedCode)) {
            throw new RuntimeException("Incorrect code");
        }
        String resetPasswordToken = "Bearer " + jwtService.generateAccessToken(user);

        return resetPasswordToken;
    }

    public UserDTO changePassword(NewPasswordDTO dto, String token, User user) {
        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        boolean isValid = jwtService.validateToken(token, userDetails, "resetPassword");
        if (!isValid) {
            throw new RuntimeException("Invalid refresh token");
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User updatedUser = userRepo.save(user);

        UserDTO userDto = new UserDTO(updatedUser);
        return userDto;
    }
}
