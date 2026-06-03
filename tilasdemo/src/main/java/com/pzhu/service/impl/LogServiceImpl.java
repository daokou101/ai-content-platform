package com.pzhu.service.impl;

import com.pzhu.dao.LogDao;
import com.pzhu.pojo.OperateLog;
import com.pzhu.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现类
 * <p>
 * 【@Service 注解】
 * 告诉 Spring：这是一个"业务类"，请将其创建为 Bean 并纳入 IOC 容器管理。
 * IOC（控制反转）容器是 Spring 的核心，它会管理所有 Bean 的创建和生命周期。
 * <p>
 * 【自动注入 @Autowired】
 * Spring 会自动创建 LogDao 的实例并"注入"到这里，
 * 我们不需要自己 new LogDao()，Spring 会帮我们做。
 */
@Service // 标记为 Spring 管理的 Bean
public class LogServiceImpl implements LogService {

    @Autowired // 自动注入 LogDao
    private LogDao logDao;

    /**
     * 保存操作日志到数据库
     *
     * @param log 操作日志对象
     */
    @Override
    public void saveLog(OperateLog log) {
        logDao.insert(log);
    }
}
