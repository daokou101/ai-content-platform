package com.aicreator.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject meta) {
        this.strictInsertFill(meta, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(meta, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }
    @Override
    public void updateFill(MetaObject meta) {
        this.strictUpdateFill(meta, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }
}
