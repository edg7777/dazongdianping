package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;

/**
 * @author fzj
 * @date 2023-07-20 23:34
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {
    public Result seckillVoucher(Long voucherId);

    void createVoucherOrder(VoucherOrder voucherId);
}
