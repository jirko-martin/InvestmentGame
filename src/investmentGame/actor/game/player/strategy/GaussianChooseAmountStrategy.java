package investmentGame.actor.game.player.strategy;

import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.Game;
import investmentGame.actor.game.PlayerInterface;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 27/03/14
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */
public class GaussianChooseAmountStrategy extends ChooseAmountStrategy {

    //the values mean percentages

    private int meanA = 15;
    private int meanB = 65;
    private int standardDeviationA = 10;
    private int standardDeviationB = 10;

    private Random random = new Random();

    protected Random getRandom() {
        return random;
    }

    public int getMeanA() {
        return meanA;
    }

    public void setMeanA(int meanA) {
        this.meanA = meanA;
    }

    public int getMeanB() {
        return meanB;
    }

    public void setMeanB(int meanB) {
        this.meanB = meanB;
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


    @Override
    public String getStrategyName() {
        return "Zufall (Gauss)";
    }

    @Override
    public String getFullDescription() {
        return toString();
    }

    @Override
    public double chooseCreditAmountForTransferA(PlayerInterface opponent) {
        double creditBalance = getPlayer().getCreditBalance();

        double gaussian = Math.round(getRandom().nextGaussian() * getStandardDeviationA() + getMeanA());

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

                    double gaussian = Math.round(getRandom().nextGaussian() * getStandardDeviationB() + getMeanB());

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

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public JPanel getConfigurationPanel() {

        JPanel panel = new JPanel(new GridLayout(4,1));

        panel.setBackground(Color.WHITE);

        JPanel meanAPanel = new JPanel(new BorderLayout());
        meanAPanel.setBorder(new TitledBorder("Erwartungswert A"));
        JPanel meanBPanel = new JPanel(new BorderLayout());
        meanBPanel.setBorder(new TitledBorder("Erwartungswert B"));
        JPanel stdDevAPanel = new JPanel(new BorderLayout());
        stdDevAPanel.setBorder(new TitledBorder("Standardabweichung A"));
        JPanel stdDevBPanel = new JPanel(new BorderLayout());
        stdDevBPanel.setBorder(new TitledBorder("Standardabweichung B"));

        final JSlider meanASlider = new JSlider(JSlider.HORIZONTAL,0,100,getMeanA());
        meanASlider.setMajorTickSpacing(10);
        meanASlider.setMinorTickSpacing(5);
        meanASlider.setLabelTable(meanASlider.createStandardLabels(10));
        meanASlider.setPaintLabels(true);
        meanASlider.setPaintTicks(true);
        meanASlider.setSnapToTicks(true);

        meanASlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setMeanA(meanASlider.getValue());
            }
        });

        final JSlider meanBSlider = new JSlider(JSlider.HORIZONTAL,0,100,getMeanB());
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
                setMeanB(meanBSlider.getValue());
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

        meanAPanel.add(meanASlider,BorderLayout.CENTER);
        meanBPanel.add(meanBSlider,BorderLayout.CENTER);
        stdDevAPanel.add(stdDevASlider,BorderLayout.CENTER);
        stdDevBPanel.add(stdDevBSlider,BorderLayout.CENTER);


        panel.add(meanAPanel);
        panel.add(stdDevAPanel);
        panel.add(meanBPanel);
        panel.add(stdDevBPanel);

        panel.setPreferredSize(new Dimension(330,480));

        return panel;

    }
}
