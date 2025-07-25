package com.back.domain.note.service;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.note.entity.Note;
import com.back.domain.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;

    public Optional<Bookmark> findBookmarkById(int id) {
        return bookmarkRepository.findById(id);
    }

    public Note write(int bookmarkId, String title, String content) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new NoSuchElementException("%d번 북마크가 없습니다.".formatted(bookmarkId)));

        Note note = new Note(title, content, bookmark);
        bookmark.getNotes().add(note);
//        return noteRepository.save(note);
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

    public void delete(Bookmark bookmark, Note note) {
        bookmark.getNotes().remove(note);
    }

    public Optional<Note> findNoteById(Bookmark bookmark, int id) {
        return bookmark
                .getNotes()
                .stream()
                .filter(note -> note.getId() == id)
                .findFirst();
    }
}
