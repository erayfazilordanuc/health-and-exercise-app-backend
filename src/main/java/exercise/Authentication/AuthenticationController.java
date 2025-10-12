package exercise.Authentication;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exercise.Authentication.dtos.AuthResponseDTO;
import exercise.Authentication.dtos.ForgotPasswordRequestDTO;
import exercise.Authentication.dtos.LoginRequestDTO;
import exercise.Authentication.dtos.NewPasswordDTO;
import exercise.Authentication.dtos.RegisterRequestDTO;
import exercise.Authentication.dtos.VerifyCodeDTO;
import exercise.Authentication.dtos.TwoStepLoginRequestDTO;
import exercise.Authentication.dtos.TwoStepRegisterRequestDTO;
import exercise.Authentication.services.AuthenticationService;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/auth")
@Tags(value = @Tag(name = "Authentication Operations"))
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO loginDTO) {
        return authenticationService.login(loginDTO);
    }

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO registerDTO) {
        return authenticationService.register(registerDTO);
    }

    @Tag(name = "Admin Operations")
    @PostMapping("/admin/login")
    public AuthResponseDTO loginAdmin(@Valid @RequestBody TwoStepLoginRequestDTO loginDTO, Locale locale) {
        return authenticationService.loginAdmin(loginDTO, locale); // loginAdmin lazım
    }

    @Tag(name = "Admin Operations")
    @PostMapping("/admin/register")
    public AuthResponseDTO registerAdmin(@Valid @RequestBody TwoStepRegisterRequestDTO requestDTO, Locale locale) {
        return authenticationService.registerAdmin(requestDTO, locale);
    }

    // @PostMapping("/guest")
    // public String guest(@RequestParam String username) {
    // return authenticationService.guest(username);
    // }

    @PostMapping("/refresh-token")
    public AuthResponseDTO refreshAccessToken(@RequestHeader("Authorization") String refreshToken) {
        return authenticationService.refreshAccessToken(refreshToken);
    }

    @PostMapping("/forgot-password/send-code")
    public ResponseEntity<Void> sendCode(@RequestBody ForgotPasswordRequestDTO dto, Locale locale) {
        authenticationService.sendForgotPasswordCode(dto, locale);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password/validate-code")
    public ResponseEntity<String> verifyCode(@RequestBody VerifyCodeDTO dto) {
        String passwordResetToken = authenticationService.verifyForgotPasswordCode(dto);
        return ResponseEntity.ok(passwordResetToken);
    }

    @PostMapping("/change-password")
    public ResponseEntity<UserDTO> changePassword(@RequestHeader("Authorization") String token, NewPasswordDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authenticationService.changePassword(dto, token, user));
    }
}
