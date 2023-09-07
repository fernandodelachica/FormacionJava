package com.connect4.domain;

import com.connect4.domain.utils.Token;
import lombok.Data;

@Data
public class Player {

    private String userName;
    private Token token;
}
