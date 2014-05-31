package investmentGame.actor.game.player.strategy;

import investmentGame.actor.Player;
import investmentGame.actor.game.PlayerInterface;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 31/05/14
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public abstract class SelectOpponentStrategy {

    private Player player;

    public abstract String getStrategyName();

    public String toString(){
        return getStrategyName();
    }

    public abstract String getFullDescription();

    public Player getPlayer(){
        return player;
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public abstract PlayerInterface selectOpponentForTransferA();


    public abstract boolean isConfigurable();

    public abstract JPanel getConfigurationPanel();

}
