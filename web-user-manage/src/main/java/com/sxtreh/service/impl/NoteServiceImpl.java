package com.sxtreh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.constant.StatusConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.dto.NoteDTO;
import com.sxtreh.entity.Note;
import com.sxtreh.entity.NoteCatalog;
import com.sxtreh.exception.DataNotExistException;
import com.sxtreh.mapper.NoteCatalogMapper;
import com.sxtreh.mapper.NoteMapper;
import com.sxtreh.service.NoteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private NoteCatalogMapper noteCatalogMapper;

    @Override
    public void saveNote(NoteDTO noteDTO) {
        Note note = new Note();
        BeanUtils.copyProperties(noteDTO, note);

        //如果插入的目录不存在或者不是自己的目录，请求非法！非法请求也可当作不存在处理
        NoteCatalog noteCatalog = noteCatalogMapper.selectById(note.getCatalogId());
        if (noteCatalog == null || noteCatalog.getUserId() != BaseContext.getCurrentId()) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }

        note.setUserId(BaseContext.getCurrentId());
        note.setStatus(StatusConstant.UNSHARED);
        note.setNoteOrder(1);//TODO 临时填充

        noteMapper.insert(note);
    }

    @Override
    public void deleteNote(Long noteId) {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getId, noteId)
                .eq(Note::getUserId, BaseContext.getCurrentId());
        noteMapper.delete(queryWrapper);
    }

    @Override
    public void modifyNote(NoteDTO noteDTO) {
        Note note = new Note();
        BeanUtils.copyProperties(noteDTO, note);
        //TODO 目录排序
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getId, noteDTO.getNoteId())
                .eq(Note::getUserId, BaseContext.getCurrentId());
        noteMapper.update(note, queryWrapper);
    }

    @Override
    public List<Note> listNote() {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, BaseContext.getCurrentId());
        return noteMapper.selectList(queryWrapper);
    }

    @Override
    public List<Note> listNoteByCatalogId(Integer catalogId) {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getCatalogId, catalogId)
                .eq(Note::getUserId, BaseContext.getCurrentId());
        return noteMapper.selectList(queryWrapper);
    }
}
