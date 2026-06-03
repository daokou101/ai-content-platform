package com.pzhu.aspect;

import com.pzhu.pojo.OperateLog;
import com.pzhu.service.LogService;
import com.pzhu.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 日志切面类 —— 自动记录操作日志
 * <p>
 * 【什么是 AOP？】
 * AOP（Aspect Oriented Programming）面向切面编程。
 * 可以理解为：在不修改原有代码的情况下，在方法执行前后"切入"一些额外的逻辑。
 * <p>
 * 【应用场景】
 * 日志记录、权限校验、性能监控、事务管理 等。
 * <p>
 * 【这个切面做了什么？】
 * 当 Controller 中的方法被调用时，自动记录：
 * - 谁做的操作（从 JWT 中解析用户名）
 * - 什么时间做的
 * - 调用了哪个类的哪个方法
 * - 传入了什么参数
 * - 返回了什么结果
 * - 花了多少时间
 * <p>
 * 【@Aspect 注解】
 * 标记这是一个切面类。
 * <p>
 * 【@Component 注解】
 * 让 Spring 管理这个类。
 * <p>
 * 【@Around 注解 + 切点表达式】
 * 注解中的表达式指定了"在哪些方法上生效"：
 * execution(* com.pzhu.controller.*.*(..))
 * - execution：在方法执行时切入
 * - 第一个 *：任意返回值
 * - com.pzhu.controller.*：controller 包下的所有类
 * - 第二个 *：所有方法
 * - (..)：任意参数
 * <p>
 * 也就是说：controller 包下的所有方法被调用时，都会执行这个切面逻辑。
 * 但排除了 LoginController.login()，因为登录操作不需要记录日志。
 */
@Slf4j // 日志
@Aspect // 标记为切面类
@Component // 标记为 Spring 组件
public class LogAspect {

    @Autowired // 注入日志服务，用于保存日志到数据库
    private LogService logService;

    @Autowired // 注入 HttpServletRequest 对象，用于获取请求头中的 token
    private HttpServletRequest request;

    /**
     * 环绕通知 —— 在方法执行前后都要做一些事情
     * <p>
     * 【ProceedingJoinPoint 参数】
     * 代表当前正在执行的方法，可以：
     * - getTarget().getClass().getName()：获取类名
     * - getSignature().getName()：获取方法名
     * - getArgs()：获取方法参数
     * - proceed()：执行目标方法（让原方法继续执行）
     * <p>
     *
     * @param joinPoint 连接点对象，封装了目标方法的信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法可能抛出的异常
     */
    @Around("execution(* com.pzhu.controller.*.*(..)) " +
            "&& !execution(* com.pzhu.controller.LoginController.login(..))")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {

        // ===== 前置操作：方法执行前 =====

        // 1. 获取操作时间
        LocalDateTime operateTime = LocalDateTime.now();

        // 2. 获取当前操作的用户（从 JWT 中解析）
        String operateUser = getCurrentUser();

        // 3. 获取类名
        String className = joinPoint.getTarget().getClass().getName();

        // 4. 获取方法名
        String methodName = joinPoint.getSignature().getName();

        // 5. 获取方法参数
        Object[] args = joinPoint.getArgs();
        String methodParams = Arrays.toString(args);

        // 记录开始时间（用于计算耗时）
        long startTime = System.currentTimeMillis();

        // ===== 执行目标方法 =====
        // proceed() 就是让原本的 Controller 方法继续执行
        // 比如如果是 DeptController.addDept()，这里就会执行 addDept() 方法
        Object result = joinPoint.proceed();

        // ===== 后置操作：方法执行后 =====

        // 6. 计算耗时
        long endTime = System.currentTimeMillis();
        Long costTime = endTime - startTime;

        // 7. 获取方法返回值
        String returnValue = result != null ? result.toString() : null;

        // 8. 自动推断操作描述
        String description = inferDescription(className, methodName);

        // 9. 构建日志对象
        OperateLog logEntry = new OperateLog();
        logEntry.setOperateUser(operateUser);
        logEntry.setOperateTime(operateTime);
        logEntry.setClassName(className);
        logEntry.setMethodName(methodName);
        logEntry.setMethodParams(methodParams);
        logEntry.setReturnValue(returnValue);
        logEntry.setCostTime(costTime);
        logEntry.setDescription(description);

        // 10. 保存日志到数据库
        logService.saveLog(logEntry);

        // 打印日志
        log.info("操作日志：{} - {} - {}ms", operateUser, description, costTime);

        // 返回目标方法的执行结果
        return result;
    }

    /**
     * 从 JWT 令牌中解析当前登录的用户名
     * <p>
     * 【怎么获取当前用户？】
     * 前端在请求头中携带了 token（JWT），
     * 我们从 request 中获取 token，然后解析出其中存放的用户名。
     *
     * @return 当前操作的用户名，如果获取失败返回"未知用户"
     */
    private String getCurrentUser() {
        try {
            // 从请求头中获取令牌
            String token = request.getHeader("token");
            if (token != null && !token.isEmpty()) {
                // 解析令牌获取声明信息
                Claims claims = JwtUtils.parseToken(token);
                // 返回用户名
                return (String) claims.get("username");
            }
        } catch (Exception e) {
            // 解析失败返回默认值
            log.warn("获取当前用户失败：{}", e.getMessage());
        }
        return "未知用户";
    }

    /**
     * 根据类名和方法名推断操作描述
     * <p>
     * 【为什么需要这个方法？】
     * 每个方法的名称就暗示了它的用途，比如：
     * - addDept → "新增部门"
     * - deleteById → "根据ID删除"
     * <p>
     * 通过方法名自动推断操作描述，就不用手动在每个方法上添加注解了。
     *
     * @param className  类名
     * @param methodName 方法名
     * @return 操作描述
     */
    private String inferDescription(String className, String methodName) {
        // 从类名中提取简单类名（去掉包名）
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
        // 去掉 "Controller" 后缀
        String module = simpleClassName.replace("Controller", "");

        StringBuilder desc = new StringBuilder();

        // 根据方法名前缀推断操作类型
        if (methodName.startsWith("add") || methodName.startsWith("insert")) {
            desc.append("新增");
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            desc.append("删除");
        } else if (methodName.startsWith("update") || methodName.startsWith("modify")) {
            desc.append("修改");
        } else if (methodName.startsWith("select") || methodName.startsWith("query")
                || methodName.startsWith("page") || methodName.startsWith("get")) {
            desc.append("查询");
        } else if (methodName.startsWith("upload")) {
            desc.append("上传");
        } else if (methodName.startsWith("login")) {
            desc.append("登录");
        } else {
            desc.append("操作");
        }

        // 追加模块名
        if ("Dept".equals(module)) {
            desc.append("部门");
        } else if ("Emp".equals(module)) {
            desc.append("员工");
        } else if ("Login".equals(module)) {
            desc.append("登录");
        } else if ("Upload".equals(module)) {
            desc.append("文件");
        }

        return desc.toString();
    }
}
