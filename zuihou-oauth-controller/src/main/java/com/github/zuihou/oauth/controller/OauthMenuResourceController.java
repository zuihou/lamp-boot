package com.github.zuihou.oauth.controller;

import cn.hutool.core.collection.CollUtil;
import com.github.zuihou.authority.dto.auth.ResourceQueryDTO;
import com.github.zuihou.authority.dto.auth.RouterMeta;
import com.github.zuihou.authority.dto.auth.VueRouter;
import com.github.zuihou.authority.entity.auth.Menu;
import com.github.zuihou.authority.entity.auth.Resource;
import com.github.zuihou.authority.entity.auth.Role;
import com.github.zuihou.authority.service.auth.MenuService;
import com.github.zuihou.authority.service.auth.ResourceService;
import com.github.zuihou.authority.service.auth.RoleService;
import com.github.zuihou.base.R;
import com.github.zuihou.common.constant.BizConstant;
import com.github.zuihou.dozer.DozerUtils;
import com.github.zuihou.security.annotation.LoginUser;
import com.github.zuihou.security.model.SysUser;
import com.github.zuihou.utils.TreeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.zuihou.utils.StrPool.DEF_PARENT_ID;


/**
 * <p>
 * 前端控制器
 * 资源 角色 菜单
 * </p>
 *
 * @author zuihou
 * @date 2019-07-22
 */
@Slf4j
@RestController
@AllArgsConstructor
@Api(value = "OauthMenuResource", tags = "资源")
public class OauthMenuResourceController {
    private final DozerUtils dozer;
    private final ResourceService resourceService;
    private final MenuService menuService;
    private final RoleService roleService;

    /**
     * 查询用户可用的所有资源
     *
     * @param resource <br>
     *                 menuId 菜单 <br>
     *                 userId 当前登录人id
     * @return
     */
    @ApiOperation(value = "查询用户可用的所有资源", notes = "查询用户可用的所有资源")
    @GetMapping("/resource/visible")
    public R<Collection<String>> visible(ResourceQueryDTO resource, @ApiIgnore @LoginUser SysUser sysUser) {
        if (resource == null) {
            resource = new ResourceQueryDTO();
        }

        if (resource.getUserId() == null) {
            resource.setUserId(sysUser.getId());
        }
        List<Resource> resourceList = resourceService.findVisibleResource(resource);
        List<Role> roleList = roleService.findRoleByUserId(resource.getUserId());
        Collection<String> list = CollUtil.union(
                resourceList.stream().filter(Objects::nonNull).map(Resource::getCode).collect(Collectors.toSet()),
                roleList.stream().filter(Objects::nonNull).map(this::splicing).collect(Collectors.toSet())
        );
        return R.success(list);
    }

    private String splicing(Role role) {
        return BizConstant.ROLE_PREFIX + role.getCode();
    }

    /**
     * 查询用户可用的所有资源
     *
     * @param group  分组 <br>
     * @param userId 指定用户id
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "group", value = "菜单组", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "long", paramType = "query"),
    })
    @ApiOperation(value = "查询用户可用的所有菜单", notes = "查询用户可用的所有菜单")
    @GetMapping("/menu/menus")
    public R<List<Menu>> myMenus(@RequestParam(value = "group", required = false) String group,
                                 @RequestParam(value = "userId", required = false) Long userId,
                                 @ApiIgnore @LoginUser SysUser sysUser) {
        if (userId == null || userId <= 0) {
            userId = sysUser.getId();
        }
        List<Menu> list = menuService.findVisibleMenu(group, userId);
        List<Menu> tree = TreeUtil.buildTree(list);
        return R.success(tree);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "group", value = "菜单组", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "long", paramType = "query"),
    })
    @ApiOperation(value = "查询用户可用的所有菜单路由树", notes = "查询用户可用的所有菜单路由树")
    @GetMapping("/menu/router")
    public R<List<VueRouter>> myRouter(@RequestParam(value = "group", required = false) String group,
                                       @RequestParam(value = "userId", required = false) Long userId,
                                       @ApiIgnore @LoginUser SysUser sysUser) {
        if (userId == null || userId <= 0) {
            userId = sysUser.getId();
        }
        List<Menu> list = menuService.findVisibleMenu(group, userId);
        List<VueRouter> treeList = dozer.mapList(list, VueRouter.class);
        return R.success(TreeUtil.buildTree(treeList));
    }

    /**
     * 超管的路由菜单写死，后期在考虑配置在
     *
     * @return
     */
    @ApiOperation(value = "查询超管菜单路由树", notes = "查询超管菜单路由树")
    @GetMapping("/menu/admin/router")
    public R<List<VueRouter>> adminRouter() {
        return R.success(buildSuperAdminRouter());
    }

    private List<VueRouter> buildSuperAdminRouter() {
        List<VueRouter> tree = new ArrayList<>();
        List<VueRouter> children = new ArrayList<>();

        VueRouter tenant = new VueRouter();
        tenant.setPath("/defaults/tenant");
        tenant.setComponent("zuihou/defaults/tenant/Index");
        tenant.setHidden(false);
        // 没有name ，刷新页面后，切换菜单会报错：
        // [Vue warn]: Error in nextTick: "TypeError: undefined is not iterable (cannot read property Symbol(Symbol.iterator))"
        // found in
        // <TagsView> at src/layout/components/TagsView/index.vue
        tenant.setName("租户管理");
        tenant.setAlwaysShow(true);
        tenant.setMeta(RouterMeta.builder()
                .title("租户管理").breadcrumb(true).icon("")
                .build());
        tenant.setId(-2L);
        tenant.setParentId(DEF_PARENT_ID);
        children.add(tenant);

        VueRouter globalUser = new VueRouter();
        globalUser.setPath("/defaults/globaluser");
        globalUser.setComponent("zuihou/defaults/globaluser/Index");
        globalUser.setName("运营用户");
        globalUser.setHidden(false);
        globalUser.setMeta(RouterMeta.builder()
                .title("运营用户").breadcrumb(true).icon("")
                .build());
        globalUser.setId(-3L);
        globalUser.setParentId(DEF_PARENT_ID);
        children.add(globalUser);

        VueRouter defaults = new VueRouter();
        defaults.setPath("/defaults");
        defaults.setComponent("Layout");
        defaults.setHidden(false);
        defaults.setName("系统设置");
        defaults.setAlwaysShow(true);
        defaults.setMeta(RouterMeta.builder()
                .title("系统设置").icon("el-icon-coin").breadcrumb(true)
                .build());
        defaults.setId(-1L);
        defaults.setChildren(children);

        tree.add(defaults);
        return tree;
    }

}
