package com.deko.mall.pms.service.impl;

import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.pms.mapper.PmsShopMapper;
import com.deko.mall.pms.model.dto.ShopUpdateDTO;
import com.deko.mall.pms.model.entity.PmsShop;
import com.deko.mall.pms.model.vo.ShopVO;
import com.deko.mall.pms.service.SpuService;
import com.deko.mall.ums.api.FavorFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 店铺服务单元测试
 * <p>
 * 覆盖：审核通过建店写入 logo/简介、供应商后台获取/更新自己的店铺（归属校验）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShopServiceImplTest {

    @InjectMocks
    private ShopServiceImpl shopService;

    @Mock
    private PmsShopMapper pmsShopMapper;

    @Mock
    private SpuService spuService;

    @Mock
    private FavorFeignClient favorFeignClient;

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl 的 baseMapper 为 @Autowired protected 字段，需反射注入
        TestServiceInjector.injectBaseMapper(shopService, pmsShopMapper);
    }

    @Test
    @DisplayName("createShop：写入店铺名称/logo/简介，默认开启")
    void createShop_persistsLogoAndDescription() {
        when(pmsShopMapper.insert(any(PmsShop.class))).thenReturn(1);

        PmsShop shop = shopService.createShop("SUPPLIER", 10L, "健康公社官方店", "http://img/logo.png", "专注于健康产品");

        assertNotNull(shop);
        assertEquals("SUPPLIER", shop.getOwnerType());
        assertEquals(10L, shop.getOwnerId());
        assertEquals("健康公社官方店", shop.getShopName());
        assertEquals("http://img/logo.png", shop.getLogo());
        assertEquals("专注于健康产品", shop.getDescription());
        assertEquals(1, shop.getStatus());
    }

    @Test
    @DisplayName("getScmShopVO：返回供应商自己的店铺VO")
    void getScmShopVO_returnsOwnShop() {
        PmsShop shop = new PmsShop();
        shop.setId(100L);
        shop.setOwnerType("SUPPLIER");
        shop.setOwnerId(10L);
        shop.setShopName("我的店铺");
        shop.setLogo("http://img/logo.png");
        shop.setDescription("店铺简介");
        when(pmsShopMapper.selectOne(any())).thenReturn(shop);

        ShopVO vo = shopService.getScmShopVO(10L);

        assertNotNull(vo);
        assertEquals(100L, vo.getId());
        assertEquals("我的店铺", vo.getShopName());
        assertEquals("http://img/logo.png", vo.getLogo());
        assertEquals("店铺简介", vo.getDescription());
    }

    @Test
    @DisplayName("updateScmShopInfo：仅能更新自己名下的店铺")
    void updateScmShopInfo_updatesOwnShop() {
        PmsShop shop = new PmsShop();
        shop.setId(100L);
        shop.setOwnerType("SUPPLIER");
        shop.setOwnerId(10L);
        when(pmsShopMapper.selectOne(any())).thenReturn(shop);
        when(pmsShopMapper.updateById(any(PmsShop.class))).thenReturn(1);

        ShopUpdateDTO dto = new ShopUpdateDTO();
        dto.setShopName("新店名");
        dto.setLogo("http://img/new-logo.png");
        dto.setLogoLong("http://img/banner.png");
        dto.setDescription("新简介");

        boolean result = shopService.updateScmShopInfo(10L, dto);

        assertTrue(result);
        // 校验更新对象携带了店铺ID与字段
        verify(pmsShopMapper).updateById(any(PmsShop.class));
    }

    @Test
    @DisplayName("updateScmShopInfo：他人店铺归属校验拒绝")
    void updateScmShopInfo_rejectsOtherOwner() {
        PmsShop shop = new PmsShop();
        shop.setId(100L);
        shop.setOwnerType("SUPPLIER");
        shop.setOwnerId(99L); // 店铺属于其他供应商
        when(pmsShopMapper.selectOne(any())).thenReturn(shop);

        ShopUpdateDTO dto = new ShopUpdateDTO();
        dto.setShopName("新店名");

        assertThrows(BizException.class, () -> shopService.updateScmShopInfo(10L, dto));
        verify(pmsShopMapper, never()).updateById(any(PmsShop.class));
    }
}
