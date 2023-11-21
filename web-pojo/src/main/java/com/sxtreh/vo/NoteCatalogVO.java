package com.sxtreh.vo;

import com.sxtreh.entity.NoteCatalog;
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
public class NoteCatalogVO {
//    Long catalogId;
//
//    String catalogLevel;
//
//    String catalogName;
//
//    Long parentCatalogId;
//
//    Integer catalogOrder;
    Integer totals;

    List<NoteCatalog> noteCatalogs;

}
