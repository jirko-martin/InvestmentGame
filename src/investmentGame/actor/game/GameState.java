package investmentGame.actor.game;

import madkit.message.ActMessage;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 23/03/14
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class GameState<GameType extends Game> {

    private String stateName;

    protected GameType game;

    public GameState(String stateName, GameType game){
        this.stateName = stateName;
        this.game = game;
    }

    public void onEnterState(){

    }

    public void onExitState(){

    }

    public GameState processMessageEvent(ActMessage message){
        return this;
    }

    public boolean equals(GameState other){
        return (other instanceof GameState) && other.stateName.equals(this.stateName);
    }

    public String getStateName() {
        return stateName;
    }
}
