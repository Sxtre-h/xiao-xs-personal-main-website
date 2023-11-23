package com.sxtreh.vo;

import com.sxtreh.entity.UserFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFileVO {
    UserFile file;
    //子文件数
    Integer totals;
    //TODO 直接返回实体对象可能不安全？空间占用也大，新建一个VO可能比较好，但是COPY会耗费后端性能
    List<UserFile> subFiles;
}
