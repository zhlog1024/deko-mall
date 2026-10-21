package com.deko.mall.ums.controller.app;

import com.deko.mall.common.result.Result;
import com.deko.mall.ums.dto.AddressParseReq;
import com.deko.mall.ums.dto.AddressParseRes;
import com.deko.mall.ums.dto.MemberAddressDTO;
import com.deko.mall.ums.model.entity.UmsAddress;
import com.deko.mall.ums.model.form.AddressForm;
import com.deko.mall.ums.service.AddressParseService;
import com.deko.mall.ums.service.UmsAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "App-会员地址")
@RestController
@RequestMapping("/app-api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final UmsAddressService addressService;
    private final AddressParseService addressParseService;

    @Operation(summary = "智能地址识别")
    @PostMapping("/parse")
    public Result<AddressParseRes> parseAddress(@RequestBody AddressParseReq req) {
        AddressParseRes result = addressParseService.parse(req);
        return Result.success(result);
    }

    @Operation(summary = "获取当前会员地址列表")
    @GetMapping
    public Result<List<MemberAddressDTO>> listCurrentMemberAddresses() {
        List<MemberAddressDTO> addressList = addressService.listCurrentMemberAddresses();
        return Result.success(addressList);
    }

    @Operation(summary = "获取地址详情")
    @GetMapping("/{addressId}")
    public Result<UmsAddress> getAddressDetail(
            @Parameter(name = "地址ID") @PathVariable Long addressId
    ) {
        UmsAddress umsAddress = addressService.getById(addressId);
        return Result.success(umsAddress);
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public Result addAddress(
            @RequestBody @Validated AddressForm addressForm
    ) {
        boolean result = addressService.addAddress(addressForm);
        return Result.judge(result);
    }

    @Operation(summary = "修改地址")
    @PutMapping("/{addressId}")
    public Result updateAddress(
            @Parameter(name = "地址ID") @PathVariable Long addressId,
            @RequestBody @Validated AddressForm addressForm
    ) {
        boolean result = addressService.updateAddress(addressForm);
        return Result.judge(result);
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{ids}")
    public Result deleteAddress(
            @Parameter(name = "地址ID，过个以英文逗号(,)分割") @PathVariable String ids
    ) {

        boolean status = addressService.deletedByIds(Arrays.asList(ids.split(",")));
        return Result.judge(status);
    }

}
