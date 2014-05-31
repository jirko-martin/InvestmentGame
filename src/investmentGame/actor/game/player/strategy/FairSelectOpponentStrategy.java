package investmentGame.actor.game.player.strategy;

import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.PlayerInterface;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 31/05/14
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public class FairSelectOpponentStrategy extends SelectOpponentStrategy{

    @Override
    public String getStrategyName() {
        return "Fair Select Opponent";
    }

    @Override
    public String getFullDescription() {
        return getStrategyName();
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
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public JPanel getConfigurationPanel() {
        return null;
    }

}
