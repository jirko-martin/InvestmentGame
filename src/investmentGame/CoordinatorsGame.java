package investmentGame;

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
public class CoordinatorsGame extends Game{

    private Coordinator coordinator;

    private int numberOfPlayersTotal;

    private int numberOfRoundsTotal;

    private int numberOfRoundsPassed = 0;

    protected PlayerInterface playerAtTurnA;
    protected PlayerInterface playerAtTurnB;

    protected PlayerInterface primaryPlayer;

    public final Map<String,GameState<CoordinatorsGame>> gameStates = new HashMap<String, GameState<CoordinatorsGame>>();

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
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
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

                            playerAtTurnA = game.selectPlayer();

                            game.setGameState(gameStates.get("waiting_1"));

                            game.incNumberOfRoundsPassed();

                            game.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player_"+playerAtTurnA.getPlayersName(),new ActMessage("your_turn_A"),"coordinator");

                        }else{

                            game.setGameState(gameStates.get("game_over"));

                        }

                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        return super.processMessageEvent(message);    //To change body of overridden methods use File | Settings | File Templates.
                    }
                }

        );

        gameStates.put(

                "waiting_1",

                new GameState<CoordinatorsGame>("waiting_1",this){
                    @Override
                    public void onEnterState() {
                        super.onEnterState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("decide_on_transfer_A")){
                            game.getCoordinator().getLogger().log(Level.INFO,"coordinator processes event decide_on_transfer_A : "+message.getContent());
                            Pattern transferDescriptionPattern = Pattern.compile("<player>\\s+([^<>\\s]+)\\s+<decides_to_transfer>\\s+(\\d+)\\s+<credits>\\s+<to>\\s+<player>\\s+([^<>\\s]+)");
                            Matcher m = transferDescriptionPattern.matcher(message.getContent());
                            if (m.matches()){
                                String sendersName = m.group(1);
                                double credits = Double.parseDouble(m.group(2));
                                String recipientsName = m.group(3);
                                if (sendersName.equals(playerAtTurnA.getPlayersName())){
                                    ActMessage messageToEmit = new ActMessage("info_transfer_A",sendersName+" <sends> "+((int)credits)+" <to> "+recipientsName);
                                    game.transfer(messageToEmit);
                                    game.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player",messageToEmit,"coordinator");
                            //        game.getCoordinator().pauseAWhile(1500);
                                    playerAtTurnB = game.getPlayer(recipientsName);
                                    return gameStates.get("join_transfer_A");
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

                        game.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player_"+playerAtTurnB.getPlayersName(),new ActMessage("your_turn_B"),"coordinator");
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("decide_on_transfer_B")){
                            Pattern transferDescriptionPattern = Pattern.compile("<player>\\s+([^<>\\s]+)\\s+<decides_to_transfer>\\s+(\\d+)\\s+<credits>\\s+<to>\\s+<player>\\s+([^<>\\s]+)");
                            Matcher m = transferDescriptionPattern.matcher(message.getContent());
                            if (m.matches()){
                                String sendersName = m.group(1);
                                double credits = Double.parseDouble(m.group(2));
                                String recipientsName = m.group(3);
                                if (sendersName.equals(playerAtTurnB.getPlayersName())
                                        && recipientsName.equals(playerAtTurnA.getPlayersName())){
                                    ActMessage messageToEmit = new ActMessage("info_transfer_B",sendersName+" <sends> "+((int)credits)+" <to> "+recipientsName);
                                    game.transfer(messageToEmit);
                                    game.getCoordinator().broadcastMessageWithRole("investment_game",game.getGameId(),"player",messageToEmit,"coordinator");
                          //          game.getCoordinator().pauseAWhile(1500);
                                    return gameStates.get("join_transfer_B");
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
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
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
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
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

                        List<PlayerInterface> playersRanked = game.getPlayersRanked();

                        game.getCoordinator().getLogger().log(Level.INFO,"\n\nAND THE WINNER IS ... "+playersRanked.get(0).getPlayersName()+" ("+playersRanked.get(0).getCreditBalance()+" credits)");
                        game.getCoordinator().getLogger().log(Level.INFO,"THE SECOND PRICE  ... "+playersRanked.get(1).getPlayersName()+" ("+playersRanked.get(1).getCreditBalance()+" credits)");
                        game.getCoordinator().broadcastMessageWithRole("investment_game", game.getGameId(), "player", new ActMessage("game_over"), "coordinator");
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        return super.processMessageEvent(message);    //To change body of overridden methods use File | Settings | File Templates.
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

        if (playerAtTurnB!=null)
            return playerAtTurnB;
        else
            return primaryPlayer;

//        ArrayList<PlayerInterface> playersList = new ArrayList<PlayerInterface>(players.values());
//        return playersList.get((int)Math.round(Math.random()*(playersList.size()-1)));
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
