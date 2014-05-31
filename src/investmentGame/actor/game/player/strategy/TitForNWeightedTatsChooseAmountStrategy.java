package investmentGame.actor.game.player.strategy;

import investmentGame.Configuration;
import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.Game;
import investmentGame.actor.game.PlayerInterface;
import investmentGame.actor.game.Transfer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 30/03/14
 * Time: 00:26
 * To change this template use File | Settings | File Templates.
 */
public class TitForNWeightedTatsChooseAmountStrategy extends ChooseAmountStrategy {


    public class MotivationalStrategyComponent{

        public double egoismCoefficient = 0.55;
        public double egoismStandardDeviation = 0.1;

        public double altruismCoefficient = 0.3;
        public double altruismStandardDeviation = 0.1;

        public double aggressionCoefficient = 0.1;
        public double aggressionStandardDeviation = 0.1;

        public double competitivenessCoefficient = 0.75;
        public double competitivenessStandardDeviation = 0.1;
        
        public double optimismCoefficient = 0.35;
        public double optimismStandardDeviation = 0.15;

        public double getEgoismStandardDeviation() {
            return egoismStandardDeviation;
        }

        public void setEgoismStandardDeviation(double egoismStandardDeviation) {
            this.egoismStandardDeviation = egoismStandardDeviation;
        }

        public double getAltruismStandardDeviation() {
            return altruismStandardDeviation;
        }

        public void setAltruismStandardDeviation(double altruismStandardDeviation) {
            this.altruismStandardDeviation = altruismStandardDeviation;
        }

        public double getAggressionStandardDeviation() {
            return aggressionStandardDeviation;
        }

        public void setAggressionStandardDeviation(double aggressionStandardDeviation) {
            this.aggressionStandardDeviation = aggressionStandardDeviation;
        }

        public double getCompetitivenessStandardDeviation() {
            return competitivenessStandardDeviation;
        }

        public void setCompetitivenessStandardDeviation(double competitivenessStandardDeviation) {
            this.competitivenessStandardDeviation = competitivenessStandardDeviation;
        }

        public double getEgoismCoefficient() {
            return egoismCoefficient;
        }

        public void setEgoismCoefficient(double egoismCoefficient) {
            this.egoismCoefficient = egoismCoefficient;
        }

        public double getAltruismCoefficient() {
            return altruismCoefficient;
        }

        public void setAltruismCoefficient(double altruismCoefficient) {
            this.altruismCoefficient = altruismCoefficient;
        }

        public double getAggressionCoefficient() {
            return aggressionCoefficient;
        }

        public void setAggressionCoefficient(double aggressionCoefficient) {
            this.aggressionCoefficient = aggressionCoefficient;
        }

        public double getCompetitivenessCoefficient() {
            return competitivenessCoefficient;
        }

        public void setCompetitivenessCoefficient(double competitivenessCoefficient) {
            this.competitivenessCoefficient = competitivenessCoefficient;
        }

        public double getOptimismCoefficient() {
            return optimismCoefficient;
        }

        public void setOptimismCoefficient(double optimismCoefficient) {
            this.optimismCoefficient = optimismCoefficient;
        }

        public double getOptimismStandardDeviation() {
            return optimismStandardDeviation;
        }

        public void setOptimismStandardDeviation(double optimismStandardDeviation) {
            this.optimismStandardDeviation = optimismStandardDeviation;
        }

        public double calcEgoismComponentA(double expectedPercentageTransferBOpponent, PlayerInterface opponent){

            double currentBalance = getPlayer().getCreditBalance();

            try {
                Transfer transferA = new Exchange.TransferA(getPlayer(),opponent,(getBaseMeanA()/100.0)*currentBalance, Configuration.transferAMultiplier);
                Transfer transferB = new Exchange.TransferB(opponent,getPlayer(),expectedPercentageTransferBOpponent*transferA.getRecipientsBalanceDelta());

                double expectedBalanceDelta = transferA.getSendersBalanceDelta() + transferB.getRecipientsBalanceDelta();

                double expectedBalance = (currentBalance+expectedBalanceDelta);

                return expectedBalance>0 ? (expectedBalanceDelta / expectedBalance) : (currentBalance > 0 ? -1 : 0);

            } catch (Transfer.InvalidTransferException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }
        
        public double calcEgoismComponentB(){
            try {
                
                double gainMeBMin = getPlayer().getGame().getCurrentRoundExchange().getTransferA().getRecipientsBalanceDelta();
                double balanceMeBMin = getPlayer().getCreditBalance();
                
                return (gainMeBMin / balanceMeBMin);
                
            } catch (Exchange.TransferNotCommittedException e) {
                e.printStackTrace();  
                throw new RuntimeException(e);
            } catch (Game.GameNotStartedYetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);  
            }
        }

        public double calcAltruismComponentA(double expectedPercentageTransferBOpponent, PlayerInterface opponent){

            double currentBalanceOpponent = opponent.getCreditBalance();

            try {
                Transfer transferA = new Exchange.TransferA(getPlayer(),opponent,(getBaseMeanA()/100.0)*getPlayer().getCreditBalance(), Configuration.transferAMultiplier);
                Transfer transferB = new Exchange.TransferB(opponent,getPlayer(),expectedPercentageTransferBOpponent*transferA.getRecipientsBalanceDelta());

                double expectedBalanceDeltaOpponent = transferA.getRecipientsBalanceDelta() + transferB.getSendersBalanceDelta();

                double expectedBalanceOpponent = (currentBalanceOpponent+expectedBalanceDeltaOpponent);

                return expectedBalanceOpponent > 0 ? (expectedBalanceDeltaOpponent / expectedBalanceOpponent) : 0;

            } catch (Transfer.InvalidTransferException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }

        public double calcAltruismComponentB(){
            try {

                Exchange.TransferB maxBTransfer = new Exchange.TransferB(getPlayer(),getPlayer().getGame().getCurrentRoundExchange().getTransferA().getSender(),getPlayer().getGame().getCurrentRoundExchange().getTransferA().getRecipientsBalanceDelta());
                
                
                double gainOpponentBMax = getPlayer().getGame().getCurrentRoundExchange().getTransferA().getSendersBalanceDelta() + maxBTransfer.getRecipientsBalanceDelta();
                double balanceOpponentBMax = getPlayer().getGame().getCurrentRoundExchange().getTransferA().getSender().getCreditBalance() + maxBTransfer.getRecipientsBalanceDelta();

                return (gainOpponentBMax / balanceOpponentBMax);

            } catch (Exchange.TransferNotCommittedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (Game.GameNotStartedYetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (Transfer.InvalidTransferException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        public double calcAggressionComponentA(double expectedPercentageTransferBOpponent, PlayerInterface opponent){

            double currentBalanceOpponent = opponent.getCreditBalance();

            try {
                Transfer transferA = new Exchange.TransferA(getPlayer(),opponent,(getBaseMeanA()/100.0)*getPlayer().getCreditBalance(), Configuration.transferAMultiplier);
                Transfer transferB = new Exchange.TransferB(opponent,getPlayer(),expectedPercentageTransferBOpponent*transferA.getRecipientsBalanceDelta());

                double expectedBalanceDeltaOpponent = transferA.getRecipientsBalanceDelta() + transferB.getSendersBalanceDelta();

                double expectedBalanceOpponent = (currentBalanceOpponent+expectedBalanceDeltaOpponent);

                return expectedBalanceOpponent > 0 ? -(expectedBalanceDeltaOpponent / expectedBalanceOpponent) : -1;

            } catch (Transfer.InvalidTransferException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }

        public double calcAggressionComponentB(){
            try {

                double lossOpponentBMin = -1.0 * getPlayer().getGame().getCurrentRoundExchange().getTransferA().getSendersBalanceDelta();
                double balanceOpponentBMin = getPlayer().getGame().getCurrentRoundExchange().getTransferA().getSender().getCreditBalance();

                return (lossOpponentBMin / balanceOpponentBMin);

            } catch (Exchange.TransferNotCommittedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (Game.GameNotStartedYetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        public double calcCompetitivenessComponentA(double expectedPercentageTransferBOpponent, PlayerInterface opponent){
            double totalCapitalInGame = getPlayer().getGame().getTotalCapitalInGame();


            try {

                double fractionMeNow = getPlayer().getCreditBalance() / totalCapitalInGame;
                double fractionOpponentNow = opponent.getCreditBalance() / totalCapitalInGame;

                double distanceStatus = fractionMeNow - fractionOpponentNow;

                Transfer transferA = new Exchange.TransferA(getPlayer(),opponent,(getBaseMeanA()/100.0)*getPlayer().getCreditBalance(), Configuration.transferAMultiplier);
                Transfer transferB = new Exchange.TransferB(opponent,getPlayer(),expectedPercentageTransferBOpponent*transferA.getRecipientsBalanceDelta());

                double expectedBalanceDelta = transferA.getSendersBalanceDelta() + transferB.getRecipientsBalanceDelta();

                double expectedBalance = (getPlayer().getCreditBalance()+expectedBalanceDelta);

                double expectedBalanceDeltaOpponent = transferA.getRecipientsBalanceDelta() + transferB.getSendersBalanceDelta();

                double expectedBalanceOpponent = (opponent.getCreditBalance()+expectedBalanceDeltaOpponent);

                double totalCapitalInGamePost = totalCapitalInGame + transferA.getSendersBalanceDelta() + transferA.getRecipientsBalanceDelta();

                double fractionMePost = expectedBalance / totalCapitalInGamePost;
                double fractionOpponentPost = expectedBalanceOpponent / totalCapitalInGamePost;

                double distanceStatusPost = fractionMePost - fractionOpponentPost;

                return distanceStatusPost - distanceStatus;


            } catch (Transfer.InvalidTransferException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


        }
        
        public double calcCompetitivenessComponentB(){
            double totalCapitalInGame = getPlayer().getGame().getTotalCapitalInGame();


            try {
                PlayerInterface opponentA = getPlayer().getGame().getCurrentRoundExchange().getTransferA().getSender();
                
                Exchange.TransferB fairBTransfer = new Exchange.TransferB(getPlayer(),opponentA,getPlayer().getGame().getCurrentRoundExchange().getTransferA().getRecipientsBalanceDelta()*(2.0/3.0));
            
                double fractionOfTotalCapitalMeFairBTransfer = (getPlayer().getCreditBalance()+fairBTransfer.getSendersBalanceDelta()) / totalCapitalInGame;
                
                double fractionOfTotalCapitalOpponentFairBTransfer = (opponentA.getCreditBalance()+fairBTransfer.getRecipientsBalanceDelta()) / totalCapitalInGame;
                
                return (fractionOfTotalCapitalMeFairBTransfer - fractionOfTotalCapitalOpponentFairBTransfer);
            
            } catch (Transfer.InvalidTransferException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exchange.TransferNotCommittedException e) {
                e.printStackTrace();  
                throw new RuntimeException(e);
            } catch (Game.GameNotStartedYetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


        }
        
        public double calculateMotivationalComplexA(PlayerInterface opponent, double expectedReTransferFractionBOpponent){
            
            double egoism = getRandom().nextGaussian() * getEgoismStandardDeviation() + getEgoismCoefficient();
            double altruism = getRandom().nextGaussian() * getAltruismStandardDeviation() + getAltruismCoefficient();
            double aggressiveness = getRandom().nextGaussian() * getAggressionStandardDeviation() + getAggressionCoefficient();
            double competitiveness = getRandom().nextGaussian() * getCompetitivenessStandardDeviation() + getCompetitivenessCoefficient();
            
            double coefficientSum = egoism + altruism + aggressiveness + competitiveness;
            
            return (
                     egoism          * calcEgoismComponentA(expectedReTransferFractionBOpponent,opponent)
                   + altruism        * calcAltruismComponentA(expectedReTransferFractionBOpponent,opponent)
                   + aggressiveness  * calcAggressionComponentA(expectedReTransferFractionBOpponent, opponent)
                   + competitiveness * calcCompetitivenessComponentA(expectedReTransferFractionBOpponent, opponent)
            ) / coefficientSum;
        }

        public double calculateMotivationalComplexB(){

            double egoism = getRandom().nextGaussian() * getEgoismStandardDeviation() + getEgoismCoefficient();
            double altruism = getRandom().nextGaussian() * getAltruismStandardDeviation() + getAltruismCoefficient();
            double aggressiveness = getRandom().nextGaussian() * getAggressionStandardDeviation() + getAggressionCoefficient();
            double competitiveness = getRandom().nextGaussian() * getCompetitivenessStandardDeviation() + getCompetitivenessCoefficient();

            double coefficientSum = egoism + altruism + aggressiveness + competitiveness;

            return (
                     -1.0 * egoism          * calcEgoismComponentB()
                    + 1.0 * altruism        * calcAltruismComponentB()
                    - 1.0 * aggressiveness  * calcAggressionComponentB()
                    + 1.0 * competitiveness * calcCompetitivenessComponentB()
            ) / coefficientSum;
        }
        
    }


    protected abstract class ExchangeExtractor{

        public abstract double extractValue0_1(Exchange exchange);

    }


    private int baseMeanA = 20;

    private int baseMeanB = 65;

    private int standardDeviationA = 15;

    private int standardDeviationB = 15;

    private double weightingCoefficient = 0.7;

    private double c1 = 0.5;

    private double c2 = 0.5;

    private double c3 = 0.5;

    private double c4 = 0.5;


    private boolean titForTatIncludeOtherPlayersExperience = false;

    private MotivationalStrategyComponent motivationalStrategyComponent = new MotivationalStrategyComponent();


    private Random random = new Random();


    @Override
    public String getStrategyName() {
        return "Tit For N Weighted Tats";
    }

    @Override
    public String getFullDescription() {
        return toString();
    }

    @Override
    public double chooseCreditAmountForTransferA(PlayerInterface opponent) {
        double creditBalance = getPlayer().getCreditBalance();

        double TFT2 = getEvaluationOfOpponent(opponent, new ExchangeExtractor() {
            @Override
            public double extractValue0_1(Exchange exchange) {

                try {

                    if (exchange.getTransferA().getCreditsTransferred()==0){
                        return 0.5;
                    }

                    double gainA = exchange.getEffectivePrimaryPlayersBalanceDelta();
                    double gainB = exchange.getEffectiveSecondaryPlayersBalanceDelta();

                    if (gainA > gainB)
                        return 1.0;

                    if (gainA > 0)
                        return 0.5;

                    return 0.0;

                } catch (Exchange.TransferNotCommittedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }


            }
        });

        if (Double.isNaN(TFT2))
            TFT2 = 0.5;

        double TFT1 = getEvaluationOfOpponent(opponent, new ExchangeExtractor() {
            @Override
            public double extractValue0_1(Exchange exchange) {
                try {
                    return exchange.getTransferA().getCreditsTransferred() > 0
                            ? (exchange.getTransferB().getRecipientsBalanceDelta() / exchange.getTransferA().getRecipientsBalanceDelta())
                            : (2.0 / 3.0);
                } catch (Exchange.TransferNotCommittedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }
        });

        if (Double.isNaN(TFT1))
            TFT1 = 0.5;
        
        double optimism = getRandom().nextGaussian() * motivationalStrategyComponent.getOptimismStandardDeviation() + motivationalStrategyComponent.getOptimismCoefficient();
        
        double estimatedFractionOfReturn = TFT1 * 2*optimism;
        
        if (estimatedFractionOfReturn>1)
            estimatedFractionOfReturn = 1;
        if (estimatedFractionOfReturn<0)
            estimatedFractionOfReturn = 0;

        double MOTIVATION = motivationalStrategyComponent.calculateMotivationalComplexA(opponent, estimatedFractionOfReturn);

        double gaussian = Math.round(

                getRandom().nextGaussian() * getStandardDeviationA()
                + 100.0 * (
                               getC4()  * ( (getC3() * TFT2) + ((1 - getC3()) * (getBaseMeanA() / 100.0)) )
                       +( (1 - getC4()) * MOTIVATION)
                )

        );

        gaussian = gaussian <= 100 ? gaussian : 100;

        gaussian = gaussian >= 0 ? gaussian : 0;

        return Math.round(creditBalance * (gaussian/100.0));
    }

    @Override
    public double chooseCreditAmountForTransferB(PlayerInterface opponent) {
        try {
            Exchange currentExchange = getPlayer().getGame().getCurrentRoundExchange();
            if (currentExchange.transferAWasExecuted()){
                if (!currentExchange.transferBIsCommitted()){

                    double gainSoFar = currentExchange.getEffectiveSecondaryPlayersBalanceDelta();

                    double TFT1 = getEvaluationOfOpponent(opponent, new ExchangeExtractor() {
                        @Override
                        public double extractValue0_1(Exchange exchange) {
                            try {
                                return exchange.getTransferA().getCreditsTransferred() > 0
                                        ? (exchange.getTransferB().getRecipientsBalanceDelta() / exchange.getTransferA().getRecipientsBalanceDelta())
                                        : (2.0 / 3.0);
                            } catch (Exchange.TransferNotCommittedException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }

                        }
                    });

                    if (Double.isNaN(TFT1))
                        TFT1 = 0.5;

                    //double NUH = currentExchange.getTransferA().getRecipientsBalanceDelta() / getPlayer().getCreditBalance();
                    
                    double MOTIVATION = motivationalStrategyComponent.calculateMotivationalComplexB();

                    double gaussian = Math.round(

                            getRandom().nextGaussian() * getStandardDeviationB()

                            + 100.0
                                *
                                    (
                                          (     getC2()  *   ( (getC1() * TFT1) + ((1 - getC1()) * (getBaseMeanB() / 100.0)) ))
                                        + ((1 - getC2()) *   MOTIVATION)
                                    )

                    );

                    gaussian = gaussian <= 100 ? gaussian : 100;

                    gaussian = gaussian >= 0 ? gaussian : 0;

                    return Math.round((gaussian/100.0)*gainSoFar);

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

    private double getEvaluationOfOpponent(PlayerInterface opponent, ExchangeExtractor extractor){

        List<Exchange> exchanges = new LinkedList<Exchange>(getPlayer().getGame().getExchanges());
        Collections.reverse(exchanges);

        Iterator<Exchange> exchangeIterator = exchanges.iterator();

        double weight = 1.0;
        double weightSum = 0.0;
        double sum = 0.0;

        while (exchangeIterator.hasNext()){

            Exchange exchange = exchangeIterator.next();

            if (exchange.isCompleteAndExecuted()){

                try {

                    PlayerInterface playerA = exchange.getTransferA().getSender();
                    PlayerInterface playerB = exchange.getTransferA().getRecipient();

                    if (playerB.equals(opponent) && (isTitForTatIncludeOtherPlayersExperience() || playerA.equals(getPlayer()))){

                        double extractedValue = extractor.extractValue0_1(exchange);

                        double evaluationWeighted = extractedValue * weight;

                        sum += evaluationWeighted;

                        weightSum += weight;

                        weight *= getWeightingCoefficient();

                    }


                } catch (Exchange.TransferNotCommittedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }


            }

        }

        double result = weightSum>0 ? (sum / weightSum) : Double.NaN;
        getPlayer().getLogger().log(Level.INFO,"TitForTat ... player "+getPlayer().getPlayersName()+" evaluates kindness of opponent "+opponent.getPlayersName()+" as : "+result);
        return result;

    }

    public int getStandardDeviationA() {
        return standardDeviationA;
    }

    public void setStandardDeviationA(int standardDeviationA) {
        this.standardDeviationA = standardDeviationA;
    }

    public int getStandardDeviationB() {
        return standardDeviationB;
    }

    public void setStandardDeviationB(int standardDeviationB) {
        this.standardDeviationB = standardDeviationB;
    }

    public double getWeightingCoefficient() {
        return weightingCoefficient;
    }

    public void setWeightingCoefficient(double weightingCoefficient) {
        this.weightingCoefficient = weightingCoefficient;
    }


    public int getBaseMeanB() {
        return baseMeanB;
    }

    public void setBaseMeanB(int baseMeanB) {
        this.baseMeanB = baseMeanB;
    }

    public int getBaseMeanA() {
        return baseMeanA;
    }

    public void setBaseMeanA(int baseMeanA) {
        this.baseMeanA = baseMeanA;
    }


    public double getC1() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1 = c1;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2 = c2;
    }

    public double getC3() {
        return c3;
    }

    public void setC3(double c3) {
        this.c3 = c3;
    }

    public double getC4() {
        return c4;
    }

    public void setC4(double c4) {
        this.c4 = c4;
    }

    public boolean isTitForTatIncludeOtherPlayersExperience() {
        return titForTatIncludeOtherPlayersExperience;
    }

    public void setTitForTatIncludeOtherPlayersExperience(boolean titForTatIncludeOtherPlayersExperience) {
        this.titForTatIncludeOtherPlayersExperience = titForTatIncludeOtherPlayersExperience;
    }

    protected Random getRandom() {
        return random;
    }


    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public JPanel getConfigurationPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        //JPanel contentPane = new JPanel(new BorderLayout());
        JSplitPane contentPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

//        JScrollPane contentScrollPane = new JScrollPane(contentPane);
//
//        contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(contentPane,BorderLayout.CENTER);

        JPanel formulaPane = new JPanel();
        formulaPane.setBackground(Color.WHITE);

        formulaPane.setLayout(new BorderLayout());
        //formulaPane.setBorder(new LineBorder(Color.darkGray,2));

        String formulaD_A = "<div><font size=+2>D<sub>A</sub>(<font size=-2>opponent</font>) = </font>gauss<sub>[-1;1],1</sub> * <font color=\"#0000FF\">stdDev<sub>A</sub></font> + <font color=\"#0000FF\">c<sub>4</sub></font> * (<font color=\"#0000FF\">c<sub>3</sub></font> * <font color=\"#00FF00\">TitTat<sub>A</sub>(opponent)</font> + (1-<font color=\"#0000FF\">c<sub>3</sub></font>) * <font color=\"#0000FF\">baseMean<sub>A</sub></font>)</div>";

        String formulaD_B = "<div><font size=+2>D<sub>B</sub>(<font size=-2>opponent</font>) = </font>gauss<sub>[-1;1],1</sub> * <font color=\"#0000FF\">stdDev<sub>B</sub></font> + <font color=\"#0000FF\">c<sub>2</sub></font> * (<font color=\"#0000FF\">c<sub>1</sub></font> * <font color=\"#00FF00\">TitTat<sub>B</sub>(opponent)</font> + (1-<font color=\"#0000FF\">c<sub>1</sub></font>) * <font color=\"#0000FF\">baseMean<sub>B</sub></font>) + (1 - <font color=\"#0000FF\">c<sub>2</sub></font>) * <font color=\"#00FF00\">MotivComp</font></div>";


        String formulaTitTat_A = "<div><font size=+1 color=\"#00FF00\">TitTat<sub>A</sub>(<font size=-1>opponent</font>) = </font><font size=+2>&#8721;</font><sub>i=0<sub>,past_rounds</sub></sub><font size=+1>[</font><font color=\"#0000FF\">w</font><sup>i</sup> * f{win:1,winwin:0.5,loss:0}(round)<font size=+1>]</font></div>";

        String formulaTitTat_B = "<div><font size=+1 color=\"#00FF00\">TitTat<sub>B</sub>(<font size=-1>opponent</font>) = </font><font size=+2>&#8721;</font><sub>i=0<sub>,past_rounds</sub></sub><font size=+1>[</font><font color=\"#0000FF\">w</font><sup>i</sup> * (amount<sub>B</sub> / amount_received<sub>A</sub>)<font size=+1>]</font></div>";

        String formulaMotivComp = "<div><font size=+1 color=\"#00FF00\">MotivComp = </font>(-<font color=\"#00FF00\">ego</font>*<font color=\"#00FF00\">EgoComp</font>+<font color=\"#00FF00\">alt</font>*<font color=\"#00FF00\">AltComp</font>-<font color=\"#00FF00\">agg</font>*<font color=\"#00FF00\">AggComp</font>+<font color=\"#00FF00\">cmp</font>*<font color=\"#00FF00\">CmpComp</font>)/(<font color=\"#00FF00\">ego</font>+<font color=\"#00FF00\">alt</font>+<font color=\"#00FF00\">agg</font>+<font color=\"#00FF00\">cmp</font>)</div>";

        String formulaEgo = "<div><font size=+1 color=\"#00FF00\">ego = </font>gauss<sub>[-1;1],1</sub> * <font color=\"#0000FF\">stdDev<sub>EGOISM</sub></font> + <font color=\"#0000FF\">Degree<sub>EGOISM</sub></font></div>";

        String formulaEgoComp = "<div><font size=+1 color=\"#00FF00\">EgoComp = </font>Profit<sub>minAmount(B)=0.0</sub>(thisPlayer) / accountBalance<sub>now and if amount(B)=0.0</sub>(thisPlayer)</div>";

        String formulaAltComp = "<div><font size=+1 color=\"#00FF00\">AltComp = </font>Profit<sub>maxAmount(B)=amount_received(A)</sub>(opponent) / accountBalance<sub>now and if amount(B)=amount_received(A)</sub>(opponent)</div>";

        String formulaAggComp = "<div><font size=+1 color=\"#00FF00\">AggComp = </font>Loss<sub>minAmount(B)=0.0</sub>(opponent) / accountBalance<sub>now and if amount(B)=0.0</sub>(opponent)</div>";

        String formulaCmpComp = "<div><font size=+1 color=\"#00FF00\">CmpComp = </font>(accountBalance<sub>fairestAmount(B)=2/3*amount_received(A)</sub>(thisPlayer) / totalCapitalInGame<sub>now</sub>) - (accountBalance<sub>fairestAmount(B)=2/3*amount_received(A)</sub>(opponent) / totalCapitalInGame<sub>now</sub>)</div>";


        String formulaAbstractHtml =
            "<html><head></head><body>"

                    +formulaD_A
                    +"<br><div><font size=-1>Entscheidungsfunktion D<sub>A</sub> berechnet den Anteil des Guthabens von Spieler A, den A an B überweist. (Gauss-basiert)</font></div><br><br><br>"
                    +formulaD_B
                    +"<br><div><font size=-1>Entscheidungsfunktion D<sub>B</sub> berechnet den Anteil des von A erhaltenen Betrags, den B an A zurücküberweist. (Gauss-basiert)</font></div><br><br><br>"
                    +formulaTitTat_A
                    +"<br><div><font size=-1>Gedächtnisfunktion TitTat<sub>A</sub>(opponent) berechnet den Mittelwert des in früheren Runden<br> von B anteilig zurücküberwiesenen Betrags (dieser wird in eine von drei Klassen eingeordnet:<br> win=\"richtig\" gewonnen, winwin=\"nur\" gewonnen, loss=verloren).<br>Dabei werden Messwerte um so geringer gewichtet, je länger der Zug vergangen ist <br>(steuerbar über <i>w</i>)</font></div><br><br><br>"
                    +formulaTitTat_B
                    +"<br><div><font size=-1>Gedächtnisfunktion TitTat<sub>B</sub>(opponent) berechnet den Mittelwert des in früheren Runden<br> von A anteilig zurücküberwiesenen Betrags.<br>Dabei werden Messwerte um so geringer gewichtet, je länger der Zug vergangen ist <br>(steuerbar über <i>w</i>)</font></div><br><br><br>"
                    +formulaMotivComp
                    +"<br><div><font size=-1>Motivationsfunktion mit Sub-Komponenten: Egoismus, Altruismus, Aggressivität, Konkurrenzdenken</font></div><br><br><br>"
                    +formulaEgo
                    +"<br><div><font size=-1>Momentane Ausprägung der egoistischen Motivationskomponente<br><b>Analog für alt (Altruismus), agg (Aggressivität), cmp (Konkurrenzdenken)</b></font></div><br><br><br>"
                    +formulaEgoComp
                    +"<br><div><font size=-1>Ein egoistischer Spieler möchte seinen eigenen (Zu-)Gewinn (gemessen an seinem aktuellen Guthaben) maximieren</font></div><br><br><br>"
                    +formulaAltComp
                    +"<br><div><font size=-1>Ein altruistischer Spieler möchte den (Zu-)Gewinn seines \"Gegners\" (gemessen an dessem aktuellen Guthaben) maximieren</font></div><br><br><br>"
                    +formulaAggComp
                    +"<br><div><font size=-1>Ein aggressiver Spieler möchte den Verlust/Schaden seines Gegners (gemessen an dessem aktuellen Guthaben) maximieren</font></div><br><br><br>"
                    +formulaCmpComp
                    +"<br><div><font size=-1>Ein Spieler mit starkem Konkurrenzdenken bewertet die Option eines fairen <br> Zugs B danach, ob er damit im Gesamtvergleich besser oder schlechter da stehen würde als der Gegner.<br>Solche Spieler sind quasi nur gegenüber schwächeren Spielern fair, von denen ihnen keine Gefahr droht.</font></div><br><br>"
                +"</body></html>";



        Reader stringReader = new StringReader(formulaAbstractHtml);
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();

        try {
            htmlKit.read(stringReader, htmlDoc, 0);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (BadLocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        JTextPane formulaAbstractAPane = new JTextPane();
        formulaAbstractAPane.setContentType("text/html");

        formulaAbstractAPane.setStyledDocument(htmlDoc);

        formulaAbstractAPane.setEditable(false);

        formulaPane.add(formulaAbstractAPane);

        JTextPane formulaWithValuesAPane = new JTextPane();
        formulaWithValuesAPane.setContentType("text/html");

        JPanel sliderPane = new JPanel(new GridLayout(18,1));

        JPanel meanAPanel = new JPanel(new BorderLayout());
        meanAPanel.setBorder(new TitledBorder("baseMean_A"));
        JPanel meanBPanel = new JPanel(new BorderLayout());
        meanBPanel.setBorder(new TitledBorder("baseMean_B"));
        JPanel stdDevAPanel = new JPanel(new BorderLayout());
        stdDevAPanel.setBorder(new TitledBorder("stdDev_A"));
        JPanel stdDevBPanel = new JPanel(new BorderLayout());
        stdDevBPanel.setBorder(new TitledBorder("stdDev_B"));
        JPanel c1Panel = new JPanel(new BorderLayout());
        c1Panel.setBorder(new TitledBorder("coefficient c_1"));
        JPanel c2Panel = new JPanel(new BorderLayout());
        c2Panel.setBorder(new TitledBorder("coefficient c_2"));
        JPanel c3Panel = new JPanel(new BorderLayout());
        c3Panel.setBorder(new TitledBorder("coefficient c_3"));
        JPanel c4Panel = new JPanel(new BorderLayout());
        c4Panel.setBorder(new TitledBorder("coefficient c_4"));
        JPanel wPanel = new JPanel(new BorderLayout());
        wPanel.setBorder(new TitledBorder("weighting base w"));
        JPanel optimismPanel = new JPanel(new BorderLayout());
        optimismPanel.setBorder(new TitledBorder("degree of optimism (0:pessimistic,1:optimistic)"));

        JPanel egoPanel = new JPanel(new BorderLayout());
        egoPanel.setBorder(new TitledBorder("degree of egoism"));
        JPanel egoStdDevPanel = new JPanel(new BorderLayout());
        egoStdDevPanel.setBorder(new TitledBorder("stdDev of degree of egoism"));
        JPanel altPanel = new JPanel(new BorderLayout());
        altPanel.setBorder(new TitledBorder("degree of altruism"));
        JPanel altStdDevPanel = new JPanel(new BorderLayout());
        altStdDevPanel.setBorder(new TitledBorder("stdDev of degree of altruism"));
        JPanel aggPanel = new JPanel(new BorderLayout());
        aggPanel.setBorder(new TitledBorder("degree of aggressiveness"));
        JPanel aggStdDevPanel = new JPanel(new BorderLayout());
        aggStdDevPanel.setBorder(new TitledBorder("stdDev of degree of aggressiveness"));
        JPanel compPanel = new JPanel(new BorderLayout());
        compPanel.setBorder(new TitledBorder("degree of competitiveness"));
        JPanel compStdDevPanel = new JPanel(new BorderLayout());
        compStdDevPanel.setBorder(new TitledBorder("stdDev of degree of competitiveness"));

        final JSlider meanASlider = new JSlider(JSlider.HORIZONTAL,0,100,getBaseMeanA());
        meanASlider.setMajorTickSpacing(10);
        meanASlider.setMinorTickSpacing(5);
        meanASlider.setLabelTable(meanASlider.createStandardLabels(10));
        meanASlider.setPaintLabels(true);
        meanASlider.setPaintTicks(true);
        meanASlider.setSnapToTicks(true);

        meanASlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setBaseMeanA(meanASlider.getValue());
            }
        });

        final JSlider meanBSlider = new JSlider(JSlider.HORIZONTAL,0,100,getBaseMeanB());
        meanBSlider.setMajorTickSpacing(10);
        meanBSlider.setMinorTickSpacing(5);
        meanBSlider.createStandardLabels(10);
        meanBSlider.setLabelTable(meanBSlider.createStandardLabels(10));
        meanBSlider.setPaintLabels(true);
        meanBSlider.setPaintTicks(true);
        meanBSlider.setSnapToTicks(true);

        meanBSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setBaseMeanB(meanBSlider.getValue());
            }
        });

        final JSlider stdDevASlider = new JSlider(JSlider.HORIZONTAL,0,100,getStandardDeviationA());
        stdDevASlider.setMajorTickSpacing(10);
        stdDevASlider.setMinorTickSpacing(5);
        stdDevASlider.createStandardLabels(10);
        stdDevASlider.setLabelTable(stdDevASlider.createStandardLabels(10));
        stdDevASlider.setPaintLabels(true);
        stdDevASlider.setPaintTicks(true);
        stdDevASlider.setSnapToTicks(true);

        stdDevASlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setStandardDeviationA(stdDevASlider.getValue());
            }
        });

        final JSlider stdDevBSlider = new JSlider(JSlider.HORIZONTAL,0,100,getStandardDeviationB());
        stdDevBSlider.setMajorTickSpacing(10);
        stdDevBSlider.setMinorTickSpacing(5);
        stdDevBSlider.createStandardLabels(10);
        stdDevBSlider.setLabelTable(stdDevBSlider.createStandardLabels(10));
        stdDevBSlider.setPaintLabels(true);
        stdDevBSlider.setPaintTicks(true);
        stdDevBSlider.setSnapToTicks(true);

        stdDevBSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setStandardDeviationB(stdDevBSlider.getValue());
            }
        });

        final JSlider c1Slider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*getC1()));
        c1Slider.setMajorTickSpacing(10);
        c1Slider.setMinorTickSpacing(5);
        c1Slider.createStandardLabels(10);
        c1Slider.setLabelTable(c1Slider.createStandardLabels(10));
        c1Slider.setPaintLabels(true);
        c1Slider.setPaintTicks(true);
        c1Slider.setSnapToTicks(true);

        c1Slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setC1(c1Slider.getValue()/100.0);
            }
        });

        final JSlider c2Slider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*getC2()));
        c2Slider.setMajorTickSpacing(10);
        c2Slider.setMinorTickSpacing(5);
        c2Slider.createStandardLabels(10);
        c2Slider.setLabelTable(c2Slider.createStandardLabels(10));
        c2Slider.setPaintLabels(true);
        c2Slider.setPaintTicks(true);
        c2Slider.setSnapToTicks(true);

        c2Slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setC2(c2Slider.getValue()/100.0);
            }
        });

        final JSlider c3Slider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*getC3()));
        c3Slider.setMajorTickSpacing(10);
        c3Slider.setMinorTickSpacing(5);
        c3Slider.createStandardLabels(10);
        c3Slider.setLabelTable(c3Slider.createStandardLabels(10));
        c3Slider.setPaintLabels(true);
        c3Slider.setPaintTicks(true);
        c3Slider.setSnapToTicks(true);

        c3Slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setC3(c3Slider.getValue()/100.0);
            }
        });

        final JSlider c4Slider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*getC4()));
        c4Slider.setMajorTickSpacing(10);
        c4Slider.setMinorTickSpacing(5);
        c4Slider.createStandardLabels(10);
        c4Slider.setLabelTable(c4Slider.createStandardLabels(10));
        c4Slider.setPaintLabels(true);
        c4Slider.setPaintTicks(true);
        c4Slider.setSnapToTicks(true);

        c4Slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setC4(c4Slider.getValue()/100.0);
            }
        });
        final JSlider wSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*getWeightingCoefficient()));
        wSlider.setMajorTickSpacing(10);
        wSlider.setMinorTickSpacing(5);
        wSlider.createStandardLabels(10);
        wSlider.setLabelTable(wSlider.createStandardLabels(10));
        wSlider.setPaintLabels(true);
        wSlider.setPaintTicks(true);
        wSlider.setSnapToTicks(true);

        wSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setWeightingCoefficient(wSlider.getValue()/100.0);
            }
        });

        final JSlider optimismSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getOptimismCoefficient()));
        optimismSlider.setMajorTickSpacing(10);
        optimismSlider.setMinorTickSpacing(5);
        optimismSlider.createStandardLabels(10);
        optimismSlider.setLabelTable(optimismSlider.createStandardLabels(10));
        optimismSlider.setPaintLabels(true);
        optimismSlider.setPaintTicks(true);
        optimismSlider.setSnapToTicks(true);

        optimismSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setOptimismCoefficient(optimismSlider.getValue()/100.0);
            }
        });

        final JSlider egoSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getEgoismCoefficient()));
        egoSlider.setMajorTickSpacing(10);
        egoSlider.setMinorTickSpacing(5);
        egoSlider.createStandardLabels(10);
        egoSlider.setLabelTable(egoSlider.createStandardLabels(10));
        egoSlider.setPaintLabels(true);
        egoSlider.setPaintTicks(true);
        egoSlider.setSnapToTicks(true);

        egoSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setEgoismCoefficient(egoSlider.getValue()/100.0);
            }
        });

        final JSlider egoStdDevSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getEgoismStandardDeviation()));
        egoStdDevSlider.setMajorTickSpacing(10);
        egoStdDevSlider.setMinorTickSpacing(5);
        egoStdDevSlider.createStandardLabels(10);
        egoStdDevSlider.setLabelTable(egoStdDevSlider.createStandardLabels(10));
        egoStdDevSlider.setPaintLabels(true);
        egoStdDevSlider.setPaintTicks(true);
        egoStdDevSlider.setSnapToTicks(true);

        egoStdDevSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setEgoismStandardDeviation(egoStdDevSlider.getValue()/100.0);
            }
        });

        final JSlider altSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getAltruismCoefficient()));
        altSlider.setMajorTickSpacing(10);
        altSlider.setMinorTickSpacing(5);
        altSlider.createStandardLabels(10);
        altSlider.setLabelTable(altSlider.createStandardLabels(10));
        altSlider.setPaintLabels(true);
        altSlider.setPaintTicks(true);
        altSlider.setSnapToTicks(true);

        altSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setAltruismCoefficient(altSlider.getValue()/100.0);
            }
        });

        final JSlider altStdDevSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getAltruismStandardDeviation()));
        altStdDevSlider.setMajorTickSpacing(10);
        altStdDevSlider.setMinorTickSpacing(5);
        altStdDevSlider.createStandardLabels(10);
        altStdDevSlider.setLabelTable(altStdDevSlider.createStandardLabels(10));
        altStdDevSlider.setPaintLabels(true);
        altStdDevSlider.setPaintTicks(true);
        altStdDevSlider.setSnapToTicks(true);

        altStdDevSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setAltruismStandardDeviation(altStdDevSlider.getValue()/100.0);
            }
        });
        
        final JSlider aggSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getAggressionCoefficient()));
        aggSlider.setMajorTickSpacing(10);
        aggSlider.setMinorTickSpacing(5);
        aggSlider.createStandardLabels(10);
        aggSlider.setLabelTable(aggSlider.createStandardLabels(10));
        aggSlider.setPaintLabels(true);
        aggSlider.setPaintTicks(true);
        aggSlider.setSnapToTicks(true);

        aggSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setAggressionCoefficient(aggSlider.getValue()/100.0);
            }
        });

        final JSlider aggStdDevSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getAggressionStandardDeviation()));
        aggStdDevSlider.setMajorTickSpacing(10);
        aggStdDevSlider.setMinorTickSpacing(5);
        aggStdDevSlider.createStandardLabels(10);
        aggStdDevSlider.setLabelTable(aggStdDevSlider.createStandardLabels(10));
        aggStdDevSlider.setPaintLabels(true);
        aggStdDevSlider.setPaintTicks(true);
        aggStdDevSlider.setSnapToTicks(true);

        aggStdDevSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setAggressionStandardDeviation(aggStdDevSlider.getValue()/100.0);
            }
        });

        final JSlider compSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getCompetitivenessCoefficient()));
        compSlider.setMajorTickSpacing(10);
        compSlider.setMinorTickSpacing(5);
        compSlider.createStandardLabels(10);
        compSlider.setLabelTable(compSlider.createStandardLabels(10));
        compSlider.setPaintLabels(true);
        compSlider.setPaintTicks(true);
        compSlider.setSnapToTicks(true);

        compSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setCompetitivenessCoefficient(compSlider.getValue() / 100.0);
            }
        });

        final JSlider compStdDevSlider = new JSlider(JSlider.HORIZONTAL,0,100,(int)Math.round(100.0*motivationalStrategyComponent.getCompetitivenessStandardDeviation()));
        compStdDevSlider.setMajorTickSpacing(10);
        compStdDevSlider.setMinorTickSpacing(5);
        compStdDevSlider.createStandardLabels(10);
        compStdDevSlider.setLabelTable(compStdDevSlider.createStandardLabels(10));
        compStdDevSlider.setPaintLabels(true);
        compStdDevSlider.setPaintTicks(true);
        compStdDevSlider.setSnapToTicks(true);

        compStdDevSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                motivationalStrategyComponent.setCompetitivenessStandardDeviation(compStdDevSlider.getValue() / 100.0);
            }
        });
        
        meanAPanel.add(meanASlider,BorderLayout.CENTER);
        meanBPanel.add(meanBSlider,BorderLayout.CENTER);
        stdDevAPanel.add(stdDevASlider,BorderLayout.CENTER);
        stdDevBPanel.add(stdDevBSlider,BorderLayout.CENTER);
        c1Panel.add(c1Slider,BorderLayout.CENTER);
        c2Panel.add(c2Slider,BorderLayout.CENTER);
        c3Panel.add(c3Slider,BorderLayout.CENTER);
        c4Panel.add(c4Slider,BorderLayout.CENTER);
        wPanel.add(wSlider,BorderLayout.CENTER);
        optimismPanel.add(optimismSlider,BorderLayout.CENTER);
        egoPanel.add(egoSlider,BorderLayout.CENTER);
        egoStdDevPanel.add(egoStdDevSlider,BorderLayout.CENTER);
        altPanel.add(altSlider,BorderLayout.CENTER);
        altStdDevPanel.add(altStdDevSlider,BorderLayout.CENTER);
        aggPanel.add(aggSlider,BorderLayout.CENTER);
        aggStdDevPanel.add(aggStdDevSlider,BorderLayout.CENTER);
        compPanel.add(compSlider,BorderLayout.CENTER);
        compStdDevPanel.add(compStdDevSlider,BorderLayout.CENTER);

        sliderPane.add(meanAPanel);
        sliderPane.add(stdDevAPanel);
        sliderPane.add(meanBPanel);
        sliderPane.add(stdDevBPanel);
        sliderPane.add(c1Panel);
        sliderPane.add(c2Panel);
        sliderPane.add(c3Panel);
        sliderPane.add(c4Panel);
        sliderPane.add(wPanel);
        sliderPane.add(optimismPanel);
        sliderPane.add(egoPanel);
        sliderPane.add(egoStdDevPanel);
        sliderPane.add(altPanel);
        sliderPane.add(altStdDevPanel);
        sliderPane.add(aggPanel);
        sliderPane.add(aggStdDevPanel);
        sliderPane.add(compPanel);
        sliderPane.add(compStdDevPanel);

        formulaAbstractAPane.setPreferredSize(new Dimension(700, 300));
        formulaAbstractAPane.setMinimumSize(new Dimension(700, 300));

        JScrollPane formulaScrollPane = new JScrollPane(formulaAbstractAPane);
        formulaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formulaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        contentPane.setTopComponent(formulaScrollPane);

        JScrollPane sliderScrollPane = new JScrollPane(sliderPane);

        contentPane.setBottomComponent(sliderScrollPane);

        contentPane.setDividerLocation(300);

        return panel;

    }


}
