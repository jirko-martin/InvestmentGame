package investmentGame;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.message.ActMessage;

import javax.swing.*;
import java.awt.*;
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
public abstract class Player extends Agent implements PlayerInterface{

    protected PlayersGame game;

    private ModelPlayer model;

    private JFrame frame;

    public Player(){
        this("anonymous",null);
    }

    public Player(String name,ImageIcon picture){
        this.model = new ModelPlayer(name,picture);
        setName(name);
    }


    @Override
    protected void activate() {
        setLogLevel(Level.INFO);

        createGroupIfAbsent("investment_game", "tout_le_monde");
        requestRole("investment_game","tout_le_monde","player");

        pause(500);
    }

    @Override
    protected void live() {
        ActMessage message;
        while ((message=(ActMessage)waitNextMessage())!=null){
            if (game!=null){
                game.processMessageEvent(message);
            }
        }
    }

    public void playGame(int numberOfPlayersTotal, int numberOfComputerOpponentsToInvite){
        AgentAddress coordinatorAddress = findCoordinator("tout_le_monde");

        String requestGameSpec = ""+numberOfPlayersTotal+" <players> <thereof> "+numberOfComputerOpponentsToInvite+" <computer_players>";

        ActMessage reply = (ActMessage)sendMessageWithRoleAndWaitForReply(coordinatorAddress,new ActMessage("request_game",requestGameSpec),"player");

        if (reply.getAction().equals("game_initialized")){

            joinGame(reply.getContent());

        }

    }

    public PlayersGame joinGame(String gameId){
        AgentAddress coordinator = findCoordinator(gameId);

        requestRole("investment_game",gameId,"player");
        requestRole("investment_game", gameId, "player_" + getPlayersName());

        PlayersGame game = new PlayersGame(gameId,this);

        game.addPlayer(this);

        this.game = game;

        ActMessage reply = (ActMessage)sendMessageWithRoleAndWaitForReply(coordinator,new ActMessage("hello_my_name_is",getPlayersName()),"player");

        if (reply.getAction().equals("welcome")){
            game.start();
            logger.log(Level.INFO,"PLAYER "+getName()+" joined GAME "+gameId);
        }

        return game;
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
    public Transaction transferA(PlayerInterface recipient, double credits) {
        getLogger().log(Level.INFO,"I own "+getCreditBalance()+" credits currently. \nI will A-transfer "+credits+" credits to "+recipient.getPlayersName());

        return this.model.transferA(recipient,credits);
    }

    @Override
    public Transaction transferB(PlayerInterface recipient, double credits) {
        getLogger().log(Level.INFO,"I own "+getCreditBalance()+" credits currently. \nI will B-transfer "+credits+" credits to "+recipient.getPlayersName());

        return this.model.transferB(recipient,credits);
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
    public ImageIcon getPicture() {
        return this.model.getPicture();
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
    public String getPlayersName() {
        return this.model.getPlayersName();
    }

    public Collection<PlayerInterface> getOpponents(){
        Iterator<PlayerInterface> playerIterator = game.getPlayers().iterator();
        Set<PlayerInterface> opponents = new HashSet<PlayerInterface>();
        while (playerIterator.hasNext()){
            logger.log(Level.INFO,"next opponent");
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
        frame.setPreferredSize(new Dimension(800,600));
    }

    public JFrame getFrame() {
        return frame;
    }

    public void pauseAWhile(int msec){
        pause(msec);
    }
}
