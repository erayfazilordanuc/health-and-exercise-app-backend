package exercise.Group;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.util.List;
import java.util.Objects;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exercise.Group.dtos.CreateGroupDTO;
import exercise.Group.dtos.GroupDTO;
import exercise.Group.entities.Group;
import exercise.Group.services.GroupService;
import exercise.User.entities.User;

@RestController
@RequestMapping("api/groups")
@Tags(value = @Tag(name = "Groups Operations"))
public class GroupController {

  @Autowired
  public GroupService groupService;

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
}
