package com.waitnomore.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemTypeEnum {

    VISION("展望"),
    REALSUN("耀聖"),
    E_SAINT("醫聖"),
    HASH("杏翔"),
    UNKNOWN("未知");

    private final String description;
}
