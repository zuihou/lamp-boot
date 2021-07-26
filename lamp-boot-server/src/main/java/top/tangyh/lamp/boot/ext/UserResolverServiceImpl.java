package top.tangyh.lamp.boot.ext;


import top.tangyh.basic.base.R;
import top.tangyh.basic.security.feign.UserQuery;
import top.tangyh.basic.security.feign.UserResolverService;
import top.tangyh.basic.security.model.SysUser;
import top.tangyh.lamp.authority.service.auth.UserService;

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
