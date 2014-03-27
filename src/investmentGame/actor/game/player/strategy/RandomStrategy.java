package investmentGame.actor.game.player.strategy;

import investmentGame.actor.Player;
import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.Game;
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

        try {
            Exchange currentExchange = getPlayer().getGame().getCurrentRoundExchange();
            if (currentExchange.transferAWasExecuted()){
                if (!currentExchange.transferBIsCommitted()){

                    double gainSoFar = currentExchange.getEffectiveSecondaryPlayersBalanceDelta();

                    return Math.round(Math.random()*gainSoFar);

                }else{
                    throw new RuntimeException("transferB has already been committed to current exchange -- it does not make sense to reconsider it as the decision is already fixed");
                }
            }else{
                throw new RuntimeException("TransferA in current exchange was not executed yet -- unable to decide on amount for transferB reliably");
            }
        } catch (Game.GameNotStartedYetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
