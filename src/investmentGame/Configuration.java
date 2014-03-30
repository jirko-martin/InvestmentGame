package investmentGame;

import investmentGame.actor.game.player.strategy.GaussianStrategy;
import investmentGame.actor.game.player.strategy.RandomStrategy;
import investmentGame.actor.game.player.strategy.Strategy;
import investmentGame.actor.game.player.strategy.TitForNWeightedTatsStrategy;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 25/03/14
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class Configuration {

    public static final String picturePath = "/Users/martin/IdeaProjects/InvestmentGame/src/investmentGame/pictures";

    public static final int transferAMultiplier = 3;

    public static final int initialCreditBalance = 10;

    public static class Colors {

        public static final Color gamePanelBackgroundColor = new Color(66, 219, 222);

        public static final Color transferAArrowColor = Color.GREEN;

        public static final Color transferBArrowColor = Color.ORANGE;

    }

    public static class Timings{

        public static final long showTransferInGUIForMSec = 2000;

        public static final long delayAcknowledgeInfoTransferMSec = 2500;

    }

    public static final Class[] strategies = {

            GaussianStrategy.class,
            RandomStrategy.class,
            TitForNWeightedTatsStrategy.class

    };

}
