package exercise.Group;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exercise.Group.dtos.CreateGroupDTO;
import exercise.Group.dtos.GroupDTO;
import exercise.Group.dtos.GroupRequestDTO;
import exercise.Group.dtos.UpdateGroupDTO;
import exercise.Group.entities.Group;
import exercise.Group.services.GroupService;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;
import exercise.User.services.UserService;

@RestController
@RequestMapping("api/groups")
@Tags(value = @Tag(name = "Group Operations"))
public class GroupController {

  @Autowired
  public GroupService groupService;

  @Autowired
  public UserService userService;

  @PostMapping("/id/{id}/join-request")
  public GroupRequestDTO createJoinRequest(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return groupService.createJoinRequest(id, user.getId());
  }

  @DeleteMapping("/join-request/id/{id}")
  public void deleteJoinRequest(@PathVariable Long id, @AuthenticationPrincipal User user) {
    groupService.deleteJoinRequest(id, user.getId());
  }

  @GetMapping("/id/{id}/join-request")
  public List<GroupRequestDTO> getGroupRequestsByGroupId(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return groupService.getGroupRequestsByGroupId(id);
  }

  @GetMapping("/join-request/user/id/{userId}")
  public GroupRequestDTO getGroupRequessByUserId(@PathVariable Long userId, @AuthenticationPrincipal User user) {
    return groupService.getGroupRequestByUserId(userId);
  }

  @GetMapping
  public List<Group> getAll() {
    List<Group> groups = groupService.getAll();
    return groups;
  }

  @GetMapping("/id/{id}")
  public Group getById(@PathVariable Long id) {
    Group group = groupService.getGroupById(id);
    return group;
  }

  @GetMapping("/id/{id}/size")
  public ResponseEntity<Integer> getGroupSize(@PathVariable Long id) {
    Integer count = groupService.countMembers(id);
    return ResponseEntity.ok(count);
  }

  @GetMapping("/id/{id}/admin")
  public ResponseEntity<UserDTO> getGroupAdmin(@PathVariable Long id) {
    UserDTO admin = groupService.getAdmin(id);
    return ResponseEntity.ok(admin);
  }

  @GetMapping("/id/{id}/users")
  @Transactional(readOnly = true)
  public List<UserDTO> getByGroupId(@PathVariable Long id) {
    List<UserDTO> userDTOs = userService.getUsersByGroupId(id);
    return userDTOs;
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/join-request/id/{id}/response")
  public void respondToGroupJoinRequest(@PathVariable Long id, @RequestParam boolean approved,
      @AuthenticationPrincipal User user) {
    groupService.respondToGroupJoinRequest(id, approved);
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Group create(@RequestBody CreateGroupDTO createGroupDTO, @AuthenticationPrincipal User user) {
    if (!Objects.isNull(createGroupDTO.getAdminId())) {
      if (!createGroupDTO.getAdminId().equals(user.getId())) {
        throw new RuntimeException("You can not create group for someone else");
      }
    }
    createGroupDTO.setAdminId(user.getId());
    Group group = groupService.createGroup(createGroupDTO);
    return group;
  }

  @Tag(name = "Admin Operations")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping
  public Group update(@RequestBody UpdateGroupDTO updateGroupDTO) {
    Group group = groupService.updateGroup(updateGroupDTO);
    return group;
  }
}
