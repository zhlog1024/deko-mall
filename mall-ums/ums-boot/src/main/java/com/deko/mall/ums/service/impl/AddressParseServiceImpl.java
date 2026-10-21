package com.deko.mall.ums.service.impl;

import com.deko.mall.common.web.exception.BizException;
import com.deko.mall.ums.config.UmsResultCode;
import com.deko.mall.ums.dto.AddressParseReq;
import com.deko.mall.ums.dto.AddressParseRes;
import com.deko.mall.ums.mapper.SysRegionMapper;
import com.deko.mall.ums.service.AddressParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智能地址识别实现
 * <p>
 * 支持常见电商地址复制格式：淘宝、京东、拼多多等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressParseServiceImpl implements AddressParseService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d{9})");

    /**
     * 电商平台常见标签前缀（+ 淘宝/京东复制格式）
     */
    private static final Pattern LABEL_PATTERN = Pattern.compile(
            "(收货人|收件人|收件方|联系人|姓名|名字|昵称|称呼|" +
                    "手机号|手机号码|手机|电话|联系电话|联系方式|号码|电话号|" +
                    "所在地区|详细地址|地址|" +
                    "收|寄)[：:：\\s=]*",
            Pattern.CASE_INSENSITIVE);

    private final SysRegionMapper sysRegionMapper;

    @Override
    public AddressParseRes parse(AddressParseReq req) {
        String raw = req.getRawAddress();
        if (raw == null || raw.isBlank()) {
            throw new BizException(UmsResultCode.ADDRESS_TEXT_REQUIRED);
        }

        AddressParseRes result = new AddressParseRes();

        // 1. 提取手机号
        Matcher phoneMatcher = PHONE_PATTERN.matcher(raw);
        if (phoneMatcher.find()) {
            String phone = phoneMatcher.group(1);
            result.setConsigneeMobile(phone);
            int phoneIdx = phoneMatcher.start();

            // 从手机号周边提取姓名
            String name = extractName(raw, phoneIdx, phone.length());
            if (name != null) {
                result.setConsigneeName(name);
            }

            // 提取地址文本（去除姓名、手机号、标签）
            String addressText = extractAddressText(raw, phoneIdx, phone.length(), name);
            matchRegion(addressText, result);
        } else {
            // 无手机号：全文当作地址匹配
            String clean = stripLabels(raw).replaceAll("[\\s：:，,。.、]+", "").trim();
            matchRegion(clean, result);
            if (result.getDetailAddress() != null && !result.getDetailAddress().isEmpty()) {
                extractNameFromStart(result);
            }
        }

        // 2. 兜底拼接详细地址
        if (result.getDetailAddress() == null || result.getDetailAddress().isEmpty()) {
            result.setDetailAddress(buildFallbackDetail(raw, result));
        }

        log.info("address parse result: {}", result);
        return result;
    }

    /**
     * 从手机号周围提取姓名（2-4 中文字符，取最靠近手机号的）
     */
    private String extractName(String text, int phoneIdx, int phoneLen) {
        int beforeStart = Math.max(0, phoneIdx - 20);
        String before = text.substring(beforeStart, phoneIdx);
        before = stripLabels(before);

        int afterEnd = Math.min(text.length(), phoneIdx + phoneLen + 20);
        String after = text.substring(phoneIdx + phoneLen, afterEnd);
        after = stripLabels(after);

        Pattern chineseName = Pattern.compile("[\\u4e00-\\u9fa5]{2,4}");

        // 手机号前取最后一个（最靠近）
        Matcher beforeMatcher = chineseName.matcher(before);
        String lastName = null;
        while (beforeMatcher.find()) {
            lastName = beforeMatcher.group();
        }
        if (lastName != null) return lastName;

        // 手机号后取第一个
        Matcher afterMatcher = chineseName.matcher(after);
        if (afterMatcher.find()) {
            return afterMatcher.group();
        }
        return null;
    }

    /**
     * 去掉姓名、手机号、标签 → 纯地址文本
     */
    private String extractAddressText(String raw, int phoneIdx, int phoneLen, String name) {
        String text = raw.substring(0, phoneIdx) + raw.substring(phoneIdx + phoneLen);
        if (name != null) {
            text = text.replace(name, "");
        }
        text = stripLabels(text);
        // 去掉空白、换行、标点（中文冒号：也去掉）
        text = text.replaceAll("[\\s：:，,。.、\n\r]+", "").trim();
        return text;
    }

    private String stripLabels(String text) {
        if (text == null) return "";
        return LABEL_PATTERN.matcher(text).replaceAll("");
    }

    private void extractNameFromStart(AddressParseRes result) {
        String detail = result.getDetailAddress();
        Pattern namePattern = Pattern.compile("^([\\u4e00-\\u9fa5]{2,4})(.+)");
        Matcher m = namePattern.matcher(detail);
        if (m.find()) {
            result.setConsigneeName(m.group(1));
            result.setDetailAddress(m.group(2));
        }
    }

    private String buildFallbackDetail(String raw, AddressParseRes result) {
        String remain = raw;
        if (result.getConsigneeName() != null) remain = remain.replace(result.getConsigneeName(), "");
        if (result.getConsigneeMobile() != null) remain = remain.replace(result.getConsigneeMobile(), "");
        if (result.getProvinceName() != null) remain = remain.replace(result.getProvinceName(), "");
        if (result.getCityName() != null) remain = remain.replace(result.getCityName(), "");
        if (result.getAreaName() != null) remain = remain.replace(result.getAreaName(), "");
        remain = stripLabels(remain);
        remain = remain.replaceAll("[\\s：:，,。.、\n\r]+", "").trim();
        return remain.isEmpty() ? null : remain;
    }

    // 省市区匹配

    private void matchRegion(String text, AddressParseRes result) {
        if (text == null || text.isEmpty()) return;

        // 匹配省（2 阶段：全名优先，短名兜底）
        List<SysRegionMapper.RegionSimple> provinces = sysRegionMapper.findProvinces();
        SysRegionMapper.RegionSimple matchedProvince = null;
        int provincePos = -1;
        int consumedLen = 0;

        for (SysRegionMapper.RegionSimple p : provinces) {
            int idx = text.indexOf(p.getName());
            if (idx >= 0 && p.getName().length() > consumedLen) {
                matchedProvince = p;
                provincePos = idx;
                consumedLen = p.getName().length();
            }
        }
        if (matchedProvince == null) {
            for (SysRegionMapper.RegionSimple p : provinces) {
                String sn = shortProvinceName(p.getName());
                int idx = text.indexOf(sn);
                if (idx >= 0 && sn.length() > consumedLen) {
                    matchedProvince = p;
                    provincePos = idx;
                    consumedLen = sn.length();
                }
            }
        }

        if (matchedProvince == null) {
            result.setDetailAddress(text);
            return;
        }

        result.setProvinceId(matchedProvince.getId());
        result.setProvinceName(matchedProvince.getName());
        String afterProvince = text.substring(provincePos + consumedLen);

        // 匹配市
        List<SysRegionMapper.RegionSimple> cities = sysRegionMapper.findChildrenByParentId(matchedProvince.getId());
        SysRegionMapper.RegionSimple matchedCity = null;
        boolean isMunicipality = false;

        // 直辖市检测：level 2 的 city 与 province 同名
        for (SysRegionMapper.RegionSimple c : cities) {
            if (c.getName().equals(matchedProvince.getName())) {
                isMunicipality = true;
                matchedCity = c;
                break;
            }
        }

        if (!isMunicipality) {
            // 常规省份：2 阶段 city 匹配
            int cityConsumed = 0;
            // 阶段 1：全名
            for (SysRegionMapper.RegionSimple c : cities) {
                int idx = afterProvince.indexOf(c.getName());
                if (idx >= 0 && idx < 10 && c.getName().length() > cityConsumed) {
                    matchedCity = c;
                    cityConsumed = c.getName().length();
                }
            }
            // 阶段 2：短名
            if (matchedCity == null) {
                for (SysRegionMapper.RegionSimple c : cities) {
                    String sn = shortCityName(c.getName());
                    if (sn.length() < 2) continue;
                    int idx = afterProvince.indexOf(sn);
                    if (idx >= 0 && idx < 10 && sn.length() > cityConsumed) {
                        matchedCity = c;
                        cityConsumed = sn.length();
                    }
                }
            }
            if (matchedCity != null) {
                // 试着用全名切，全名不存在则用短名长度
                int cutLen = cityConsumed;
                int fullIdx = afterProvince.indexOf(matchedCity.getName());
                if (fullIdx >= 0 && fullIdx < 10) {
                    cutLen = matchedCity.getName().length();
                }
                afterProvince = afterProvince.substring(afterProvince.indexOf(
                        fullIdx >= 0 && fullIdx < 10 ? matchedCity.getName() :
                                shortCityName(matchedCity.getName())) + cutLen);
            }
        }

        String afterCity = afterProvince;
        if (matchedCity != null) {
            result.setCityId(matchedCity.getId());
            result.setCityName(matchedCity.getName());

            // 匹配区（2 阶段）
            List<SysRegionMapper.RegionSimple> areas = sysRegionMapper.findChildrenByParentId(matchedCity.getId());
            SysRegionMapper.RegionSimple matchedArea = null;
            int areaConsumed = 0;

            // 阶段 1：全名
            for (SysRegionMapper.RegionSimple a : areas) {
                int idx = afterCity.indexOf(a.getName());
                if (idx >= 0 && idx < 10 && a.getName().length() > areaConsumed) {
                    matchedArea = a;
                    areaConsumed = a.getName().length();
                }
            }
            // 阶段 2：短名
            if (matchedArea == null) {
                for (SysRegionMapper.RegionSimple a : areas) {
                    String sn = shortAreaName(a.getName());
                    if (sn.length() < 2) continue;
                    int idx = afterCity.indexOf(sn);
                    if (idx >= 0 && idx < 10 && sn.length() > areaConsumed) {
                        matchedArea = a;
                        areaConsumed = sn.length();
                    }
                }
            }
            if (matchedArea != null) {
                result.setAreaId(matchedArea.getId());
                result.setAreaName(matchedArea.getName());
                // 全名优先切割
                int cutLen = areaConsumed;
                int fullIdx = afterCity.indexOf(matchedArea.getName());
                if (fullIdx >= 0 && fullIdx < 10) {
                    cutLen = matchedArea.getName().length();
                }
                afterCity = afterCity.substring(afterCity.indexOf(
                        fullIdx >= 0 && fullIdx < 10 ? matchedArea.getName() :
                                shortAreaName(matchedArea.getName())) + cutLen);
            }
        }

        result.setDetailAddress(afterCity.trim());
    }

    private static String shortProvinceName(String name) {
        return name.replace("省", "").replace("市", "")
                .replace("自治区", "").replace("特别行政区", "");
    }

    private static String shortCityName(String name) {
        return name.replace("市", "").replace("自治州", "")
                .replace("地区", "").replace("盟", "");
    }

    private static String shortAreaName(String name) {
        return name.replace("区", "").replace("县", "").replace("市", "");
    }
}
