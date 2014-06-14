package investmentGame.actor;

import investmentGame.actor.game.Game;
import investmentGame.actor.game.ModelPlayer;
import investmentGame.actor.game.PlayerInterface;
import investmentGame.actor.game.player.PlayersGame;
import investmentGame.actor.game.player.PlayersModelPlayer;
import investmentGame.actor.game.Transfer;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.message.ActMessage;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public abstract class Player extends Agent implements PlayerInterface {

    protected PlayersGame game;

    private ModelPlayer model;

    private JFrame frame;

    public Player(){
        this("anonymous",null);
    }

    public Player(String name, URI picturePath){
        this.model = new PlayersModelPlayer(null,name,picturePath);
        setName(name);
    }


    @Override
    protected void activate() {
        setLogLevel(Level.FINEST);

        createGroupIfAbsent("investment_game", "tout_le_monde");
        requestRole("investment_game","tout_le_monde","player");

        pause(500);
    }

    @Override
    protected void live() {
        ActMessage message;
        while ((message=(ActMessage)waitNextMessage())!=null){
            if (message.getAction().equals("welcome") && !game.isStarted()){
                game.start();
                logger.log(Level.INFO, "PLAYER " + getName() + " joined GAME " + game.getGameId());
            }else{
                if (game!=null){

                    game.processMessageEvent(message);

                }
            }
        }
    }

    public void joinGame(String gameId, boolean asPrimaryPlayer, URI picturePath){
        AgentAddress coordinator = findCoordinator(gameId);

        requestRole("investment_game",gameId,"player");
        requestRole("investment_game", gameId, "player_" + getPlayersName());

        PlayersGame game = new PlayersGame(gameId,this);

        game.addPlayer(this);

        this.game = game;

        this.model = new PlayersModelPlayer(game,getPlayersName(),picturePath);

        String playerDesc = (asPrimaryPlayer ? "<primary>" : "<secondary>")+" <player> "+getPlayersName()+" <looking_like_that> "+picturePath;

        sendMessageWithRole(coordinator, new ActMessage("hello_my_name_is", playerDesc), "player");

    }

    private AgentAddress findCoordinator(String groupName){
        AgentAddress coordinatorAddress = null;

        while (coordinatorAddress == null){
            pause(1000);
            coordinatorAddress = getAgentWithRole("investment_game",groupName,"coordinator");
        }

        return coordinatorAddress;
    }


    @Override
    public void setCreditBalance(double balance) {
        this.model.setCreditBalance(balance);
    }

    @Override
    public double getCreditBalance() {
        return this.model.getCreditBalance();
    }

    @Override
    public URI getPicturePath() {
        return this.model.getPicturePath();
    }

    @Override
    public String getStringDescription() {
        return this.model.getStringDescription();
    }

    @Override
    public JPanel getGUIView() {
        return this.model.getGUIView();
    }

    @Override
    public Image getScaledPicture(int width) {
        return this.model.getScaledPicture(width);
    }

    @Override
    public Game getGame() {
        return this.model.getGame();
    }

    @Override
    public void setSelectable(boolean selectable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canMakeTransfer(Transfer transfer) {
        return this.model.canMakeTransfer(transfer);
    }

    @Override
    public void makeTransfer(Transfer transfer) throws Transfer.InvalidTransferException {
        this.model.makeTransfer(transfer);
    }

    @Override
    public void receiveTransfer(Transfer transfer) {
        this.model.receiveTransfer(transfer);
    }

    @Override
    public String getPlayersName() {
        return this.model.getPlayersName();
    }

    public Collection<PlayerInterface> getOpponents(){
        Iterator<PlayerInterface> playerIterator = game.getPlayers().iterator();
        Set<PlayerInterface> opponents = new HashSet<PlayerInterface>();
        while (playerIterator.hasNext()){
            PlayerInterface player = playerIterator.next();
            if (!player.getPlayersName().equals(getPlayersName()))
                opponents.add(player);
        }
        return opponents;
    }

    public void onMyTurnA(){

    }

    public void onMyTurnB(){

    }

    @Override
    public void setupFrame(JFrame frame) {
        this.frame = frame;
        frame.setPreferredSize(new Dimension(1024,768));
    }

    public JFrame getFrame() {
        return frame;
    }

    public void pauseAWhile(int msec){
        pause(msec);
    }

    public boolean equals(Player other){
        return other.getPlayersName().equals(getPlayersName());
    }

    public void selectPlayer(PlayerInterface player){

    }

    public String toString(){
        return this.getClass().getName()+"["+super.toString()+" -- "+getPlayersName()+"]";
    }
}
