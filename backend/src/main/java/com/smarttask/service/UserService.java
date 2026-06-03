package com.smarttask.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarttask.common.api.ResultCode;
import com.smarttask.common.constant.CommonConstants;
import com.smarttask.common.exception.BusinessException;
import com.smarttask.dto.UserUpdateDTO;
import com.smarttask.entity.User;
import com.smarttask.mapper.UserMapper;
import com.smarttask.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户管理服务
 *
 * 提供管理员对用户的增删改查、权限调整等功能
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PointsService pointsService;

    /**
     * 分页查询用户列表
     *
     * IPage<T>: MyBatis-Plus 的分页模型，包含当前页、总条数、总页数等信息
     * Page<T>: IPage 的实现类，构造参数为 (当前页码, 每页条数)
     *
     * LambdaQueryWrapper: MyBatis-Plus 的查询条件构造器，使用 Lambda 表达式避免字段名硬编码
     * 比如 eq(User::getStatus, 0) 对应 SQL 的 WHERE status = 0
     * 如果用传统方式写 "status" 字符串，重构时改了字段名不会报错，Lambda 方式重构时自动感知
     */
    public IPage<UserInfoVO> getUserPage(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 如果有关键词，模糊查询用户名、昵称、邮箱
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                   .or().like(User::getNickname, keyword)
                   .or().like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> userPage = userMapper.selectPage(page, wrapper);

        // 将 User 转换为 UserInfoVO（隐藏密码等敏感信息）
        return userPage.convert(this::convertToVO);
    }

    /**
     * 获取单个用户信息
     */
    public UserInfoVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return convertToVO(user);
    }

    /**
     * 管理员更新用户信息（不更新密码）
     */
    public void updateUser(Long id, UserUpdateDTO updateDTO) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 如果修改了权限等级，记录积分变动（VIP升级需要消耗积分）
        if (updateDTO.getLevel() != null && updateDTO.getLevel() > user.getLevel()) {
            int costPoints = (updateDTO.getLevel() - user.getLevel()) * 500;
            if (updateDTO.getLevel() >= 1 && user.getPoints() < costPoints) {
                throw new BusinessException(ResultCode.POINTS_INSUFFICIENT);
            }
            if (updateDTO.getLevel() >= 1) {
                pointsService.addPoints(user.getId(), -costPoints, CommonConstants.POINTS_UPGRADE,
                        "权限升级消耗 " + costPoints + " 积分");
            }
        }

        // 更新字段（只更新非空字段）
        if (updateDTO.getNickname() != null) user.setNickname(updateDTO.getNickname());
        if (updateDTO.getEmail() != null) user.setEmail(updateDTO.getEmail());
        if (updateDTO.getPhone() != null) user.setPhone(updateDTO.getPhone());
        if (updateDTO.getAvatar() != null) user.setAvatar(updateDTO.getAvatar());
        if (updateDTO.getStatus() != null) user.setStatus(updateDTO.getStatus());
        if (updateDTO.getLevel() != null) {
            user.setLevel(updateDTO.getLevel());
            // 根据等级自动设置角色
            user.setRole(switch (updateDTO.getLevel()) {
                case 3 -> CommonConstants.ROLE_SUPER_ADMIN;
                case 2 -> CommonConstants.ROLE_ADMIN;
                case 1 -> CommonConstants.ROLE_VIP_USER;
                default -> CommonConstants.ROLE_NORMAL_USER;
            });
        }

        userMapper.updateById(user);
    }

    /**
     * 管理员重置用户密码
     */
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /**
     * 删除用户（逻辑删除）
     */
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        // 不允许删除超级管理员
        if (CommonConstants.ROLE_SUPER_ADMIN.equals(user.getRole())) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "不能删除超级管理员");
        }
        userMapper.deleteById(id);
    }

    /**
     * User 转 UserInfoVO，过滤敏感字段（密码等）
     */
    private UserInfoVO convertToVO(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(user.getRole())
                .points(user.getPoints())
                .status(user.getStatus())
                .level(user.getLevel())
                .lastLoginIp(user.getLastLoginIp())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .build();
    }
}
