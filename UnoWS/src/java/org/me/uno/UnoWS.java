/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.uno;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author felipebrizola
 */
@WebService(serviceName = "UnoWS")
public class UnoWS implements IUno{

    private static int MAX_PLAYERS = 500;
    private ArrayList<Player> playersPool = new ArrayList<>();
    private ArrayList<Game> games = new ArrayList<>();
    
    private final HashMap<String, String> preRegister = new HashMap<>();

    
    private synchronized Game getGameByPlayerId(int playerId) {

        for (Game game : games) {
            for (Player player : game.getPlayers()) {
                if (player.getId() == playerId) {
                    return game;
                }
            }
        }

        return null;
    }

    public synchronized void removeClosedGames() {
        new Thread() {
            public void run() {
                try {
                    while (true) {

                        // 1 min p destruir partida
                        Thread.sleep(60000);
                        long now = System.currentTimeMillis();

                        System.out.println("ROTINA PARA REMOVER JOGOS FINALIZADOS");

                        for (int i = 0; i < games.size(); i += 1) {
                            if ((games.get(i).getStatus() == GameStatus.CLOSED)
                                    && (now - games.get(i).getClosedTimer() > 20000)) {
                                removePlayersFromPlayerPool();
                                games.remove(i);
                                System.out.println("JOGO REMOVIDO");
                            }
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void removePlayersFromPlayerPool() {
        for (int i = 0; i < playersPool.size(); i += 1) {
            for (Player player : games.get(i).getPlayers()) {
                if (player.getId() == playersPool.get(i).getId()) {
                    playersPool.remove(i);
                }
            }
        }
    }

    private void removeGameByPlayerId(int playerId) {
        for (int i = 0; i < games.size(); i += 1) {
            if (games.get(i).getPlayerByPlayerId(playerId).getId() == playerId) {
                games.remove(i);
            }
        }

    }

    // Aloca jogador em alguma partida nao inicializada ou cria uma com ele
    // mesmo
    private synchronized Game allocatesPlayer(Player newPlayer) throws Exception {
        boolean wasAllocated = false;

        if (games.size() > 0) {
            for (Game game : games) {

                System.out.println("ROTINA DE ALOCACAO");
                System.out.println("PARAMETRO ENVIADO" + newPlayer.getId());

                // entra segundo jogador
                if (game.getStatus() == GameStatus.WAITING && game.getPlayers().size() == 1) {

                    System.out.println("ADICIONANDO " + newPlayer.getId() + "A PARTIDA");
                    game.addOpponent(newPlayer);
                    wasAllocated = true;
                    game.watchTurnTimer();
                    break;
                }

            }

        }

        if (!wasAllocated) {
            System.out.println("NOVA PARTIDA: " + newPlayer.getId());
            Game game = new Game(newPlayer);
            game.watchGameTimer();
            return game;
        }

        return null;
    }

    // remove carta do deck do jogador e a coloca no topo da pilha de descarte
    private void playCardFull(int playerId, int cardIndex, Card playedCard) {
        Stack<Card> tableDeck = this.getGameByPlayerId(playerId).getTableDeck();
        tableDeck.push(playedCard);
        this.getGameByPlayerId(playerId).setTableDeck(tableDeck);
        this.getGameByPlayerId(playerId).getPlayerByPlayerId(playerId).getDeck().remove(cardIndex);
    }

    private int match(Card playedCard, Card tableCard, int playerId) {

        if ((playedCard.getColor() != null)
                && this.getGameByPlayerId(playerId).getActiveColor() == playedCard.getColor().getValue()) {
            return 0;
        }

        if (playedCard.getType() == TypeCard.Cg) {
            return 3;
        }

        if (playedCard.getType() == TypeCard.C4) {
            return 4;
        }

        if (playedCard.getType() != null) {

            if (playedCard.getNumber() == -1 && ((tableCard.getColor() == playedCard.getColor())
                    || (tableCard.getType() == playedCard.getType()))) {
                switch (playedCard.getType()) {
                    case M2:
                        return 2;

                    case Pu:
                        return 1;
                    case In:
                        return 1;

                    default:
                        break;
                }
            }

            return -1;
        }

        if ((tableCard.getColor() == playedCard.getColor()) || (tableCard.getNumber() == playedCard.getNumber())) {
            return 0;
        }

        return -1;
    }
        
    @Override
    public int preRegistro(String playerNameOne, int playerOneId, String playerNameTwo, int playerTwoId) {
        preRegister.put(playerNameOne, playerOneId + ";" + playerTwoId);
        preRegister.put(playerNameTwo, playerTwoId + ";" + playerOneId);

        return 0;
    }

    @Override
    public int registraJogador(String playerName) throws RemoteException {
       String id = "";
        Game game = null;
        Player newPlayer = null;
        
        if(preRegister.containsKey(playerName)) {
            id = preRegister.get(playerName);
            
            preRegister.remove(playerName);
            
            game = getGameByPlayerId(Integer.parseInt(id.split(";")[1]));
            newPlayer = new Player(playerName, Integer.parseInt(id.split(";")[0]));
            
            if (game == null) {
                game = new Game(newPlayer);
                game.watchGameTimer();
                games.add(game);
            }
                
            else {
                game.addOpponent(newPlayer);
                game.watchTurnTimer();
            }
                
            
            playersPool.add(newPlayer);
            
            return newPlayer.getId();
        }
        
        if (playersPool.size() > MAX_PLAYERS) {
            return -2;
        }

        for (Player player : playersPool) {
            if (player.getName().equals(playerName))
                return -1;
            
        }
        
        // id do jogador sera o indice da lista
        newPlayer = new Player(playerName, playersPool.size());

        try {
            game = allocatesPlayer(newPlayer);
            if (game != null)
                games.add(game);

            System.out.println("QUANTIDADE DE JOGOS NO SERVIDOR " + games.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        playersPool.add(newPlayer);

        return newPlayer.getId();
    }

    @Override
    public int encerraPartida(int playerId) throws RemoteException {
        ArrayList<Player> playersFromGame = this.getGameByPlayerId(playerId).getPlayers();

        for (Player playerFromGame : playersFromGame) {
            for (int i = 0; i < this.playersPool.size(); i += 1) {
                if (this.playersPool.get(i).getId() == playerFromGame.getId()) 
                    this.playersPool.remove(i);
                
            }
        }

        return 0;
    }

    @Override
    public int temPartida(int playerId) throws RemoteException {
        try {

            Game game = this.getGameByPlayerId(playerId);

            // acabou o tempo de espera para inicializar partida
            if (game != null && game.getStatus() == GameStatus.TIMEOUT) {
                removeGameByPlayerId(playerId);
                for (int i = 0; i < playersPool.size(); i += 1) {
                    if (playersPool.get(i).getId() == playerId) 
                        playersPool.remove(i);
                    
                }
                return -2;
            }

            // aguarda partida
            if (game != null && game.getPlayers().size() == 1)
                return 0;
            

            // decide qual jogador comeca jogando.
            if (game != null && game.getPlayers().size() == 2) {
                if (game.getPlayerByPlayerId(playerId).getId() < game.getOpponentByPlayerId(playerId).getId()) {
                    game.getPlayerByPlayerId(playerId).setIsMyTurn(true);
                    return 1;
                }
                game.getOpponentByPlayerId(playerId).setIsMyTurn(true);
                return 2;
            }

            return 0;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    @Override
    public String obtemOponente(int playerId) throws RemoteException {
         for (Player player : this.getGameByPlayerId(playerId).getPlayers()) {
            if (player.getId() != playerId) {
                return player.getName();
            }
        }

        return "";
    }

    @Override
    public int ehMinhaVez(int playerId) throws RemoteException {
        try {

            Game game = getGameByPlayerId(playerId);

            // playerId venceu por wo
            if (playerId == game.getWoPlayers()[0]) {
                game.setClosedTimer(System.currentTimeMillis());
                game.setStatus(GameStatus.CLOSED);
                return 6;
            }

            // playerId perdeu por wo
            if (playerId == game.getWoPlayers()[1]) {
                game.setClosedTimer(System.currentTimeMillis());
                game.setStatus(GameStatus.CLOSED);
                return 5;
            }

            // nao existem 2 jogadores na partida
            if (game.getPlayers().size() != 2) {
                return -2;
            }

            // vencedor
            if (this.obtemNumCartas(playerId) == 0) {
                game.setClosedTimer(System.currentTimeMillis());
                game.setStatus(GameStatus.CLOSED);
                return 2;
            } // perdedor
            else if (this.obtemNumCartasOponente(playerId) == 0) {
                game.setClosedTimer(System.currentTimeMillis());
                game.setStatus(GameStatus.CLOSED);
                return 3;
            } // jogadores ainda tem cartas, mas baralho de compra acabou
            else if (this.obtemNumCartas(playerId) > 0 && this.obtemNumCartasOponente(playerId) > 0
                    && this.obtemNumCartasBaralho(playerId) == 0) {
                game.setClosedTimer(System.currentTimeMillis());
                game.setStatus(GameStatus.CLOSED);
                return 4;
            }

            Player opponent = game.getOpponentByPlayerId(playerId);
            Player currentPlayer = game.getPlayerByPlayerId(playerId);

            // vez do oponente do playerId
            if (opponent.getIsMyTurn() && !currentPlayer.getIsMyTurn()) {
                return 0;
            }

            // vez do playerId
            if (currentPlayer.getIsMyTurn() && !opponent.getIsMyTurn()) {
                return 1;
            }

            return 0;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    @Override
    public int obtemNumCartasBaralho(int playerId) throws RemoteException {
        Game game = this.getGameByPlayerId(playerId);
        return game.getDeck().size();
    }

    @Override
    public int obtemNumCartas(int playerId) throws RemoteException {
         try {
            Player player = this.getGameByPlayerId(playerId).getPlayerByPlayerId(playerId);
            return player.getDeck().size();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int obtemNumCartasOponente(int playerId) throws RemoteException {
         try {
            Player player = this.getGameByPlayerId(playerId).getOpponentByPlayerId(playerId);
            return player.getDeck().size();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String mostraMao(int playerId) throws RemoteException {
        String deckStr = "";
        Stack<Card> deck = null;
        
        Game game = this.getGameByPlayerId(playerId);
        deck = game.getPlayerByPlayerId(playerId).getDeck();
        
        for(int i = 0; i < deck.size(); i += 1) {
            if (i == deck.size())
                deckStr += Helper.cardToString(deck.get(i));
            else
                deckStr += Helper.cardToString(deck.get(i)) + "|";
                
        }
        return deckStr;
    }

    @Override
    public int obtemCorAtiva(int playerId) throws RemoteException {
        Game game = this.getGameByPlayerId(playerId);
        return game.getActiveColor();
    }

    @Override
    public int obtemPontos(int playerId) throws RemoteException {
        Game game = getGameByPlayerId(playerId);

        if (game == null) {
            return 2;
        }

        Player player = game.getPlayerByPlayerId(playerId);

        if (player == null) {
            return 1;
        }

        if (game.getStatus() != GameStatus.CLOSED) {
            return 3;
        }

        return Helper.sumScore(player.getDeck());
    }

    @Override
    public String obtemCartaMesa(int playerId) throws RemoteException {
        Game game = this.getGameByPlayerId(playerId);
        Card card = game.getTableDeck().peek();

        return Helper.cardToString(card);
    }

    @Override
    public int compraCarta(int playerId) throws RemoteException {
        // comprar carta
        // remove carta do baralho de compra
        // insere no baralho do playerId

        try {
            Game game = this.getGameByPlayerId(playerId);
            Player player = game.getPlayerByPlayerId(playerId);
            Card card = game.getDeck().pop();
            Stack<Card> playerDeck = player.getDeck();
            playerDeck.push(card);
            player.setDeck(playerDeck);
            
            // troca a vez apos compra de carta
            game.setTurnPlayer();
            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int obtemPontosOponente(int playerId) throws RemoteException {
        Game game = getGameByPlayerId(playerId);

        if (game == null) {
            return 2;
        }

        Player player = game.getOpponentByPlayerId(playerId);

        if (player == null) {
            return 1;
        }

        if (game.getStatus() != GameStatus.CLOSED) {
            return 3;
        }

        return Helper.sumScore(player.getDeck());
    }

    @Override
    public int jogaCarta(int playerId, int index, int cardColor) throws RemoteException {
        
        try {
        
            Game game = this.getGameByPlayerId(playerId);

            // set do time da jogada
            game.getPlayerByPlayerId(playerId).setTurnTime(System.currentTimeMillis());

            // troca a vez do jogador
//            if (index == -1) {
//                this.getGameByPlayerId(playerId).setTurnPlayer();
//                return 1;
//            }

            // algum jogador nao encontrado
            if (game.getPlayerByPlayerId(playerId) == null || game.getOpponentByPlayerId(playerId) == null) {
                return -1;
            }

            // parametros invalidos - indice do baralho
            if (index < -1 || index > this.obtemNumCartas(playerId)) {
                return -3;
            }

            // parametros invalidos - cor inexistente
            if (cardColor < -2 || cardColor > 3) {
                return -3;
            }

            // nao é vez do jogador ainda
            if (!game.getPlayerByPlayerId(playerId).getIsMyTurn()) {
                return -4;
            }

            Card tableCard = Helper.stringToCard(this.obtemCartaMesa(playerId));
            Card playedCard = this.getGameByPlayerId(playerId).getPlayerByPlayerId(playerId).getDeck().get(index);

            Player player = null;

            // seta cor ativa
            if (cardColor != -1) {
                game.setActiveColor(cardColor);
            }

            switch (this.match(playedCard, tableCard, playerId)) {
                // erro
                case -1:
                    return 0;
                // jogada normal
                case 0:
                    this.playCardFull(playerId, index, playedCard);
                    this.getGameByPlayerId(playerId).setTurnPlayer();
                    game.setActiveColor(-1);
                    break;
                // pular e inverter
                case 1:
                    this.playCardFull(playerId, index, playedCard);
                    game.setActiveColor(-1);
                    break;
                case 2:
                    // +2
                    // adversario compra 2 cartas e turno nao é trocado.
                    this.playCardFull(playerId, index, playedCard);

                    player = game.getOpponentByPlayerId(playerId);
                    this.compraCarta(player.getId());
                    this.compraCarta(player.getId());
                    game.setActiveColor(-1);
                    break;
                case 3:
                    // coringa
                    this.playCardFull(playerId, index, playedCard);
                    this.getGameByPlayerId(playerId).setTurnPlayer();
                    break;
                case 4:
                    // coringa +4
                    player = game.getOpponentByPlayerId(playerId);
                    this.compraCarta(player.getId());
                    this.compraCarta(player.getId());
                    this.compraCarta(player.getId());
                    this.compraCarta(player.getId());
                    this.getGameByPlayerId(playerId).setTurnPlayer();
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 1;

    }

}
