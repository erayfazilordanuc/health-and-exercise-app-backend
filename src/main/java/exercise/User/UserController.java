package exercise.User;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import exercise.User.dtos.UserDTO;
import exercise.User.dtos.UpdateUserDTO;
import exercise.User.entities.User;
import exercise.User.services.UserService;

@RestController
@RequestMapping("api/user")
@Tags(value = @Tag(name = "User Operations"))
public class UserController {

    @Autowired
    public UserService userService;

    @GetMapping
    @Cacheable("user")
    @Transactional(readOnly = true)
    public UserDTO getUser(@AuthenticationPrincipal User user) {
        UserDTO userDTO = userService.getUserDTO(user);
        return userDTO;
    }

    @PutMapping
    public String updateUser(@RequestBody UpdateUserDTO newUser, @AuthenticationPrincipal User user) throws Exception {
        User updatedUser = userService.updateUser(newUser, user);

        return "User " + updatedUser.getId() + " updated";
    }

    @DeleteMapping
    public String deleteUser(@AuthenticationPrincipal User user) {
        String response = userService.deleteUser(user);

        return response;
    }
}
