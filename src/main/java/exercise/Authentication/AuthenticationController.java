package exercise.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import exercise.Authentication.dtos.AuthResponseDTO;
import exercise.Authentication.dtos.LoginRequestDTO;
import exercise.Authentication.dtos.RegisterRequestDTO;
import exercise.Authentication.services.AuthenticationService;

@RestController
@RequestMapping("api/auth")
@Tags(value = @Tag(name = "Authentication Operations"))
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        return authenticationService.login(requestDTO);
    }

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        return authenticationService.register(requestDTO);
    }

    // @PostMapping("/guest")
    // public String guest(@RequestParam String username) {
    // return authenticationService.guest(username);
    // }

    @PostMapping("/refresh-token")
    public AuthResponseDTO refreshAccessToken(@RequestHeader("Authorization") String refreshToken) {
        return authenticationService.refreshAccessToken(refreshToken);
    }
}
