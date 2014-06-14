package investmentGame.actor.game.coordinator;

import investmentGame.actor.game.*;
import investmentGame.actor.Coordinator;
import madkit.message.ActMessage;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 17:50
 * To change this template use File | Settings | File Templates.
 */
public class CoordinatorsGame extends Game {

    private Coordinator coordinator;

    private int numberOfPlayersTotal;

    private int numberOfRoundsTotal;

    private int numberOfRoundsPassed = 0;

    protected PlayerInterface primaryPlayer;

    public final Map<String, GameState<CoordinatorsGame>> gameStates = new HashMap<String, GameState<CoordinatorsGame>>();

    public CoordinatorsGame(Coordinator coordinator,String gameId, int numberOfPlayersTotal,int numberOfRoundsTotal) {
        super(gameId,coordinator);
        this.coordinator = coordinator;
        initializeGameStates();
        this.numberOfPlayersTotal = numberOfPlayersTotal;
        this.numberOfRoundsTotal = numberOfRoundsTotal;
    }

    private void initializeGameStates() {

        gameStates.put(

                "initial",

                new GameState<CoordinatorsGame>("initial",this){

                    private Set<String> playersToBecomeReady = new HashSet<String>();

                    @Override
                    public void onEnterState() {
                        if (game.isStarted()){

                            Iterator<PlayerInterface> players = game.getPlayers().iterator();

                            String message = "";
                            boolean first = true;

                            while (players.hasNext()){
                                PlayerInterface player = players.next();
                                if (!first)
                                    message += "##";
                                message += player.getStringDescription();
                                playersToBecomeReady.add(player.getPlayersName());
                                first = false;
                            }

                            CoordinatorsGame.this.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player",new ActMessage("game_starts",message),"coordinator");

                        }
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("ready_to_play")){
                            if (playersToBecomeReady.contains(message.getContent())){
                                playersToBecomeReady.remove(message.getContent());
                            }

                            if (playersToBecomeReady.isEmpty()){
                                game.getCoordinator().getLogger().log(Level.INFO,"all players are ready...");
                                return gameStates.get("next_round");
                            }
                        }
                        return this;
                    }
                }
        );

        gameStates.put(

                "next_round",

                new GameState<CoordinatorsGame>("next_round",this){
                    @Override
                    public void onEnterState() {

                        if (game.hasNextRound()){

                            PlayerInterface playerAtTurnA = game.selectPlayer();

                            game.setGameState(gameStates.get("waiting_1"));

                            game.incNumberOfRoundsPassed();

                            game.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player_"+playerAtTurnA.getPlayersName(),new ActMessage("your_turn_A"),"coordinator");

                        }else{

                            game.setGameState(gameStates.get("game_over"));

                        }

                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        return super.processMessageEvent(message);
                    }
                }

        );

        gameStates.put(

                "waiting_1",

                new GameState<CoordinatorsGame>("waiting_1",this){
                    @Override
                    public void onEnterState() {
                        super.onEnterState();
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {

                        if (message.getAction().equals("decide_on_transfer_A")){

                            game.getCoordinator().getLogger().log(Level.INFO,"coordinator processes event decide_on_transfer_A : "+message.getContent());

                            Transfer transfer = TransferFactory.transferFromMessage(message,game);

                            if (transfer instanceof Exchange.TransferA){

                                Exchange.TransferA transferA = (Exchange.TransferA)transfer;

                                try {

                                    game.commit(transferA);

                                    game.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player",TransferFactory.getInfoMessageForTransfer(transferA),"coordinator");

                                    return gameStates.get("join_transfer_A");

                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferAlreadyCommittedException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                        return this;
                    }
                }

        );

        gameStates.put(

                "waiting_2",

                new GameState<CoordinatorsGame>("waiting_2",this){
                    @Override
                    public void onEnterState() {

                        try {
                            PlayerInterface playerAtTurnB = getCurrentRoundExchange().getTransferA().getRecipient();

                            if (getCurrentRoundExchange().transferAWasExecuted()){

                                game.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player_"+playerAtTurnB.getPlayersName(),new ActMessage("your_turn_B"),"coordinator");

                            }else{
                                game.agent.getLogger().log(Level.SEVERE, "onEnterState waiting_2 : transferA was not executed");
                            }

                        } catch (Exchange.TransferNotCommittedException e) {
                            e.printStackTrace();
                        } catch (GameNotStartedYetException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("decide_on_transfer_B")){

                            game.getCoordinator().getLogger().log(Level.INFO,"coordinator processes event decide_on_transfer_B : "+message.getContent());

                            Transfer transfer = TransferFactory.transferFromMessage(message, game);

                            if (transfer instanceof Exchange.TransferB){

                                Exchange.TransferB transferB = (Exchange.TransferB)transfer;

                                try {

                                    game.commit(transferB);

                                    game.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player",TransferFactory.getInfoMessageForTransfer(transferB),"coordinator");

                                    return gameStates.get("join_transfer_B");

                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferAlreadyCommittedException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                } catch (Exchange.PrimaryTransferNotCommittedYetException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                        return this;
                    }
                }

        );

        gameStates.put(

                "join_transfer_B",

                new GameState<CoordinatorsGame>("join_transfer_B",this){

                    private Set<String> playersToBecomeReady = new HashSet<String>();

                    @Override
                    public void onEnterState() {
                        Iterator<PlayerInterface> players = game.getPlayers().iterator();


                        while (players.hasNext()){
                            PlayerInterface player = players.next();
                            playersToBecomeReady.add(player.getPlayersName());
                        }
                    }

                    @Override
                    public void onExitState() {
                        try {
                            game.confirmExecute(game.getCurrentRoundExchange().getTransferB());
                        } catch (GameNotStartedYetException e) {
                            e.printStackTrace();
                        } catch (Exchange.ConstraintViolationException e) {
                            e.printStackTrace();
                        } catch (Exchange.TransferNotCommittedException e) {
                            e.printStackTrace();
                        } catch (GameIsOverException e) {
                            e.printStackTrace();
                        } catch (RoundNotFinalizedYetException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("processed_transfer_B")){
                            if (playersToBecomeReady.contains(message.getContent())){
                                playersToBecomeReady.remove(message.getContent());
                            }
                            if (playersToBecomeReady.isEmpty()){
                                return gameStates.get("next_round");
                            }
                        }
                        return this;
                    }
                }

        );

        gameStates.put(

                "join_transfer_A",

                new GameState<CoordinatorsGame>("join_transfer_A",this){

                    private Set<String> playersToBecomeReady = new HashSet<String>();

                    @Override
                    public void onEnterState() {
                        Iterator<PlayerInterface> players = game.getPlayers().iterator();


                        while (players.hasNext()){
                            PlayerInterface player = players.next();
                            playersToBecomeReady.add(player.getPlayersName());
                        }
                    }

                    @Override
                    public void onExitState() {
                        try {
                            game.confirmExecute(game.getCurrentRoundExchange().getTransferA());
                        } catch (GameNotStartedYetException e) {
                            e.printStackTrace();
                        } catch (Exchange.ConstraintViolationException e) {
                            e.printStackTrace();
                        } catch (Exchange.TransferNotCommittedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("processed_transfer_A")){
                            if (playersToBecomeReady.contains(message.getContent())){
                                playersToBecomeReady.remove(message.getContent());
                            }
                            if (playersToBecomeReady.isEmpty()){
                                return gameStates.get("waiting_2");
                            }
                        }
                        return this;
                    }
                }

        );

        gameStates.put(

                "game_over",

                new GameState<CoordinatorsGame>("game_over",this){
                    @Override
                    public void onEnterState() {

                        try {
                            game.endGame();
                        } catch (GameNotStartedYetException e) {
                            e.printStackTrace();
                        }

                        List<PlayerInterface> playersRanked = game.getPlayersRanked();

                        game.getCoordinator().getLogger().log(Level.INFO,"\n\nAND THE WINNER IS ... "+playersRanked.get(0).getPlayersName()+" ("+playersRanked.get(0).getCreditBalance()+" credits)");
                        game.getCoordinator().getLogger().log(Level.INFO,"THE SECOND PRICE  ... "+playersRanked.get(1).getPlayersName()+" ("+playersRanked.get(1).getCreditBalance()+" credits)");
                        game.getCoordinator().broadcastMessageWithRole("investment_game", game.getGameId(), "player", new ActMessage("game_over"), "coordinator");

                        KMeansPlayerGroups kMeans = new KMeansPlayerGroups(game.getPlayers(),2);
                        FinalDisplay finalDisplay = new FinalDisplay(kMeans);
                        finalDisplay.show();



                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message){
                        return super.processMessageEvent(message);
                    }
                }

        );

    }

    @Override
    protected void initialize() {
        setGameState(gameStates.get("initial"));
    }

    public int getNumberOfPlayersTotal() {
        return numberOfPlayersTotal;
    }

    public boolean allPlayersHaveJoined(){
        return players.size()>=numberOfPlayersTotal;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public PlayerInterface selectPlayer(){

        try {
            return getLastRoundExchange().getTransferB().getSender();
        } catch (Exchange.TransferNotCommittedException e) {
            return primaryPlayer;
        } catch (GameNotStartedYetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchRoundExchangeException e) {
            return primaryPlayer;
        }

    }

    public int getNumberOfRoundsTotal() {
        return numberOfRoundsTotal;
    }

    public void incNumberOfRoundsPassed(){
        numberOfRoundsPassed++;
        agent.getLogger().log(Level.INFO,"GAME "+getGameId()+" : "+numberOfRoundsPassed+" / "+numberOfRoundsTotal+" rounds passed");
    }

    public boolean hasNextRound(){
        return numberOfRoundsPassed<numberOfRoundsTotal;
    }

    public void addPlayer(PlayerInterface player,boolean asPrimaryPlayer) {
        super.addPlayer(player);
        if (asPrimaryPlayer)
            primaryPlayer = player;
    }
}
