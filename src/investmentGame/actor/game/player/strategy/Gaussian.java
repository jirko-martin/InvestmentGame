package investmentGame.actor.game.player.strategy;

import investmentGame.actor.Player;
import investmentGame.actor.game.PlayerInterface;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 27/03/14
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */
public class Gaussian extends Strategy{

    public Gaussian(Player player) {
        super(player);
    }

    @Override
    public PlayerInterface selectOpponentForTransferA() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double chooseCreditAmountForTransferA(PlayerInterface opponent) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double chooseCreditAmountForTransferB(PlayerInterface opponent) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JDialog getConfigurationDialog(JComponent parentComponent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
