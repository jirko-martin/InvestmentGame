package investmentGame.actor;

import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.Game;
import investmentGame.actor.game.PlayerInterface;
import investmentGame.actor.game.player.strategy.RandomStrategy;
import investmentGame.actor.game.player.strategy.Strategy;
import madkit.message.ActMessage;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public class ComputerPlayer extends Player {

    private Strategy strategy;

    private static int instanceCounter = 0;

    public ComputerPlayer(String picturePath,Strategy strategy){
        super("COMPUTER_PLAYER_"+(instanceCounter++),picturePath);
        this.strategy = strategy; //TODO just for the moment!!
    }

    @Override
    protected void live() {

        pause(1000);

        super.live();

    }

    @Override
    public void onMyTurnA() {
        PlayerInterface opponent = strategy.selectOpponentForTransferA();
        double amountCreditsToTransfer = strategy.chooseCreditAmountForTransferA(opponent);
        String transferDesc = "<player> "+getPlayersName()+" <decides_to_transfer> "+((int)amountCreditsToTransfer)+" <credits> <to> <player> "+opponent.getPlayersName();
        receiveMessage(new ActMessage("decide_on_transfer_A",transferDesc));
    }

    @Override
    public void onMyTurnB() {
        try {
            PlayerInterface opponent = game.getCurrentRoundExchange().getTransferA().getSender();
            double amountCreditsToTransfer = strategy.chooseCreditAmountForTransferB(opponent);
            String transferDesc = "<player> "+getPlayersName()+" <decides_to_transfer> "+((int)amountCreditsToTransfer)+" <credits> <to> <player> "+opponent.getPlayersName();
            receiveMessage(new ActMessage("decide_on_transfer_B",transferDesc));
        } catch (Exchange.TransferNotCommittedException e) {
            e.printStackTrace();
        } catch (Game.GameNotStartedYetException e) {
            e.printStackTrace();
        }

    }
}
