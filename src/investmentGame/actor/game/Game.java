package investmentGame.actor.game;

import madkit.kernel.Agent;
import madkit.message.ActMessage;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public abstract class Game<StateType extends GameState> {

    public static class RoundNotFinalizedYetException extends Exception{

    }

    public static class GameNotStartedYetException extends Exception{

    }

    public static class NoSuchRoundExchangeException extends Exception{

    }

    public static class GameIsOverException extends Exception{

    }

    public static class NoSuchPlayerException extends RuntimeException{

        private String playersName;

        public NoSuchPlayerException(String playersName) {
            super("Player with name '"+playersName+"' is unknown");
            this.playersName = playersName;
        }

        public String getPlayersName() {
            return playersName;
        }
    }

    private String gameId;

    protected StateType gameState;

    protected boolean started = false;

    protected boolean over = false;

    protected Agent agent;

    protected Map<String,PlayerInterface> players = new HashMap<String,PlayerInterface>();

    protected Vector<Exchange> exchanges = new Vector<Exchange>();

    public Game(String gameId, Agent agent){
        this.gameId = gameId;
        this.agent = agent;
    }

    protected abstract void initialize();


    public String getGameId() {
        return gameId;
    }

    public void start(){
        started = true;
        exchanges.add(new Exchange(this));
        initialize();
    }

    public Exchange getCurrentRoundExchange() throws GameNotStartedYetException {
        if (isStarted()){
            return exchanges.lastElement();
        }else{
            throw new GameNotStartedYetException();
        }
    }

    public Exchange getLastRoundExchange() throws GameNotStartedYetException, NoSuchRoundExchangeException {
        if (isStarted()){
            if (exchanges.size()>=2){

                return exchanges.elementAt(exchanges.size()-2);

            }else{
                throw new NoSuchRoundExchangeException();
            }
        }else{
            throw new GameNotStartedYetException();
        }
    }

    protected void nextRound() throws RoundNotFinalizedYetException, GameNotStartedYetException, GameIsOverException {

        if (getCurrentRoundExchange().isCompleteAndExecuted()){

            if (!isOver()){

                exchanges.add(new Exchange(this));

            }else{
                throw new GameIsOverException();
            }

        }else{
            throw new RoundNotFinalizedYetException();
        }

    }

    public void endGame() throws GameNotStartedYetException {
        if (isStarted()){
            if (!getCurrentRoundExchange().isCompleteAndExecuted()){
                exchanges.removeElementAt(exchanges.size()-1);
            }
            over = true;
        }else{
            throw new GameNotStartedYetException();
        }
    }

    public boolean isOver(){
        return over;
    }

    public void commit(Exchange.TransferA transfer) throws GameNotStartedYetException, Exchange.TransferAlreadyCommittedException, Exchange.ConstraintViolationException {
        getCurrentRoundExchange().commit(transfer);
    }

    public void simulateCommit(Exchange.TransferA transfer) throws GameNotStartedYetException, Exchange.TransferAlreadyCommittedException, Exchange.ConstraintViolationException {
        getCurrentRoundExchange().commit(transfer,true);
    }

    public void commit(Exchange.TransferB transfer) throws GameNotStartedYetException, Exchange.TransferAlreadyCommittedException, Exchange.ConstraintViolationException, Exchange.PrimaryTransferNotCommittedYetException {
        getCurrentRoundExchange().commit(transfer);
    }

    public void simulateCommit(Exchange.TransferB transfer) throws GameNotStartedYetException, Exchange.TransferAlreadyCommittedException, Exchange.ConstraintViolationException, Exchange.PrimaryTransferNotCommittedYetException {
        getCurrentRoundExchange().commit(transfer,true);
    }

    public void confirmExecute(Exchange.TransferA transfer) throws GameNotStartedYetException, Exchange.TransferNotCommittedException, Exchange.ConstraintViolationException {
        getCurrentRoundExchange().confirmExecuteTransfer(transfer);
    }

    public void confirmExecute(Exchange.TransferB transfer) throws GameNotStartedYetException, Exchange.TransferNotCommittedException, Exchange.ConstraintViolationException, RoundNotFinalizedYetException, GameIsOverException {
        getCurrentRoundExchange().confirmExecuteTransfer(transfer);
        nextRound();
    }

    public void processMessageEvent(ActMessage message) {

        StateType newState = (StateType)gameState.processMessageEvent(message);

        setGameState(newState);
    }

    public void setGameState(StateType newState){
        if (!newState.equals(gameState)){
            if (gameState!=null)
                gameState.onExitState();
            gameState = newState;
            agent.getLogger().log(Level.INFO,"state transition to state "+gameState.getStateName());
            gameState.onEnterState();
        }
    }

    public PlayerInterface getPlayer(String playersName){
        if (players.containsKey(playersName)){
            return players.get(playersName);
        }else{
            throw new NoSuchPlayerException(playersName);
        }
    }

    public PlayerInterface getPlayer(PlayerInterface player){
        return getPlayer(player.getPlayersName());
    }

    public void addPlayer(PlayerInterface player){

        if (!players.containsKey(player.getPlayersName())){

            players.put(player.getPlayersName(),player);

        }

    }

    public boolean isStarted(){
        return started;
    }

    public Collection<PlayerInterface> getPlayers(){
        return players.values();
    }

    public List<PlayerInterface> getPlayersRanked(){
        Iterator<PlayerInterface> playersIterator = getPlayers().iterator();

        class PlayerComparable implements Comparable<PlayerComparable>{

            private PlayerInterface player;

            public PlayerComparable(PlayerInterface player){
                this.player = player;
            }

            @Override
            public int compareTo(PlayerComparable o) {
                return (int)(o.player.getCreditBalance()-this.player.getCreditBalance());
            }

            PlayerInterface getPlayer() {
                return player;
            }
        }

        ArrayList<PlayerComparable> playersComp = new ArrayList<PlayerComparable>();

        while (playersIterator.hasNext()){
            playersComp.add(new PlayerComparable(playersIterator.next()));
        }

        Collections.sort(playersComp);

        ArrayList<PlayerInterface> result = new ArrayList<PlayerInterface>();

        Iterator<PlayerComparable> playerComparableIterator = playersComp.iterator();

        while (playerComparableIterator.hasNext()){
            result.add(playerComparableIterator.next().getPlayer());
        }

        return result;

    }

}
