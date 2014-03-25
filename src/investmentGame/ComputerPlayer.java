package investmentGame;

import madkit.message.ActMessage;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public class ComputerPlayer extends Player{

    private Strategy strategy;

    private boolean launchedByCoordinator;

    private int test;

    private static int instanceCounter = 0;

    public ComputerPlayer(){
        this(false);
     }

    public ComputerPlayer(boolean launchedByCoordinator){
        super("COMPUTER_PLAYER_"+(instanceCounter++),null);
        this.launchedByCoordinator = launchedByCoordinator;
        this.test = Math.round((int)Math.random()*10000);
        strategy = new RandomStrategy(this); //TODO just for the moment!!
    }

    @Override
    protected void live() {

        if (!launchedByCoordinator){

            playGame(5,4);

        }
        pause(3000);

        super.live();

    }

    @Override
    public void onMyTurnA() {
        PlayerInterface opponent = strategy.selectOpponentForTransferA();
        double amountCreditsToTransfer = strategy.chooseCreditAmountForTransferA(opponent);
        String transferDesc = "<player> "+getPlayersName()+" <decides_to_transfer> "+((int)amountCreditsToTransfer)+" <credits> <to> <player> "+opponent.getPlayersName();
        //getLogger().log(Level.INFO,getPlayersName()+" --- broadcast to role "+"player_"+getPlayersName());
        //broadcastMessageWithRole("investment_game", game.getGameId(), "player_" + getPlayersName(), new ActMessage("decide_on_transfer_A", transferDesc), "player_" + getPlayersName());
        receiveMessage(new ActMessage("decide_on_transfer_A",transferDesc));
    }

    @Override
    public void onMyTurnB() {
        PlayerInterface opponent = game.playerAtTurnA;
        double amountCreditsToTransfer = strategy.chooseCreditAmountForTransferB(opponent);
        String transferDesc = "<player> "+getPlayersName()+" <decides_to_transfer> "+((int)amountCreditsToTransfer)+" <credits> <to> <player> "+opponent.getPlayersName();
        //broadcastMessageWithRole("investment_game",game.getGameId(),"player_"+getPlayersName(),new ActMessage("decide_on_transfer_B",transferDesc),"player_"+getPlayersName());
        receiveMessage(new ActMessage("decide_on_transfer_B",transferDesc));
    }
}
