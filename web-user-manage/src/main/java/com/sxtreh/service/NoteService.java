package com.sxtreh.service;

import com.sxtreh.bo.NoteBO;
import com.sxtreh.dto.NoteColDTO;
import com.sxtreh.dto.NoteDTO;
import com.sxtreh.entity.Note;

import java.util.List;

public interface NoteService {
    void saveNote(NoteDTO noteDTO);

    void deleteNote(Long noteId);

    void modifyNote(NoteDTO noteDTO);

    List<NoteBO> listNote();

    List<NoteBO> listNoteByCatalogId(Integer catalogId);

    void saveNoteCol(Long catalogId);

    void modifyNoteColNames(Long catalogId, List<String> colNames);

    void deleteNoteCol(NoteColDTO noteColDTO);

    void swapNoteOrder(Long myId, Long theOtherId);
}
