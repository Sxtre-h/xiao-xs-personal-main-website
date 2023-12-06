package com.sxtreh.note.controller;

import com.sxtreh.bo.NoteBO;
import com.sxtreh.dto.NoteColDTO;
import com.sxtreh.dto.NoteDTO;
import com.sxtreh.enumeration.ParameterRuleType;
import com.sxtreh.note.annotation.ParameterCheck;
import com.sxtreh.note.service.NoteService;
import com.sxtreh.result.Result;
import com.sxtreh.vo.NoteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记管理
 */
@Slf4j
@RequestMapping("/note/notes")
@RestController
public class NoteController {
    @Autowired
    private NoteService noteService;

    /**
     * 添加笔记
     *
     * @param noteDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.NOTE_SAVE)
    @PostMapping
    public Result<NoteVO> saveNote(@RequestBody NoteDTO noteDTO) {
        noteService.saveNote(noteDTO);
        return Result.success();
    }

    /**
     * 增加笔记表的列
     *
     * @param noteColDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.NOTE_SAVE_COL)
    @PostMapping("/cols")
    public Result<NoteVO> saveNoteCol(@RequestBody NoteColDTO noteColDTO) {
        noteService.saveNoteCol(noteColDTO.getCatalogId());
        return Result.success();
    }

    /**
     * 删除笔记
     *
     * @param noteDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.NOTE_DELETE)
    @DeleteMapping
    public Result<NoteVO> deleteNote(@RequestBody NoteDTO noteDTO) {
        noteService.deleteNote(noteDTO.getNoteId());
        return Result.success();
    }

    /**
     * 删除笔记表格的一列
     * @param noteColDTO
     * @return
     */

    @ParameterCheck(rule = ParameterRuleType.NOTE_DELETE_COL)
    @DeleteMapping("/cols")
    public Result<NoteVO> deleteNoteCol(@RequestBody NoteColDTO noteColDTO) {
        noteService.deleteNoteCol(noteColDTO);
        return Result.success();
    }


    /**
     * 修改笔记
     *
     * @param noteDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.NOTE_MODIFY)
    @PutMapping
    public Result<NoteVO> modifyNote(@RequestBody NoteDTO noteDTO) {
        noteService.modifyNote(noteDTO);
        return Result.success();
    }

    /**
     * 修改笔记顺序，目前仅支持交换两者顺序，不允许用户修改Order字段
     *
     * @param myId
     * @param theOtherId
     * @return
     */

    @ParameterCheck(rule = ParameterRuleType.ALL_NOT_NULL)
    @PutMapping("/modifyOrder")
    public Result<NoteVO> modifyNoteOrder(@RequestParam(name = "myId") Long myId, @RequestParam(name = "theOtherId") Long theOtherId) {
        noteService.swapNoteOrder(Long.valueOf(myId), Long.valueOf(theOtherId));
        return Result.success();
    }

    /**
     * 修改笔记列名
     * 弃用
     *
     * @param catalogId
     * @param colNames
     * @return
     */

    @ParameterCheck(rule = ParameterRuleType.NOTE_COLS_MODIFY)
//    @PutMapping("/cols")
    public Result<NoteVO> modifyNoteColNames(Long catalogId, List<String> colNames) {
        noteService.modifyNoteColNames(catalogId, colNames);
        return Result.success();
    }

    /**
     * 获取全部笔记
     *
     * @return
     */
    @GetMapping
    public Result<NoteVO> listNote() {
        List<NoteBO> notes = noteService.listNote();
        NoteVO noteVO = new NoteVO(notes.size(), notes);
        return Result.success(noteVO);
    }

    /**
     * 根据目录ID获取笔记
     *
     * @param catalogId
     * @return
     */
    @GetMapping("/{catalogId}")
    public Result<NoteVO> listNoteByCatalogId(@PathVariable("catalogId") Integer catalogId) {
        List<NoteBO> notes = noteService.listNoteByCatalogId(catalogId);
        NoteVO noteVO = new NoteVO(notes.size(), notes);
        return Result.success(noteVO);
    }
}
