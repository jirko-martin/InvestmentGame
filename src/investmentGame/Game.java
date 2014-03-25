package investmentGame;

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

    private String gameId;

    protected StateType gameState;

    protected boolean started = false;

    protected Agent agent;

    protected PlayerInterface playerAtTurnA;

    protected Map<String,PlayerInterface> players = new HashMap<String,PlayerInterface>();

    public String getGameId() {
        return gameId;
    }

    public Game(String gameId, Agent agent){
        this.gameId = gameId;
        this.agent = agent;
    }

    protected abstract void initialize();

    public void start(){
        started = true;
        initialize();
    }

    public void processMessageEvent(ActMessage message){

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
        return players.get(playersName);
    }

    public Transaction transfer(ActMessage message){
        if (!message.getAction().startsWith("info_transfer")){
            //TODO throw Exception
        }
        Pattern contentPattern = Pattern.compile("([^<>]+)\\s+<sends>\\s+(\\d+)\\s+<to>\\s+([^<>]+)");
        Matcher contentMatcher = contentPattern.matcher(message.getContent());

        if (contentMatcher.matches()){
            agent.getLogger().log(Level.INFO,"transfer "+message.getContent());
            String sender = contentMatcher.group(1);
            double credits = Double.parseDouble(contentMatcher.group(2));
            String recipient = contentMatcher.group(3);
            if (message.getAction().equals("info_transfer_A")){
                Transaction transaction = getPlayer(sender).transferA(getPlayer(recipient),credits);
                if (transaction != null){
                    this.playerAtTurnA = transaction.getSender();
                    return transaction;
                }
            }else{
                if (message.getAction().equals("info_transfer_B"))
                    return getPlayer(sender).transferB(getPlayer(recipient),credits);
            }
        }
        return null;
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
