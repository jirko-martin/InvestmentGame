package investmentGame.actor.game.player.strategy;

import investmentGame.Configuration;
import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.Game;
import investmentGame.actor.game.PlayerInterface;
import investmentGame.swing.RoundedPanel;
import javafx.scene.web.HTMLEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
public class TitForNWeightedTatsStrategy extends Strategy{


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

    private double c4 = 0.9;


    private boolean titForTatIncludeOtherPlayersExperience = true;


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
    public PlayerInterface selectOpponentForTransferA() {

        Iterator<PlayerInterface> playerInterfaceIterator = getPlayer().getOpponents().iterator();

        int minFrequency = Integer.MAX_VALUE;
        PlayerInterface minFrequentPlayer = null;

        while (playerInterfaceIterator.hasNext()){

            PlayerInterface playerInterface = playerInterfaceIterator.next();

            Iterator<Exchange> exchangeIterator = getPlayer().getGame().getExchanges().iterator();

            int frequency = 0;

            while (exchangeIterator.hasNext()){

                Exchange exchange = exchangeIterator.next();

                try {

                    if (exchange.transferAIsCommitted()){

                        PlayerInterface primaryPlayerExchange = exchange.getTransferA().getSender();
                        PlayerInterface secondaryPlayerExchange = exchange.getTransferA().getRecipient();

                        if (primaryPlayerExchange.equals(getPlayer()) && secondaryPlayerExchange.equals(playerInterface)){
                            frequency++;
                        }
                    }

                } catch (Exchange.TransferNotCommittedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }

            if (frequency<minFrequency){
                minFrequency = frequency;
                minFrequentPlayer = playerInterface;
            }else
                if (frequency==minFrequency){
                    if (Math.random()>0.5){
                        minFrequentPlayer = playerInterface;
                    }
                }

        }

        if (minFrequentPlayer==null){
            ArrayList<PlayerInterface> opponents = new ArrayList<PlayerInterface>(getPlayer().getOpponents());

            return opponents.get((int)Math.round(Math.random()*(opponents.size()-1)));
        }else{
            return minFrequentPlayer;
        }

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


        double gaussian = Math.round(

                getRandom().nextGaussian() * getStandardDeviationA()
                + 100.0 * getC4() * ( (getC3() * TFT2) + ((1 - getC3()) * (getBaseMeanA() / 100.0)) )

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

                    double NUH = currentExchange.getTransferA().getRecipientsBalanceDelta() / getPlayer().getCreditBalance();

                    double gaussian = Math.round(

                            getRandom().nextGaussian() * getStandardDeviationB()
                            + (100.0 * ( (getC1() * TFT1) + ((1 - getC1()) * (getBaseMeanB() / 100.0)) ))
                            - (100.0 *   (getC2() * NUH))

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
        } catch (Exchange.TransferNotCommittedException e) {
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

        JPanel contentPane = new JPanel(new BorderLayout());

        panel.add(new JScrollPane(contentPane),BorderLayout.CENTER);

        JPanel formulaPane = new JPanel();
        formulaPane.setBackground(Color.WHITE);

        formulaPane.setLayout(new GridLayout(1,1));
        formulaPane.setBorder(new LineBorder(Color.darkGray,2));

        String formulaD_A = "<div><font size=+2>D<sub>A</sub>(<font size=-2>opponent</font>) = </font>gauss<sub>[-1;1],1</sub> * <font color=\"#0000FF\">stdDev<sub>A</sub></font> + <font color=\"#0000FF\">c<sub>4</sub></font> * (<font color=\"#0000FF\">c<sub>3</sub></font> * <font color=\"#00FF00\">TitTat<sub>A</sub>(opponent)</font> + (1-<font color=\"#0000FF\">c<sub>3</sub></font>) * <font color=\"#0000FF\">baseMean<sub>A</sub></font>)</div>";

        String formulaD_B = "<div><font size=+2>D<sub>B</sub>(<font size=-2>opponent</font>) = </font>gauss<sub>[-1;1],1</sub> * <font color=\"#0000FF\">stdDev<sub>B</sub></font> + <font color=\"#0000FF\">c<sub>1</sub></font> * <font color=\"#00FF00\">TitTat<sub>B</sub>(opponent)</font> + (1-<font color=\"#0000FF\">c<sub>1</sub></font>) * <font color=\"#0000FF\">baseMean<sub>B</sub></font> - <font color=\"#0000FF\">c<sub>2</sub></font> * <font color=\"#00FF00\">BenefitBetray</font></div>";


        String formulaTitTat_A = "<div><font size=+1 color=\"#00FF00\">TitTat<sub>A</sub>(<font size=-1>opponent</font>) = </font><font size=+2>&#8721;</font><sub>i=0<sub>,past_rounds</sub></sub><font size=+1>[</font><font color=\"#0000FF\">w</font><sup>i</sup> * f{win:1,winwin:0.5,loss:0}(round)<font size=+1>]</font></div>";

        String formulaTitTat_B = "<div><font size=+1 color=\"#00FF00\">TitTat<sub>B</sub>(<font size=-1>opponent</font>) = </font><font size=+2>&#8721;</font><sub>i=0<sub>,past_rounds</sub></sub><font size=+1>[</font><font color=\"#0000FF\">w</font><sup>i</sup> * (amount<sub>B</sub> / amount_received<sub>A</sub>)<font size=+1>]</font></div>";

        String formulaBenefitBetray = "<div><font size=+1 color=\"#00FF00\">BenefitBetray</font> = amount_received(A_current) / current_own_credit_account_balance</div>";

        String formulaAbstractHtml =
            "<html><head></head><body>"

                    +formulaD_A
                    +"<div><font size=-1>Entscheidungsfunktion D<sub>A</sub> berechnet den Anteil des Guthabens von Spieler A, den A an B überweist. (Gauss-basiert)</font></div><br><br>"
                    +formulaD_B
                    +"<div><font size=-1>Entscheidungsfunktion D<sub>B</sub> berechnet den Anteil des von A erhaltenen Betrags, den B an A zurücküberweist. (Gauss-basiert)</font></div><br><br>"
                    +formulaTitTat_A
                    +"<div><font size=-1>Gedächtnisfunktion TitTat<sub>A</sub>(opponent) berechnet den Mittelwert des in früheren Runden<br> von B anteilig zurücküberwiesenen Betrags (dieser wird in eine von drei Klassen eingeordnet:<br> win=\"richtig\" gewonnen, winwin=\"nur\" gewonnen, loss=verloren).<br>Dabei werden Messwerte um so geringer gewichtet, je länger der Zug vergangen ist <br>(steuerbar über <i>w</i>)</font></div><br><br>"
                    +formulaTitTat_B
                    +"<div><font size=-1>Gedächtnisfunktion TitTat<sub>B</sub>(opponent) berechnet den Mittelwert des in früheren Runden<br> von A anteilig zurücküberwiesenen Betrags.<br>Dabei werden Messwerte um so geringer gewichtet, je länger der Zug vergangen ist <br>(steuerbar über <i>w</i>)</font></div><br><br>"
                    +formulaBenefitBetray
                    +"<div><font size=-1>Eigennutzenfunktion BenefitBetray berechnet das Verhältnis von gerade in A erhaltenem Betrag und eigenem aktuellen Guthabenstand. <br>Je größer der Quotient ist, desto mehr lohnt sich unfaires Verhalten in B für den Spieler.</font></div><br><br>"
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


        //formulaPane.add(formulaWithValuesAPane);

        JPanel sliderPane = new JPanel(new GridLayout(9,1));

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

        meanAPanel.add(meanASlider,BorderLayout.CENTER);
        meanBPanel.add(meanBSlider,BorderLayout.CENTER);
        stdDevAPanel.add(stdDevASlider,BorderLayout.CENTER);
        stdDevBPanel.add(stdDevBSlider,BorderLayout.CENTER);
        c1Panel.add(c1Slider,BorderLayout.CENTER);
        c2Panel.add(c2Slider,BorderLayout.CENTER);
        c3Panel.add(c3Slider,BorderLayout.CENTER);
        c4Panel.add(c4Slider,BorderLayout.CENTER);
        wPanel.add(wSlider,BorderLayout.CENTER);

        sliderPane.add(meanAPanel);
        sliderPane.add(stdDevAPanel);
        sliderPane.add(meanBPanel);
        sliderPane.add(stdDevBPanel);
        sliderPane.add(c1Panel);
        sliderPane.add(c2Panel);
        sliderPane.add(c3Panel);
        sliderPane.add(c4Panel);
        sliderPane.add(wPanel);

        contentPane.add(formulaPane,BorderLayout.NORTH);

        contentPane.add(sliderPane,BorderLayout.CENTER);

        return panel;

    }


}
