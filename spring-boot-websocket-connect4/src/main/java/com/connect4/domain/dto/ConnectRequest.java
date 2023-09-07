package com.connect4.domain.dto;

import com.connect4.domain.Player;
import lombok.Data;

@Data
public class ConnectRequest {
    private Player player;
    private String gameId;
}
