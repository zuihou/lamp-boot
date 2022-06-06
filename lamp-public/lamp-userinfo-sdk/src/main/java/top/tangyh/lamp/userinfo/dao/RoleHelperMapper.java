package top.tangyh.lamp.userinfo.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.tangyh.basic.base.mapper.SuperMapper;
import top.tangyh.lamp.model.entity.base.SysRole;

import java.util.List;

/**
 * @author tangyh
 * @version v1.0
 * @date 2022/4/24 1:28 PM
 * @create [2022/4/24 1:28 PM ] [tangyh] [初始创建]
 */
@Repository
public interface RoleHelperMapper extends SuperMapper<SysRole> {


    /**
     * 根据员工-部门-角色关系，查询员工拥有的角色编码
     *
     * @param userId 员工ID
     * @return
     */
    List<String> selectRoleCodeFromOrgByUserId(@Param("userId") Long userId);

    /**
     * 根据员工-角色，查询员工拥有的角色编码
     *
     * @param userId 员工ID
     * @return
     */
    List<String> selectRoleCodeFromRoleByUserId(@Param("userId") Long userId);

    /**
     * 根据员工-主部门-角色关系，查询员工拥有的角色编码
     *
     * @param userId 员工ID
     * @return
     */
    List<String> selectRoleCodeFromMainOrgByUserId(@Param("userId") Long userId);


    /**
     * 查询员工是否拥有指定角色编码
     *
     * @param codes
     * @param userId
     * @return
     */
    long countRoleFormRole(@Param("codes") List<String> codes, @Param("userId") Long userId);

    /**
     * 查询员工是否拥有指定角色编码
     *
     * @param codes
     * @param userId
     * @return
     */
    long countRoleFormOrg(@Param("codes") List<String> codes, @Param("userId") Long userId);

    /**
     * 查询员工是否拥有指定角色编码
     *
     * @param codes
     * @param userId
     * @return
     */
    long countRoleFormMainOrg(@Param("codes") List<String> codes, @Param("userId") Long userId);

    /**
     * 根据员工-部门-角色关系，查询可用的资源ID
     * <p>
     * 部门或角色被禁用后，员工不在拥有此部门或角色的权限
     *
     * @param userId        员工ID
     * @param applicationId 应用ID
     * @return
     */
    List<Long> selectResourceIdFromOrgByUserId(@Param("userId") Long userId, @Param("applicationId") Long applicationId);

    /**
     * 根据员工-主部门-角色关系，查询可用的资源ID
     * <p>
     * 部门或角色被禁用后，员工不在拥有此部门或角色的权限
     *
     * @param userId        员工ID
     * @param applicationId 应用ID
     * @return
     */
    List<Long> selectResourceIdFromMainOrgByUserId(@Param("userId") Long userId, @Param("applicationId") Long applicationId);

    /**
     * 根据员工-角色，查询可用的资源ID
     * <p>
     * 部角色被禁用后，员工不在拥有此角色的权限
     *
     * @param userId        员工ID
     * @param applicationId 应用ID
     * @return
     */
    List<Long> selectResourceIdFromRoleByUserId(@Param("userId") Long userId, @Param("applicationId") Long applicationId);
}
