package investmentGame.actor;

import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.Game;
import investmentGame.actor.game.PlayerInterface;
import investmentGame.actor.game.player.strategy.ChooseAmountStrategy;
import investmentGame.actor.game.player.strategy.SelectOpponentStrategy;
import madkit.message.ActMessage;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public class ComputerPlayer extends Player {

    private ChooseAmountStrategy chooseAmountStrategy;
    private SelectOpponentStrategy selectOpponentStrategy;

    public static int instanceCounter = 0;

    public ComputerPlayer(String name, URI picturePath,ChooseAmountStrategy chooseAmountStrategy, SelectOpponentStrategy selectOpponentStrategy){
        super(name,picturePath);
        this.chooseAmountStrategy = chooseAmountStrategy;
        this.selectOpponentStrategy = selectOpponentStrategy;
    }

    @Override
    protected void live() {

        pause(1000);

        super.live();

    }

    @Override
    public void onMyTurnA() {
        PlayerInterface opponent = selectOpponentStrategy.selectOpponentForTransferA();
        double amountCreditsToTransfer = chooseAmountStrategy.chooseCreditAmountForTransferA(opponent);
        String transferDesc = "<player> "+getPlayersName()+" <decides_to_transfer> "+((int)amountCreditsToTransfer)+" <credits> <to> <player> "+opponent.getPlayersName();
        receiveMessage(new ActMessage("decide_on_transfer_A",transferDesc));
    }

    @Override
    public void onMyTurnB() {
        try {
            PlayerInterface opponent = game.getCurrentRoundExchange().getTransferA().getSender();
            double amountCreditsToTransfer = chooseAmountStrategy.chooseCreditAmountForTransferB(opponent);
            String transferDesc = "<player> "+getPlayersName()+" <decides_to_transfer> "+((int)amountCreditsToTransfer)+" <credits> <to> <player> "+opponent.getPlayersName();
            receiveMessage(new ActMessage("decide_on_transfer_B",transferDesc));
        } catch (Exchange.TransferNotCommittedException e) {
            e.printStackTrace();
        } catch (Game.GameNotStartedYetException e) {
            e.printStackTrace();
        }

    }
}
