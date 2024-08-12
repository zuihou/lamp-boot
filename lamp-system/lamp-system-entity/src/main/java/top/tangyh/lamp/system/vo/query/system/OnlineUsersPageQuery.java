package top.tangyh.lamp.system.vo.query.system;

import lombok.Data;

/**
 * 在线用户
 * @author tangyh
 * @since 2024/8/1 15:40
 */
@Data
public class OnlineUsersPageQuery {

    /**
     * 用户名;
     */
    private String username;
    private String nickName;
    private String sessionId;

}
