package com.back.domain.note.controller;

import com.back.domain.note.dto.NoteDto;
import com.back.domain.note.entity.Note;
import com.back.domain.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notes")
public class NoteController {
    private final NoteService noteService;

    @GetMapping
    @Transactional(readOnly = true)
    public List<NoteDto> getItems() {
        List<Note> noteList = noteService.findAll();

        return noteList
                .stream()
                .map(NoteDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public NoteDto getItem(@PathVariable int id) {
        Note note = noteService.findByid(id).get();

        return new NoteDto(note);
    }
}
