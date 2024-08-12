package top.tangyh.lamp.system.vo.result.application;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

import static top.tangyh.lamp.model.constant.Condition.LIKE;

/**
 * <p>
 * 实体类
 * 资源接口
 * </p>
 *
 * @author zuihou
 * @since 2021-09-17
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"uri", "requestMethod"})
@Accessors(chain = true)
@AllArgsConstructor
public class ResourceApiVO {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 请求类型
     */
    @TableField(value = "request_method", condition = LIKE)
    private String requestMethod;

    /**
     * 接口路径;lamp-cloud版：uri需要拼接上gateway中路径前缀
     * lamp-boot版: uri需要不需要拼接前缀
     */
    @TableField(value = "uri", condition = LIKE)
    private String uri;

    @TableField(value = "code", condition = LIKE)
    private String code;
}
