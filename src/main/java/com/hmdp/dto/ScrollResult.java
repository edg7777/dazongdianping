package com.hmdp.dto;

import lombok.Data;

import java.util.List;

/**
 * @author fzj
 * @date 2023-07-24 22:23
 */
@Data

public class ScrollResult {
    private List<?> list;
    private long minTime;
    private Integer offset;
}
