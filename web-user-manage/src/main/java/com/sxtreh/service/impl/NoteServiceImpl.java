package com.sxtreh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sxtreh.bo.NoteBO;
import com.sxtreh.constant.EndlessCycleConstant;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.constant.StatusConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.dto.NoteColDTO;
import com.sxtreh.dto.NoteDTO;
import com.sxtreh.entity.Note;
import com.sxtreh.entity.NoteCatalog;
import com.sxtreh.exception.DataNotExistException;
import com.sxtreh.exception.ParameterErrorException;
import com.sxtreh.mapper.NoteCatalogMapper;
import com.sxtreh.mapper.NoteMapper;
import com.sxtreh.service.NoteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class NoteServiceImpl implements NoteService {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private NoteCatalogMapper noteCatalogMapper;

    @Transactional()
    @Override
    public void saveNote(NoteDTO noteDTO) {
        //如果插入的目录不存在或者不是自己的目录，请求非法！非法请求也可当作不存在处理
        NoteCatalog noteCatalog = noteCatalogMapper.selectById(noteDTO.getCatalogId());
        if (noteCatalog == null || !noteCatalog.getUserId().equals(BaseContext.getCurrentId())) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        //如果目录没有笔记, 表为空, 插入表头
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getCatalogId, noteCatalog.getId())
                .eq(Note::getUserId, BaseContext.getCurrentId());
        List<Note> notes = noteMapper.selectList(queryWrapper);
        int noteNum = notes.size();
        if (noteNum == 0) {
            Note header = Note.builder()
                    .userId(BaseContext.getCurrentId())
                    .catalogId(noteCatalog.getId())
                    .noteName("索引")
                    .noteBody("{\"title\":\"内容区标题\"}")
                    .noteOrder(noteNum++)
                    .status(StatusConstant.UNSHARED)
                    .build();
            noteMapper.insert(header);
        }
        Note note = new Note();
        BeanUtils.copyProperties(noteDTO, note);

        //名字改为noteOrder方便排序
        note.setNoteName(String.valueOf(noteNum));
        note.setUserId(BaseContext.getCurrentId());
        note.setStatus(StatusConstant.UNSHARED);
        note.setNoteOrder(noteNum);
        if (noteDTO.getNoteBody() != null) {
            String str = noteDTO.getNoteBody().toString();
            note.setNoteBody(str);
        }
        noteMapper.insert(note);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNoteCol(Long catalogId) {
        //拿到该目录下所有笔记
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getCatalogId, catalogId)
                .eq(Note::getUserId, BaseContext.getCurrentId());
        List<Note> notes = noteMapper.selectList(queryWrapper);
        //TODO MP逐条插入改批量插入
        for (Note note : notes) {
            JSONObject jsonObject = (JSONObject) JSON.parse(note.getNoteBody());
            //为新列创建列名，并记录是否创建成功
            boolean newColsSuccess = false;
            for (int i = 1; i < EndlessCycleConstant.MAX_NEW_COLS; i++) {
                if (jsonObject.get("新列(" + i + ")") == null) {
                    //表头还应加入默认信息
                    if (note.getNoteOrder() == 0) {
                        jsonObject.put("新列(" + i + ")", "新内容区标题");
                    } else {
                        jsonObject.put("新列(" + i + ")", "");
                    }
                    newColsSuccess = true;
                    break;
                }
            }
            if (!newColsSuccess) {
                throw new ParameterErrorException(MessageConstant.PARAMETER_ERROR);
            }
            note.setNoteBody(jsonObject.toString());
            LambdaQueryWrapper<Note> subQueryWrapper = new LambdaQueryWrapper<>();
            subQueryWrapper.eq(Note::getId, note.getId());
            noteMapper.update(note, subQueryWrapper);
        }
    }

    @Transactional()
    @Override
    public void deleteNote(Long noteId) {
        Note deleteNote = noteMapper.selectById(noteId);
        //表头不许删除
        if (deleteNote.getNoteOrder().equals(0)) {
            throw new ParameterErrorException(MessageConstant.PARAMETER_ERROR);
        }
        noteMapper.deleteById(noteId);

        //更新当前目录下所有笔记的Order, 重新排序 TODO 笔记多的话开销很大，以后改用链表
        //拿到该目录下所有笔记
        LambdaQueryWrapper<Note> selectQueryWrapper = new LambdaQueryWrapper<>();
        selectQueryWrapper.eq(Note::getCatalogId, deleteNote.getCatalogId())
                .eq(Note::getUserId, BaseContext.getCurrentId())
                .orderByAsc(Note::getNoteOrder);
        List<Note> notes = noteMapper.selectList(selectQueryWrapper);
        //表头就不要设置了
        for (int i = 1; i < notes.size(); i++) {
            Note note = notes.get(i);
            note.setNoteOrder(i);
            note.setNoteName(String.valueOf(i));
            LambdaQueryWrapper<Note> subQueryWrapper = new LambdaQueryWrapper<>();
            subQueryWrapper.eq(Note::getId, note.getId());
            noteMapper.update(note, subQueryWrapper);
        }
    }

    @Transactional()
    @Override
    public void deleteNoteCol(NoteColDTO noteColDTO) {
        //取目录下所有笔记
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getCatalogId, noteColDTO.getCatalogId())
                .eq(Note::getUserId, BaseContext.getCurrentId())
                .orderByAsc(Note::getNoteOrder);
        List<Note> notes = noteMapper.selectList(queryWrapper);
        //没有找到相关数据
        if (notes.size() == 0) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        //寻找对应列的key，默认只删除第一个key
        JSONObject jsonObject = (JSONObject) JSON.parse(notes.get(0).getNoteBody());
        Set<String> keys = jsonObject.keySet();
        String key = null;
        for (String str : keys) {
            if (jsonObject.getString(str).equals(noteColDTO.getNoteColName())) {
                key = str;
                break;
            }
        }
        if (key == null) throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        //删除列
        for (Note note : notes) {
            //修改noteBody
            JSONObject modifyObj = (JSONObject) JSON.parse(note.getNoteBody());
            modifyObj.remove(key);
            note.setNoteBody(modifyObj.toString());
            //更新表
            noteMapper.updateById(note);
        }
    }


    @Override
    public void modifyNote(NoteDTO noteDTO) {
        Note note = new Note();
        BeanUtils.copyProperties(noteDTO, note);
        if (noteDTO.getNoteBody() != null) {
            String str = noteDTO.getNoteBody().toString();
            note.setNoteBody(str);
        }
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getId, noteDTO.getNoteId())
                .eq(Note::getUserId, BaseContext.getCurrentId());
        noteMapper.update(note, queryWrapper);
    }

    @Transactional()
    @Override
    public void swapNoteOrder(Long myId, Long theOtherId) {
        //查询
        List<Long> list = new ArrayList<>();
        list.add(myId);
        list.add(theOtherId);
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, BaseContext.getCurrentId())
                .in(Note::getId, list);
        List<Note> notes = noteMapper.selectList(queryWrapper);
        if (notes.size() != 2) {
            throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
        }
        //表头不参与交换
        if (notes.get(0).getNoteOrder() == 0 || notes.get(1).getNoteOrder() == 0) {
            return;
        }
        //交换
        Integer tmp = notes.get(0).getNoteOrder();
        notes.get(0).setNoteOrder(notes.get(1).getNoteOrder());
        notes.get(0).setNoteName(notes.get(1).getNoteOrder().toString());
        notes.get(1).setNoteOrder(tmp);
        notes.get(1).setNoteName(tmp.toString());

        noteMapper.updateById(notes.get(0));
        noteMapper.updateById(notes.get(1));
    }

    @Override
    public void modifyNoteColNames(Long catalogId, List<String> colNames) {

    }

    @Override
    public List<NoteBO> listNote() {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, BaseContext.getCurrentId());
        List<Note> notes = noteMapper.selectList(queryWrapper);
        //转换类型
        List<NoteBO> noteBOS = transformNotesToNoteBOs(notes);
        return noteBOS;
    }

    @Override
    public List<NoteBO> listNoteByCatalogId(Integer catalogId) {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getCatalogId, catalogId)
                .eq(Note::getUserId, BaseContext.getCurrentId())
                .orderByAsc(Note::getNoteOrder);
        List<Note> notes = noteMapper.selectList(queryWrapper);
        List<NoteBO> noteBOS = transformNotesToNoteBOs(notes);
        return noteBOS;
    }

    public List<NoteBO> transformNotesToNoteBOs(List<Note> notes) {
        List<NoteBO> noteBOs = new ArrayList<>();
        for (Note note : notes) {
            NoteBO tmp = new NoteBO();
            //复制相同属性
            BeanUtils.copyProperties(note, tmp);

            if (note.getNoteBody() != null) {
                JSONObject jsonObject = (JSONObject) JSON.parse(note.getNoteBody());
                tmp.setNoteBody(jsonObject);
            }
            noteBOs.add(tmp);
        }
        return noteBOs;
    }
}
