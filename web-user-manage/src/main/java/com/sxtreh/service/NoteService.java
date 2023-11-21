package com.sxtreh.service;

import com.sxtreh.dto.NoteDTO;
import com.sxtreh.entity.Note;

import java.util.List;

public interface NoteService {
    void saveNote(NoteDTO noteDTO);

    void deleteNote(Long noteId);

    void modifyNote(NoteDTO noteDTO);

    List<Note> listNote();

    List<Note> listNoteByCatalogId(Integer catalogId);
}
