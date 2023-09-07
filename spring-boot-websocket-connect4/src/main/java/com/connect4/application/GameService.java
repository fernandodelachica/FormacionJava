package com.connect4.application;

import com.connect4.domain.Game;
import com.connect4.domain.GamePlay;
import com.connect4.domain.Player;
import reactor.core.publisher.Mono;

public interface GameService {

    Mono<Game> createGame(Player player);

    Mono<Game> connectToGame(Player player2, String gameId);

    Mono<Game> gamePlay(GamePlay gamePlay);
}
