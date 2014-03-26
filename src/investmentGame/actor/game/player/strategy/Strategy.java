package investmentGame.actor.game.player.strategy;

import investmentGame.actor.Player;
import investmentGame.actor.game.PlayerInterface;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 16:54
 * To change this template use File | Settings | File Templates.
 */
public abstract class Strategy {

    private final Player player;

    public Strategy(Player player){
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public abstract PlayerInterface selectOpponentForTransferA();

    public abstract double chooseCreditAmountForTransferA(PlayerInterface opponent);

    public abstract double chooseCreditAmountForTransferB(PlayerInterface opponent);
}
