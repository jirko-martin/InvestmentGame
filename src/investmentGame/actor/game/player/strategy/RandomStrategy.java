package investmentGame.actor.game.player.strategy;

import investmentGame.actor.Player;
import investmentGame.actor.game.PlayerInterface;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 23/03/14
 * Time: 20:48
 * To change this template use File | Settings | File Templates.
 */
public class RandomStrategy extends Strategy {

    public RandomStrategy(Player player){
        super(player);
    }

    @Override
    public PlayerInterface selectOpponentForTransferA() {
        ArrayList<PlayerInterface> opponents = new ArrayList<PlayerInterface>(getPlayer().getOpponents());

        return opponents.get((int)Math.round(Math.random()*(opponents.size()-1)));
    }

    @Override
    public double chooseCreditAmountForTransferA(PlayerInterface opponent) {
        double creditBalance = getPlayer().getCreditBalance();

        return Math.round(Math.random()*creditBalance);
    }

    @Override
    public double chooseCreditAmountForTransferB(PlayerInterface opponent) {
        double creditBalance = getPlayer().getCreditBalance();

        return Math.round(Math.random()*creditBalance);
    }
}
