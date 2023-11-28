package com.sxtreh.vo;

import com.sxtreh.bo.NoteBO;
import com.sxtreh.entity.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteVO {
//    Long noteId;
//
//    String noteName;
//
//    String noteBody;
//
//    Integer noteOrder;

    Integer totals;

    List<NoteBO> notes;

}
