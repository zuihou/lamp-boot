package com.tangyh.lamp.boot.ext;


import com.tangyh.basic.base.R;
import com.tangyh.basic.security.feign.UserQuery;
import com.tangyh.basic.security.feign.UserResolverService;
import com.tangyh.basic.security.model.SysUser;
import com.tangyh.lamp.authority.service.auth.UserService;

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
        return R.success(userService.getSysUserById(id, userQuery));
    }
}
