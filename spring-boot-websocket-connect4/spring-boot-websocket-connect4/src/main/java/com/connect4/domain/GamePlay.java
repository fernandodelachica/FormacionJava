package com.connect4.domain;

import com.connect4.domain.utils.Token;
import lombok.Data;

@Data
public class GamePlay {
    private String gameId;
    private Token type;
    private Integer column;
}
