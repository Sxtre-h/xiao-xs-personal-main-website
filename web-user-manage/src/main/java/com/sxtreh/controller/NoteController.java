package com.sxtreh.controller;

import com.sxtreh.annotation.RequireLogin;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.dto.NoteCatalogDTO;
import com.sxtreh.dto.NoteDTO;
import com.sxtreh.entity.Note;
import com.sxtreh.entity.NoteCatalog;
import com.sxtreh.exception.ParameterMissingException;
import com.sxtreh.result.Result;
import com.sxtreh.service.NoteCatalogService;
import com.sxtreh.service.NoteService;
import com.sxtreh.vo.NoteCatalogVO;
import com.sxtreh.vo.NoteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记管理
 * TODO CRUD前先判断目录是否存在
 */
@Slf4j
@RequestMapping("/note/notes")
@RestController
public class NoteController {
    @Autowired
    private NoteService noteService;

    /**
     * 添加笔记
     * @param noteDTO
     * @return
     */
    @RequireLogin
    @PostMapping
    public Result<NoteVO> saveNote(@RequestBody NoteDTO noteDTO){
        if(noteDTO.getCatalogId() == null || noteDTO.getNoteName() == null || noteDTO.getNoteBody() == null){
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        noteService.saveNote(noteDTO);
        return Result.success();
    }

    /**
     * 删除笔记
     * @param noteDTO
     * @return
     */
    @RequireLogin
    @DeleteMapping
    public Result<NoteVO> deleteNote(@RequestBody NoteDTO noteDTO){
        if(noteDTO.getNoteId() == null){
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        noteService.deleteNote(noteDTO.getNoteId());
        return Result.success();
    }

    /**
     * 修改笔记
     * @param noteDTO
     * @return
     */
    @RequireLogin
    @PutMapping
    public Result<NoteVO> modifyNote(@RequestBody NoteDTO noteDTO){
        if(noteDTO.getNoteId() == null || noteDTO.getNoteName() == null || noteDTO.getNoteBody() == null){
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        noteService.modifyNote(noteDTO);
        return Result.success();
    }

    /**
     * 获取全部笔记
     * @return
     */
    @RequireLogin
    @GetMapping
    public Result<NoteVO> listNote(){
        List<Note> notes = noteService.listNote();
        NoteVO noteVO =new NoteVO(notes.size(), notes);
        return Result.success(noteVO);
    }

    /**
     * 根据目录ID获取笔记
     * @param catalogId
     * @return
     */
    @RequireLogin
    @GetMapping("/{catalogId}")
    public Result<NoteVO> listNoteByCatalogId(@PathVariable("catalogId") Integer catalogId) {
        List<Note> notes = noteService.listNoteByCatalogId(catalogId);
        NoteVO noteVO =new NoteVO(notes.size(), notes);
        return Result.success(noteVO);
    }
}
