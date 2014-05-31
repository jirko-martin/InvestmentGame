package investmentGame.actor.game.player.strategy;

import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.PlayerInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 31/05/14
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */
public class ExcludePlayerSelectOpponentStrategy extends SelectOpponentStrategy{

    private String playerToExclude = "";
    private int excludeAfterNRounds = 0;

    @Override
    public String getStrategyName() {
        return "Exclude Player Strategy";
    }

    @Override
    public String getFullDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PlayerInterface selectOpponentForTransferA() {

        Iterator<PlayerInterface> playerInterfaceIterator = getPlayer().getOpponents().iterator();

        int rounds = getPlayer().getGame().getExchanges().size();

        int minFrequency = Integer.MAX_VALUE;
        PlayerInterface minFrequentPlayer = null;

        while (playerInterfaceIterator.hasNext()){

            PlayerInterface playerInterface = playerInterfaceIterator.next();

            if (rounds>getExcludeAfterNRounds()){
                String playersName = playerInterface.getPlayersName();
                if (playersName.matches(getPlayerToExclude())){
                    continue;
                }
            }



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
    public boolean isConfigurable() {
        return true;
    }

    public String getPlayerToExclude() {
        return playerToExclude;
    }

    public void setPlayerToExclude(String playerToExclude) {
        this.playerToExclude = playerToExclude;
    }

    public int getExcludeAfterNRounds() {
        return excludeAfterNRounds;
    }

    public void setExcludeAfterNRounds(int excludeAfterNRounds) {
        this.excludeAfterNRounds = excludeAfterNRounds;
    }

    @Override
    public JPanel getConfigurationPanel() {
        JPanel panel = new JPanel(new GridLayout(2,2));

        panel.add(new JLabel("Welchen Spieler ausschließen (REGEX) ?"));
        final JTextField excludePlayerInput = new JTextField(playerToExclude);

        excludePlayerInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void focusLost(FocusEvent e) {
                setPlayerToExclude(excludePlayerInput.getText());
            }
        });

        panel.add(excludePlayerInput);
        panel.add(new JLabel("Nach wieviel Runden ausschließen?"));
        final JTextField nRoundsInput = new JTextField(String.valueOf(excludeAfterNRounds));

        nRoundsInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void focusLost(FocusEvent e) {
                try{
                    int n = Integer.parseInt(nRoundsInput.getText());
                    setExcludeAfterNRounds(n);
                }catch (NumberFormatException nfe){
                    nRoundsInput.setText(String.valueOf(excludeAfterNRounds));
                }
            }
        });

        panel.add(nRoundsInput);


        return panel;
    }
}
