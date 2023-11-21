package com.sxtreh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Constant;
import com.google.common.base.Function;
import com.sxtreh.constant.CatalogLevel;
import com.sxtreh.constant.StatusConstant;
import com.sxtreh.context.BaseContext;
import com.sxtreh.dto.NoteCatalogDTO;
import com.sxtreh.entity.NoteCatalog;
import com.sxtreh.mapper.NoteCatalogMapper;
import com.sxtreh.service.NoteCatalogService;
import com.sxtreh.vo.NoteCatalogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteCatalogServiceImpl implements NoteCatalogService {
    @Autowired
    private NoteCatalogMapper noteCatalogMapper;

    @Override
    public void saveCatalog(NoteCatalogDTO noteCatalogDTO) {
        NoteCatalog noteCatalog = new NoteCatalog();
        BeanUtils.copyProperties(noteCatalogDTO, noteCatalog);

        noteCatalog.setUserId(BaseContext.getCurrentId());
        noteCatalog.setStatus(StatusConstant.UNSHARED);
        noteCatalog.setCatalogOrder(1);//TODO 临时填充

        noteCatalogMapper.insert(noteCatalog);
    }

    //TODO 删除目录下的所有内容
    @Override
    public void deleteCatalog(Long catalogId) {
        noteCatalogMapper.deleteById(catalogId);
    }

    @Override
    public void modifyCatalog(NoteCatalogDTO noteCatalogDTO) {
        NoteCatalog noteCatalog = new NoteCatalog();
        BeanUtils.copyProperties(noteCatalogDTO, noteCatalog);
        //TODO 目录排序
        LambdaQueryWrapper<NoteCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteCatalog::getId, noteCatalogDTO.getCatalogId());
        noteCatalogMapper.update(noteCatalog, queryWrapper);
    }

    @Override
    public List<NoteCatalog> listCatalog() {
        LambdaQueryWrapper<NoteCatalog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteCatalog::getUserId, BaseContext.getCurrentId());
        return noteCatalogMapper.selectList(queryWrapper);
    }
}
