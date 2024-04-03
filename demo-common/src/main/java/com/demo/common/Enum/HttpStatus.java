package com.demo.common.Enum;

import lombok.Data;

public interface HttpStatus {
    /**
     * 操作成功
     */
    Integer SUCCESS = 0;

    /**
     * 操作失败
     */
    Integer FAILURE = -1;
}
