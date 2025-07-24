package com.back.domain.note.service;

import com.back.domain.note.entity.Note;
import com.back.domain.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;

    public List<Note> findAll() {
        return noteRepository.findAll();
    }

    public Optional<Note> findByid(int id) {
        return noteRepository.findById(id);
    }

    public Note write(String title, String content) {
        Note note = new Note(title, content);

        noteRepository.save(note);

        return note;
    }

    public void flush() {
        noteRepository.flush();
    }

    public void modify(Note note, String title, String content) {
        note.setTitle(title);
        note.setContent(content);
    }

    public long count() {
        return noteRepository.count();
    }

    public void delete(int id) {
        noteRepository.deleteById(id);
    }
}
