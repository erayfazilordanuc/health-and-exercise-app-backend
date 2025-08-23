package exercise.Session.mappers;

import org.springframework.stereotype.Component;

import exercise.Session.dtos.SessionDTO;
import exercise.Session.entities.Session;
import exercise.Session.mappers.SessionMapper;

@Component
public class SessionMapper {
    // public SessionDTO entityToDTO(Session consent) {
    //     SessionDTO SessionDTO = new SessionDTO(consent.getId(),
    //             consent.getUser().getId(),
    //             consent.getLocale(),
    //             consent.getSource());

    //     return SessionDTO;
    // }
}
