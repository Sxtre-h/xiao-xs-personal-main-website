package com.sxtreh.note.controller;

import com.sxtreh.dto.NoteCatalogDTO;
import com.sxtreh.entity.NoteCatalog;
import com.sxtreh.enumeration.ParameterRuleType;
import com.sxtreh.note.annotation.ParameterCheck;
import com.sxtreh.note.service.NoteCatalogService;
import com.sxtreh.result.Result;
import com.sxtreh.vo.NoteCatalogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记目录管理
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
    @ParameterCheck(rule = ParameterRuleType.NOTE_CATALOG_SAVE)
    @PostMapping
    public Result<NoteCatalogVO> saveCatalog(@RequestBody NoteCatalogDTO noteCatalogDTO){
        noteCatalogService.saveCatalog(noteCatalogDTO);
        return Result.success();
    }

    /**
     * 删除目录
     * @param noteCatalogDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.NOTE_CATALOG_DELETE)
    @DeleteMapping
    public Result<NoteCatalogVO> deleteCatalog(@RequestBody NoteCatalogDTO noteCatalogDTO){
        noteCatalogService.deleteCatalog(noteCatalogDTO.getCatalogId());
        return Result.success();
    }

    /**
     * 修改目录
     * @param noteCatalogDTO
     * @return
     */
    @ParameterCheck(rule = ParameterRuleType.NOTE_CATALOG_MODIFY)
    @PutMapping
    public Result<NoteCatalogVO> modifyCatalog(@RequestBody NoteCatalogDTO noteCatalogDTO){

        noteCatalogService.modifyCatalog(noteCatalogDTO);
        return Result.success();
    }

    /**
     * 查找目录
     * @return
     */
    @GetMapping
    public Result<NoteCatalogVO> listCatalog(){
        List<NoteCatalog> noteCatalogs = noteCatalogService.listCatalog();
        NoteCatalogVO noteCatalogVO = new NoteCatalogVO(noteCatalogs.size(), noteCatalogs);
        return Result.success(noteCatalogVO);
    }

}
