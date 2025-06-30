package exercise.User;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import exercise.User.dtos.UserDTO;
import exercise.User.dtos.UpdateUserDTO;
import exercise.User.entities.User;
import exercise.User.services.UserService;

@RestController
@RequestMapping("api/users")
@Tags(value = @Tag(name = "Users Operations"))
public class UserController {

    @Autowired
    public UserService userService;

    // @Tag(name = "Users - GET Operations")
    @GetMapping("/me")
    // @Cacheable("user") // Is related to RedisCache
    @Transactional(readOnly = true)
    public UserDTO getMe(@AuthenticationPrincipal User user) {
        UserDTO userDTO = userService.getUserDTO(user);
        return userDTO;
    }

    @PutMapping("/me")
    public String updateMe(@RequestBody UpdateUserDTO newUser,
            @AuthenticationPrincipal User user) throws Exception {
        User updatedUser = userService.updateUser(newUser, user);

        return "User " + updatedUser.getId() + " updated";
    }

    @DeleteMapping("/me")
    public String deleteMe(@AuthenticationPrincipal User user) {
        String response = userService.deleteUser(user);

        return response;
    }

    @GetMapping("/group/id/{id}")
    @Transactional(readOnly = true)
    public List<UserDTO> getByGroupId(@PathVariable Long id) {
        List<UserDTO> userDTOs = userService.getUsersByGroupId(id);
        return userDTOs;
    }

    @Tag(name = "Admin Operations")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{id}")
    @Transactional(readOnly = true)
    public UserDTO getById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserDTO(id);
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
