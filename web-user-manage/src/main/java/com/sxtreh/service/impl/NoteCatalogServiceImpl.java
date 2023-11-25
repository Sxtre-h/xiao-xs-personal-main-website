package com.sxtreh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.constant.StatusConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.dto.NoteCatalogDTO;
import com.sxtreh.entity.Note;
import com.sxtreh.entity.NoteCatalog;
import com.sxtreh.exception.DataNotExistException;
import com.sxtreh.exception.ParameterErrorException;
import com.sxtreh.mapper.NoteCatalogMapper;
import com.sxtreh.mapper.NoteMapper;
import com.sxtreh.service.NoteCatalogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteCatalogServiceImpl implements NoteCatalogService {
    @Autowired
    private NoteCatalogMapper noteCatalogMapper;
    @Autowired
    private NoteMapper noteMapper;

    @Override
    public void saveCatalog(NoteCatalogDTO noteCatalogDTO) {
        //对非零级目录,需要验证父目录是否存在
        if (!noteCatalogDTO.getCatalogLevel().equals("0")) {
            LambdaQueryWrapper<NoteCatalog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(NoteCatalog::getId, noteCatalogDTO.getParentCatalogId())
                    .eq(NoteCatalog::getUserId, BaseContext.getCurrentId());
            List<NoteCatalog> noteCatalogs = noteCatalogMapper.selectList(queryWrapper);
            //父目录不存在或者不唯一
            if (noteCatalogMapper.selectList(queryWrapper).size() != 1) {
                throw new DataNotExistException(MessageConstant.DATA_NOT_EXIST);
            }
            //要求父目录仅比自己高一级
            if(noteCatalogDTO.getCatalogLevel() - noteCatalogs.get(0).getCatalogLevel() != 1){
                throw new ParameterErrorException(MessageConstant.PARAMETER_ERROR);
            }
        }
        NoteCatalog noteCatalog = new NoteCatalog();
        BeanUtils.copyProperties(noteCatalogDTO, noteCatalog);

        noteCatalog.setUserId(BaseContext.getCurrentId());
        noteCatalog.setStatus(StatusConstant.UNSHARED);
        noteCatalog.setCatalogOrder(1);//TODO 临时填充

        noteCatalogMapper.insert(noteCatalog);
    }

    @Override
    public void deleteCatalog(Long catalogId) {
        LambdaQueryWrapper<NoteCatalog> catalogQueryWrapper = new LambdaQueryWrapper<>();
        catalogQueryWrapper.eq(NoteCatalog::getId, catalogId)
                .eq(NoteCatalog::getUserId, BaseContext.getCurrentId());
        //删除目录
        noteCatalogMapper.delete(catalogQueryWrapper);
        //删除目录下的所有笔记
        LambdaQueryWrapper<Note> noteQueryWrapper = new LambdaQueryWrapper<>();
        noteQueryWrapper.eq(Note::getCatalogId, catalogId)
                .eq(Note::getUserId, BaseContext.getCurrentId());
        noteMapper.delete(noteQueryWrapper);
        //递归删除子目录及所有相关笔记
        LambdaQueryWrapper<NoteCatalog> subCatalogQueryWrapper = new LambdaQueryWrapper<>();
        subCatalogQueryWrapper.eq(NoteCatalog::getParentCatalogId, catalogId)
                .eq(NoteCatalog::getUserId, BaseContext.getCurrentId());
        List<NoteCatalog> noteCatalogs = noteCatalogMapper.selectList(subCatalogQueryWrapper);
        for (NoteCatalog noteCatalog : noteCatalogs) {
            deleteCatalog(noteCatalog.getId());
        }
    }

    @Override
    public void modifyCatalog(NoteCatalogDTO noteCatalogDTO) {
        NoteCatalog noteCatalog = new NoteCatalog();
        BeanUtils.copyProperties(noteCatalogDTO, noteCatalog);
        //TODO 目录排序
        LambdaQueryWrapper<NoteCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteCatalog::getId, noteCatalogDTO.getCatalogId())
                .eq(NoteCatalog::getUserId, BaseContext.getCurrentId());
        noteCatalogMapper.update(noteCatalog, queryWrapper);
    }

    @Override
    public List<NoteCatalog> listCatalog() {
        LambdaQueryWrapper<NoteCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteCatalog::getUserId, BaseContext.getCurrentId());
        return noteCatalogMapper.selectList(queryWrapper);
    }
}
