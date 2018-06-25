/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.uno;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import javax.jws.WebService;

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
        for (int i = 0; i < games.size(); i += 1) {
            if (games.get(i).getStatus() == GameStatus.EXCLUDED) {
                games.remove(i);
            }
        }
    }

    private synchronized void removePlayersFromPlayerPool() {
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

                    break;
                }

            }

        }

        if (!wasAllocated) {
            System.out.println("NOVA PARTIDA: " + newPlayer.getId());
            Game game = new Game(newPlayer);
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

        if (playedCard.getType() == TypeCard.Cg) {
            return 3;
        }

        if (playedCard.getType() == TypeCard.C4) {
            return 4;
        }
        
        if (tableCard.getType() != null && playedCard.getType() != null) {
            
            if (tableCard.getType() != playedCard.getType() && this.getGameByPlayerId(playerId).getActiveColor() != playedCard.getColor().getValue())
                return -1;
        }
        
        if (playedCard.getType() != null) {

            if (playedCard.getNumber() == -1 && ((this.getGameByPlayerId(playerId).getActiveColor() == playedCard.getColor().getValue())
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
        
        if ((playedCard.getColor() != null)
                && this.getGameByPlayerId(playerId).getActiveColor() == playedCard.getColor().getValue()) {
            return 0;
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
    public synchronized int registraJogador(String playerName) throws RemoteException {
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
                games.add(game);
            }
                
            else {
                game.addOpponent(newPlayer);
                
                // valida carta de inicio
                Card card = game.getTableDeck().peek();
                while (card.getType() != null && card.getType().getValue() >= 3) {

                    if (card.getType() == TypeCard.M2) {
                        Player op = game.getOpponentByPlayerId(newPlayer.getId());
                        compra(op.getId());
                        compra(op.getId());
                        break;
                    }

                    if (card.getType() == TypeCard.Cg || card.getType() == TypeCard.C4) {
                        card = game.getDeck().pop();
                        game.getTableDeck().push(card);
                    }

                    card = game.getTableDeck().peek();
                }

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
    public synchronized int encerraPartida(int playerId) throws RemoteException {
        Game game = this.getGameByPlayerId(playerId);
        
        for (int i = 0; i < this.playersPool.size(); i += 1) {
            if (this.playersPool.get(i).getId() == playerId) { 
                this.playersPool.remove(i);
                game.removePlayer(playerId);
                break;
            }
        }
        
        // remove partida quando 2 player encerram
        removeClosedGames();
            
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
                    return 1;
                }
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
            
            if (game.getDeck().isEmpty()) {
                
                Player p1 = game.getPlayerByPlayerId(playerId);
                Player p2 = game.getOpponentByPlayerId(playerId);
                
                if (p1.getSum() < p2.getSum())
                    return 3;
                
                if (p1.getSum() > p2.getSum())
                    return 2;
                
                if (p1.getSum() == p2.getSum())
                    return 4;
            }

            // vencedor
            if (game.getPlayerByPlayerId(playerId).getDeck().isEmpty()) {
                game.setClosedTimer(System.currentTimeMillis());
                game.setStatus(GameStatus.CLOSED);
                return 2;
            } // perdedor
            else if (game.getOpponentByPlayerId(playerId).getDeck().isEmpty()) {
                game.setClosedTimer(System.currentTimeMillis());
                game.setStatus(GameStatus.CLOSED);
                return 3;
            } // jogadores ainda tem cartas, mas baralho de compra acabou
            else if (!game.getPlayerByPlayerId(playerId).getDeck().isEmpty() 
                    && !game.getOpponentByPlayerId(playerId).getDeck().isEmpty()
                    && game.getDeck().isEmpty()) {
                game.setClosedTimer(System.currentTimeMillis());
                game.setStatus(GameStatus.CLOSED);
                return 4;
            }
            
            // se carta pula, inverte ou +2. Troca jogador inicio
            Card card = game.getTableDeck().peek();
            if (card.getType() != null && card.getType().getValue() < 4) {
                game.changeTurnPlayers();
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
        
        if (deckStr.length() > 0)
            return deckStr.substring(0, deckStr.length()-1).replaceAll("M2", "+2");

        return deckStr.replaceAll("M2", "+2");
    }

    @Override
    public int obtemCorAtiva(int playerId) throws RemoteException {
        Game game = this.getGameByPlayerId(playerId);
        try {
            return game.getActiveColor();
        } catch (NullPointerException e) {
            return -1;
        }
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

//        if (game.getStatus() != GameStatus.CLOSED) {
//            return 3;
//        }

        return Helper.sumScore(player.getDeck());
    }

    @Override
    public String obtemCartaMesa(int playerId) throws RemoteException {
        Game game = this.getGameByPlayerId(playerId);
        Card card = game.getTableDeck().peek();

        return Helper.cardToString(card).replaceAll("M2", "+2");
    }

    @Override
    public int compraCarta(int playerId) throws RemoteException {
        // comprar carta
        // remove carta do baralho de compra
        // insere no baralho do playerId

        try {
            Game game = this.getGameByPlayerId(playerId);
            
            if (game.getStatus() != GameStatus.RUNNING)
                return -2;
            
            Player player = game.getPlayerByPlayerId(playerId);
            
            if (player == null)
                return -1;
            
            if (!player.getIsMyTurn())
                return -3;
            
            Card card = game.getDeck().pop();
            Stack<Card> playerDeck = player.getDeck();
            playerDeck.push(card);
            player.setDeck(playerDeck);
            
            if (game.getDeck().isEmpty())
                computeWinner(game);
            
            // troca a vez apos compra de carta
            game.setTurnPlayer();
            return 1;
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

//        if (game.getStatus() != GameStatus.CLOSED) {
//            return 3;
//        }

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
            
            // nao é vez do jogador ainda
            if (!game.getPlayerByPlayerId(playerId).getIsMyTurn()) {
                return -3;
            }

            // parametros invalidos - indice do baralho
            if (index <= -1 || index >= this.obtemNumCartas(playerId)) {
                return -4;
            }            

            Card tableCard = game.getTableDeck().peek(); //Helper.stringToCard(this.obtemCartaMesa(playerId));
            Card playedCard = this.getGameByPlayerId(playerId).getPlayerByPlayerId(playerId).getDeck().get(index);
            
            // parametros invalidos - cor inexistente
            if (playedCard.getType() == TypeCard.Cg || playedCard.getType() == TypeCard.C4) {
                if (cardColor < 0 || cardColor > 3)
                    return -4;
            }

            Player player = null;
           
            switch (this.match(playedCard, tableCard, playerId)) {
                // erro
                case -1:
                    return 0;
                // jogada normal
                case 0:
                    this.playCardFull(playerId, index, playedCard);
                    this.getGameByPlayerId(playerId).setTurnPlayer();
                    game.setActiveColor(-1);
                    game.setChanged(true);
                    break;
                // pular e inverter
                case 1:
                    this.playCardFull(playerId, index, playedCard);
                    game.setActiveColor(-1);
                    game.setChanged(true);
                    break;
                case 2:
                    // +2
                    // adversario compra 2 cartas e turno nao é trocado.
                    this.playCardFull(playerId, index, playedCard);

                    player = game.getOpponentByPlayerId(playerId);
                    compra(player.getId());
                    compra(player.getId());
                    game.setChanged(true);
                    game.setActiveColor(-1);
                    break;
                case 3:
                    // coringa
                    this.playCardFull(playerId, index, playedCard);
                    this.getGameByPlayerId(playerId).setTurnPlayer();
                    game.setActiveColor(cardColor);
                    game.setChanged(true);
                    break;
                case 4:
                    // coringa +4
                    this.playCardFull(playerId, index, playedCard);
                    player = game.getOpponentByPlayerId(playerId);
                    compra(player.getId());
                    compra(player.getId());
                    compra(player.getId());
                    compra(player.getId());
                    this.getGameByPlayerId(playerId).setTurnPlayer();
                    game.setActiveColor(cardColor);
                    game.setChanged(true);
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 1;

    }
    
    private void compra(int playerId) {
        try {
            Game game = this.getGameByPlayerId(playerId);
            Player player = game.getPlayerByPlayerId(playerId);
            Card card = game.getDeck().pop();
            Stack<Card> playerDeck = player.getDeck();
            playerDeck.push(card);
            player.setDeck(playerDeck);
            
            if (game.getDeck().isEmpty())
                computeWinner(game);

        } catch (Exception e) {
        }

    }
    
    private void computeWinner(Game game) {
        
        
        int p1Score = Helper.sumScore(game.getPlayers().get(0).getDeck());
        int p2Score = Helper.sumScore(game.getPlayers().get(1).getDeck());
        
        game.getPlayers().get(0).setSum(p1Score);
        game.getPlayers().get(1).setSum(p2Score);
        
    }

}
