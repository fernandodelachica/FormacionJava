package com.connect4;

import com.connect4.application.GameService;
import com.connect4.domain.Game;
import com.connect4.domain.GamePlay;
import com.connect4.domain.Player;
import com.connect4.domain.dto.ConnectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/start")
    public ResponseEntity<Mono<Game>> start(@RequestBody Player player){
        log.info("Start game request: {}", player);
        return ResponseEntity.ok().body(gameService.createGame(player));
    }

    @PostMapping("/connect")
    public ResponseEntity<Mono<Game>> connect(@RequestBody ConnectRequest request){
        log.info("Connect request: {}", request);
        return ResponseEntity.ok().body(gameService.connectToGame(request.getPlayer(), request.getGameId()));
    }

    @PostMapping("/gameplay")
    public ResponseEntity<Mono<Game>> gamePlay(@RequestBody GamePlay request){
        log.info("Gameplay request: {}", request);
        Mono<Game> game = gameService.gamePlay(request);
        messagingTemplate.convertAndSend("/topic/game-progress/"+game.map(Game::getGameId), game);
        return ResponseEntity.ok().body(game);
    }

}
