package investmentGame;

import investmentGame.actor.game.player.strategy.*;

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

        public static long showTransferInGUIForMSec = 2000;

        public static long delayAcknowledgeInfoTransferMSecTurnA = 1000;

        public static long delayAcknowledgeInfoTransferMSecTurnB = 1500;
    }

    public static final Class[] chooseAmountStrategies = {

            GaussianChooseAmountStrategy.class,
            RandomChooseAmountStrategy.class,
            TitForNWeightedTatsChooseAmountStrategy.class

    };

    public static final Class[] selectOpponentStrategies = {

            FairSelectOpponentStrategy.class,
            ExcludePlayerSelectOpponentStrategy.class

    };

}
