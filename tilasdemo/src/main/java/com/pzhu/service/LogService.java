package com.pzhu.service;

import com.pzhu.pojo.OperateLog;

/**
 * 操作日志的业务层接口
 * <p>
 * 【什么是 Service 层？】
 * Service 是"业务逻辑层"，负责处理具体的业务规则。
 * Controller（控制层）接收请求 → 调用 Service 处理业务 → Service 调用 DAO 操作数据库。
 * <p>
 * 这里定义日志相关的业务方法，目前只有"保存日志"这一个功能。
 * 日志记录是通过 AOP 切面自动触发的（见 LogAspect 类），
 * 但在需要手动记录日志的场景下也可以调用这个接口。
 */
public interface LogService {

    /**
     * 保存操作日志
     *
     * @param log 操作日志对象
     */
    void saveLog(OperateLog log);
}
