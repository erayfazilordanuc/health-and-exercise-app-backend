package exercise.User;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exercise.User.dtos.UserDTO;
import exercise.User.dtos.UpdateUserDTO;
import exercise.User.entities.User;
import exercise.User.services.UserService;

@RestController
@RequestMapping("api/users")
@Tags(value = @Tag(name = "User Operations"))
public class UserController {

    @Autowired
    public UserService userService;

    // @Tag(name = "Users - GET Operations")
    @GetMapping("/me")
    // @Cacheable("user") // Is related to RedisCache
    public UserDTO getMe(@AuthenticationPrincipal User user) {
        UserDTO userDTO = userService.getUserDTO(user);
        return userDTO;
    }

    // @GetMapping("/me/achievements")
    // public List<AchievementDTO> getAchievement(@AuthenticationPrincipal User
    // user) {
    // return userService.getAchievements(user.getId());
    // }

    @PutMapping("/me")
    public UserDTO updateMe(@RequestBody UpdateUserDTO newUser,
            @AuthenticationPrincipal User user) throws Exception {
        UserDTO updatedUserDTO = userService.updateUserAndGetDTO(newUser, user);

        return updatedUserDTO;
    }

    @DeleteMapping("/me")
    public String deleteMe(@AuthenticationPrincipal User user) {
        String response = userService.deleteUser(user);

        return response;
    }

    // @Tag(name = "Admin Operations")
    // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{id}")
    @Transactional(readOnly = true)
    public UserDTO getById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        UserDTO userDTO = userService.getPublicUserDTO(id, user);
        if (user.getRole().equals("ROLE_USER")) {
            userDTO.setEmail(null);
            userDTO.setGroupId(null);
            userDTO.setRole(null);
        }
        return userDTO;
    }

    @GetMapping("/{username}")
    @Transactional(readOnly = true)
    public UserDTO getByUsername(@PathVariable String username, @AuthenticationPrincipal User user) {
        UserDTO userDTO = userService.getPublicUserDTO(username, user);
        if (user.getRole().equals("ROLE_USER")) {
            if (userDTO.getRole().equals("ROLE_USER")) {
                userDTO.setEmail(null);
            }
            userDTO.setGroupId(null);
            userDTO.setRole(null);
        }
        return userDTO;
    }

    // @PreAuthorize("hasRole('ADMIN')")
    // @PutMapping("/id/{id}")
    // public String updateById(@PathVariable Long id, @RequestBody UpdateUserDTO
    // newUser) throws Exception {
    // User updatedUser = userService.updateUser(newUser, id);

    // return "User " + updatedUser.getId() + " updated";
    // }

    // @PreAuthorize("hasRole('ADMIN')")
    // @DeleteMapping("/id/{id}")
    // public String deleteById(@PathVariable Long id) {
    // String response = userService.deleteUser(id);

    // return response;
    // }
}
