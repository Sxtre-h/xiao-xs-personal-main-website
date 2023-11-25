package com.sxtreh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteCatalogDTO {
    Long catalogId;

    Integer catalogLevel;

    String catalogName;

    Long parentCatalogId;

    Integer catalogOrder;

}
