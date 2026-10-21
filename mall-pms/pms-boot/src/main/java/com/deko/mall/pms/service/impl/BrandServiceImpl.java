package com.deko.mall.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deko.mall.pms.mapper.PmsBrandMapper;
import com.deko.mall.pms.model.entity.PmsBrand;
import com.deko.mall.pms.service.BrandService;
import org.springframework.stereotype.Service;

@Service
public class BrandServiceImpl extends ServiceImpl<PmsBrandMapper, PmsBrand> implements BrandService {
}
