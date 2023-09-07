package com.connect4.application;

import com.connect4.domain.Game;
import com.connect4.domain.GamePlay;
import com.connect4.domain.Player;
import com.connect4.domain.utils.GameStatus;
import com.connect4.domain.utils.Token;
import com.connect4.exception.InvalidGameException;
import com.connect4.exception.NotFoundException;
import com.connect4.repository.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class GameServiceImpl implements GameService{

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public Mono<Game> createGame(Player player){
        Game game = new Game();
        player.setToken(Token.RED);
        game.setGameId(UUID.randomUUID().toString());
        game.setBoard(new int[6][7]);
        game.setPlayer1(player);
        game.setStatus(GameStatus.NEW);
        return gameRepository.save(game);
    }

    @Override
    public Mono<Game> connectToGame(Player player2, String gameId){
        Mono<Game> game = gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new NotFoundException("La partida con el id: "+gameId+" no existe.")));

        return game
                .flatMap(gameDoc -> {
                    if(gameDoc.getPlayer2() != null){
                        Mono.error(new InvalidGameException("La partida está completa."));
                    }
                    player2.setToken(Token.YELLOW);
                    gameDoc.setPlayer2(player2);
                    gameDoc.setStatus(GameStatus.RED_TURN);
                    log.info("El usuario "+gameDoc.getPlayer2()+" se ha conectado a la partida de: "+ gameDoc.getPlayer1()+" con el id: "+gameDoc.getGameId());
                    return gameRepository.save(gameDoc);
                });
    }

    @Override
    public Mono<Game> gamePlay(GamePlay gamePlay){
        return gameRepository.findById(gamePlay.getGameId())
                .switchIfEmpty(Mono.error(new NotFoundException("La partida con el id: "+gamePlay.getGameId()+" no existe.")))
                .flatMap(gameDoc -> {
                    if(gameDoc.getStatus().equals(GameStatus.FINISHED)){
                        return Mono.error(new InvalidGameException("La partida ha finalizado."));
                    }

                    if(!isValidMove(gameDoc, gamePlay.getColumn())){
                        return Mono.error(new InvalidGameException("Movimiento inválido"));
                    }

                    int column = gamePlay.getColumn();
                    int row = getNextAvailableRow(gameDoc, gamePlay.getColumn());
                    int board[][] = gameDoc.getBoard();

                    //Coloca la ficha
                    if(gamePlay.getType() == Token.RED){
                        board[row][column] = 1;
                    } else if(gamePlay.getType() == Token.YELLOW){
                        board[row][column] = 2;
                    }

                    //Valida quien ha ganado
                    Boolean winnerRed = checkWinner(gameDoc, Token.RED);
                    Boolean winnerYellow = checkWinner(gameDoc, Token.YELLOW);

                    if(winnerRed){
                        gameDoc.setWinner(gameDoc.getPlayer1().getUserName());
                        gameDoc.setStatus(GameStatus.FINISHED);
                    } else if (winnerYellow){
                        gameDoc.setWinner(gameDoc.getPlayer2().getUserName());
                        gameDoc.setStatus(GameStatus.FINISHED);
                    }

                    return gameRepository.save(gameDoc);
                });
    }

    private boolean isValidMove(Game game, int column){
        return column >= 0 && column < 7 && game.getBoard()[0][column] == 0;
    }

    private int getNextAvailableRow(Game game, int column){

        for (int row = 5; row >= 0; row --){
            if(game.getBoard()[row][column] == 0){
                return row;
            }
        }
        return -1;
    }

    public Boolean checkWinner(Game game, Token token){
        if(checkHorizontal(game, token) || checkVertical(game, token) || checkDiagonal(game, token)){
            return true;
        } else {
            return false;
        }
    }

    private Boolean checkHorizontal(Game game, Token token){
        int horizontalCounter = 0;

        for (int i = 0; i < game.getBoard().length; i++){

            for (int j = 0; j < game.getBoard()[0].length; j++){
                if(game.getBoard()[i][j] == token.getValue() ){
                    horizontalCounter++;
                    if(horizontalCounter >= 4){
                        return true;
                    }
                } else {
                    horizontalCounter = 0;
                }
            }
        }
        return false;
    }

    private Boolean checkVertical(Game game, Token token){
        int verticalCounter = 0;

        for (int i = 0; i < game.getBoard()[0].length; i++){
            for (int j = 0; j < game.getBoard().length; j++){
                if(game.getBoard()[j][i] == token.getValue()){
                    verticalCounter++;
                    if(verticalCounter >= 4){
                        return true;
                    }
                } else {
                    verticalCounter = 0;
                }
            }
        }
        return false;
    }

    private Boolean checkDiagonal(Game game, Token token){
        // Verifica las diagonales de izquierda a derecha (ascendentes)
        for(int i = 0; i < 3; i++){
            for (int j = 0; j <= 4; j++){
                // Comprueba si hay cuatro fichas del mismo jugador en diagonal ascendente
                if(game.getBoard()[i][j] == token.getValue()
                        && game.getBoard()[i + 1][j + 1] == token.getValue()
                        && game.getBoard()[i + 2][j + 2] == token.getValue()
                        && game.getBoard()[i + 3][j + 3] == token.getValue()){
                    return true;
                }
            }
        }

        // Verifica las diagonales de izquierda a derecha (descendentes)
        for (int i = 0; i < 3; i++){
            // Comprueba si hay cuatro fichas del mismo jugador en diagonal descendente
            for(int j = 6; j >= 4; j--){
                if(game.getBoard()[i][j] == token.getValue()
                        && game.getBoard()[i + 1][j - 1] == token.getValue()
                        && game.getBoard()[i + 2][j - 2] == token.getValue()
                        && game.getBoard()[i + 3][j - 3] == token.getValue()){
                    return true;
                }
            }
        }

        // Verifica las diagonales de derecha a izquierda (ascendentes)
        for (int i = 5; i > 3; i--){

            for(int j = 0; j <= 4; j++){
                if(game.getBoard()[i][j] == token.getValue()
                        && game.getBoard()[i - 1][j + 1] == token.getValue()
                        && game.getBoard()[i - 2][j + 2] == token.getValue()
                        && game.getBoard()[i - 3][j + 3] == token.getValue()){
                    return true;
                }
            }
        }

        // Verifica las diagonales de derecha a izquierda (descendentes)
        for (int i = 5; i > 3; i--){

            for (int j = 6; j >= 4; j--){
                if(game.getBoard()[i][j] == token.getValue()
                        && game.getBoard()[i - 1][j - 1] == token.getValue()
                        && game.getBoard()[i - 2][j - 2] == token.getValue()
                        && game.getBoard()[i - 3][j - 3] == token.getValue()){
                    return true;
                }
            }
        }
        return false;
    }



}
