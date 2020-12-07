package com.tangyh.lamp.gateway.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangyh.basic.base.entity.Entity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import static com.baomidou.mybatisplus.annotation.SqlCondition.LIKE;

/**
 * 限流
 *
 * @author zuihou
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("e_rate_limiter")
@ApiModel(value = "RateLimiter", description = "限流")
public class RateLimiter extends Entity<Long> {

    public static final String METHOD_ALL = "ALL";

    /**
     * 请求URI
     */
    @ApiModelProperty(value = "请求URI")
    @Length(max = 255, message = "请求URI不能超过255")
    @TableField(value = "request_uri", condition = LIKE)
    private String requestUri;
    /**
     * 请求方法，如果为ALL则表示对所有方法生效
     */
    @ApiModelProperty(value = "请求方法")
    @Length(max = 10, message = "请求方法不能超过10")
    @TableField(value = "request_method", condition = LIKE)
    private String requestMethod;
    /**
     * 限制时间起
     */
    @ApiModelProperty(value = "限制时间起")
    @Length(max = 8, message = "限制时间起不能超过8")
    @TableField(value = "limit_start", condition = LIKE)
    private String limitStart;
    /**
     * 限制时间止
     */
    @ApiModelProperty(value = "限制时间止")
    @Length(max = 8, message = "限制时间止不能超过8")
    @TableField(value = "limit_end", condition = LIKE)
    private String limitEnd;
    /**
     * 次数
     */
    @ApiModelProperty(value = "次数")
    @TableField(value = "count", condition = LIKE)
    private Integer count;
    /**
     * 时间周期，单位秒
     */
    @ApiModelProperty(value = "时间周期")
    @TableField(value = "interval_sec")
    private Long intervalSec;
    /**
     * 状态，0关闭，1开启
     */
    @ApiModelProperty(value = "状态")
    @TableField(value = "state")
    private Boolean state;

}
