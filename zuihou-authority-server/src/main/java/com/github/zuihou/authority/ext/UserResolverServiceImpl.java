package com.github.zuihou.authority.ext;

import com.github.zuihou.authority.service.auth.UserService;
import com.github.zuihou.base.R;
import com.github.zuihou.user.feign.UserQuery;
import com.github.zuihou.user.feign.UserResolverService;
import com.github.zuihou.user.model.SysUser;

/**
 * 本地 实现
 *
 * @author zuihou
 * @date 2020年02月24日10:51:46
 */
public class UserResolverServiceImpl implements UserResolverService {
    private final UserService userService;

    public UserResolverServiceImpl(UserService userService) {
        this.userService = userService;
    }


    @Override
    public R<SysUser> getById(Long id, UserQuery userQuery) {
        return userService.getUserById(id, userQuery);
    }
}
