package com.connect4.domain.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Token {

    RED(1), YELLOW(2);

    private Integer value;
}
