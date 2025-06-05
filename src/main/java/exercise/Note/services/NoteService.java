package exercise.Note.services;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exercise.Note.dtos.NoteDTO;
import exercise.Note.entities.Note;
import exercise.Note.mappers.NoteMapper;
import exercise.Note.repositories.NoteRepository;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepo;

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private UserRepository userRepo;

    public Note createNote(NoteDTO noteDTO) {
        User author = userRepo.findById(noteDTO.getAuthorId()).get();

        Note newNote = new Note(null, noteDTO.getTitle(), noteDTO.getContent(), author, noteDTO.getIsFavorited(), null, null, null);
        Note savedNote = noteRepo.save(newNote);

        return savedNote;
    }

    public Note getNoteById(Long id) {
        return null;
    }

    public Note getNoteByAuthorId(Long id) {
        return null;
    }

    public Note updateNote(Long id, NoteDTO noteDTO, Long authorId) {
        Note note = noteRepo.findById(id).get();

        System.out.println(note);

        if (!Objects.equals(note.getAuthor().getId(), authorId)) {
            throw new Error("There is no note with this note id and the author id");
        }

        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());

        Note savedNote = noteRepo.save(note);

        return savedNote;
    }
}
