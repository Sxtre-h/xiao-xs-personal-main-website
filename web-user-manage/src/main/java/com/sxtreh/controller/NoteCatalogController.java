package com.sxtreh.controller;

import com.sxtreh.annotation.RequireLogin;
import com.sxtreh.constant.MessageConstant;
import com.sxtreh.dto.NoteCatalogDTO;
import com.sxtreh.entity.NoteCatalog;
import com.sxtreh.exception.ParameterMissingException;
import com.sxtreh.result.Result;
import com.sxtreh.service.NoteCatalogService;
import com.sxtreh.vo.NoteCatalogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 笔记目录管理
 * TODO 用户数据隔离
 */
@Slf4j
@RequestMapping("/note/catalogs")
@RestController
public class NoteCatalogController {
    @Autowired
    private NoteCatalogService noteCatalogService;

    /**
     * 新增目录
     * @param noteCatalogDTO
     * @return
     */
    @RequireLogin
    @PostMapping
    public Result<NoteCatalogVO> saveCatalog(@RequestBody NoteCatalogDTO noteCatalogDTO){
        if(noteCatalogDTO.getCatalogLevel() == null || noteCatalogDTO.getCatalogName() == null
        || (!noteCatalogDTO.getCatalogLevel().equals("0") && noteCatalogDTO.getParentCatalogId() == null)){//目录非0级目录且没有指定父目录
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        noteCatalogService.saveCatalog(noteCatalogDTO);
        return Result.success();
    }

    /**
     * 删除目录
     * @param noteCatalogDTO
     * @return
     */
    @RequireLogin
    @DeleteMapping
    public Result<NoteCatalogVO> deleteCatalog(@RequestBody NoteCatalogDTO noteCatalogDTO){
        if(noteCatalogDTO.getCatalogId() == null){
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        noteCatalogService.deleteCatalog(noteCatalogDTO.getCatalogId());
        return Result.success();
    }

    /**
     * 修改目录
     * @param noteCatalogDTO
     * @return
     */
    @RequireLogin
    @PutMapping
    public Result<NoteCatalogVO> modifyCatalog(@RequestBody NoteCatalogDTO noteCatalogDTO){
        if(noteCatalogDTO.getCatalogId() == null || noteCatalogDTO.getCatalogName() == null){
            throw new ParameterMissingException(MessageConstant.PARAMETER_MISSING);
        }
        noteCatalogService.modifyCatalog(noteCatalogDTO);
        return Result.success();
    }

    /**
     * 查找目录
     * @return
     */
    @RequireLogin
    @GetMapping
    public Result<NoteCatalogVO> listCatalog(){
        List<NoteCatalog> noteCatalogs = noteCatalogService.listCatalog();
        NoteCatalogVO noteCatalogVO = new NoteCatalogVO(noteCatalogs.size(), noteCatalogs);
        return Result.success(noteCatalogVO);
    }

}
