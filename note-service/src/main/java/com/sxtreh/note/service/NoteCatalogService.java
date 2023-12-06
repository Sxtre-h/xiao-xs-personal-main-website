package com.sxtreh.note.service;

import com.sxtreh.dto.NoteCatalogDTO;
import com.sxtreh.entity.NoteCatalog;

import java.util.List;

public interface NoteCatalogService {
    void saveCatalog(NoteCatalogDTO noteCatalogDTO);

    void deleteCatalog(Long catalogId);

    void modifyCatalog(NoteCatalogDTO noteCatalogDTO);

    List<NoteCatalog> listCatalog();
}
