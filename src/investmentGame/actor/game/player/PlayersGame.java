package investmentGame.actor.game.player;

import investmentGame.actor.game.*;
import investmentGame.actor.Player;
import madkit.message.ActMessage;
import org.jfree.layout.RadialLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */
public class PlayersGame extends Game {

    private Player playersSelf;

    public final Map<String, GameState<PlayersGame>> gameStates = new HashMap<String, GameState<PlayersGame>>();

    private GUI gui;

    public class GUI{

        public class Panel extends JPanel{

            Transfer transfer;
            Exchange exchange;

            private final int ARR_SIZE = 14;

            public Panel(LayoutManager manager){
                super(manager);
            }

            void drawArrow(Graphics g1, int x1, int y1, int x2, int y2, Color color, double credits) {
                Graphics2D g = (Graphics2D) g1.create();

                double dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);

                double b = Math.sqrt(((20*20)*(dy*dy))/((dx*dx)+(dy*dy)));
                double a = (dx*b)/dy;

                getPlayersSelf().getLogger().log(Level.INFO,"a = "+a+" , b = "+b);

                if (x1<x2){
                    x2 -= (dy>0 ? a : 20);
                    x1 += (dy>0 ? a : 20);
                }else{
                    x2 += (dy>0 ? a : 20);
                    x1 -= (dy>0 ? a : 20);
                }

                if (y1<y2){
                    y2 -= b;
                    y1 += b;
                }else{
                    y2 += b;
                    y1 -= b;
                }

                dx = x2 - x1;
                dy = y2 - y1;
                double angle = Math.atan2(dy, dx);
                int len = (int) Math.sqrt(dx*dx + dy*dy);

                AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
                at.concatenate(AffineTransform.getRotateInstance(angle));
                g.transform(at);

                // Draw horizontal arrow starting in (0, 0)
                g.setStroke(new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g.setColor(color);
                g.drawLine(0, 0, len, 0);
                g.fillPolygon(new int[]{len, len - ARR_SIZE, len - ARR_SIZE, len},
                        new int[]{0, -ARR_SIZE, ARR_SIZE, 0}, 4);

                g.setColor(Color.BLACK);

                dx = Math.abs(dx); dy = Math.abs(dy);

                b = Math.sqrt((((len/2)*(len/2))*(dy*dy))/((dx*dx)+(dy*dy)));
                a = (dx*b)/dy;

                if (x1<x2)
                    x2 -= (dy>0 ? a : len/2);
                else
                    x2 += (dy>0 ? a : len/2);

                if (y1<y2)
                    y2 -= b;
                else
                    y2 += b;

                g.setTransform(new AffineTransform());

                g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,20));

                g.drawString(""+((int)credits),x2,y2);

            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (transfer !=null){

                    Rectangle r1 = transfer.getSender().getGUIView().getBounds();

                    System.err.println("r1="+r1);

                    System.err.println("transfer ... " + transfer);

                    Rectangle r2 = transfer.getRecipient().getGUIView().getBounds();
                    
                    Point[] ps1 = {
                            new Point(r1.getLocation().x+(r1.width/2),r1.getLocation().y),
                            new Point(r1.getLocation().x+(r1.width/2),r1.getLocation().y+r1.height),
                            new Point(r1.getLocation().x,r1.getLocation().y+(r1.height/2)),
                            new Point(r1.getLocation().x+r1.width,r1.getLocation().y+(r1.height/2))
                    };
                    
                    Point[] ps2 = {
                            new Point(r2.getLocation().x+(r2.width/2),r2.getLocation().y),
                            new Point(r2.getLocation().x+(r2.width/2),r2.getLocation().y+r2.height),
                            new Point(r2.getLocation().x,r2.getLocation().y+(r2.height/2)),
                            new Point(r2.getLocation().x+r2.width,r2.getLocation().y+(r2.height/2))
                    };

                    Point p1=null,p2=null;
                    double minDistance = Double.MAX_VALUE;
                    for (int i=0;i<ps1.length;i++)
                        for (int j=0;j<ps2.length;j++){
                            double distance;
                            if ((distance=ps1[i].distance(ps2[j]))<minDistance){
                                minDistance=distance;
                                p1=ps1[i];
                                p2=ps2[j];
                            }
                        }
                    
                    drawArrow(g,p1.x,p1.y,p2.x,p2.y, transfer.getType()== Transfer.TYPE_A?Color.GREEN:Color.ORANGE, transfer.getCreditsTransferred());



                    (new Thread(){
                        public void run(){
                            try{
                                Thread.sleep(2000);
                                showTransaction(null,transfer instanceof Exchange.TransferA ? exchange : null);
                            }catch (InterruptedException e){

                            }
                        }
                    }).start();
                }

                if (exchange != null){

                    try {

                        Rectangle r1 = exchange.getTransferA().getSender().getGUIView().getBounds();
                        Rectangle r2 = exchange.getTransferA().getRecipient().getGUIView().getBounds();

                        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));

                        if (exchange.getEffectivePrimaryPlayersBalanceDelta()>0)
                            g.setColor(new Color(15, 182, 23));
                        else
                            g.setColor(new Color(190, 25, 0));

                        g.fill3DRect(r1.x+r1.width-90,r1.y+r1.height,90,23,false);

                        g.setColor(Color.white);

                        g.drawString((exchange.getEffectivePrimaryPlayersBalanceDelta()>0?"+":"")+((int)exchange.getEffectivePrimaryPlayersBalanceDelta()),r1.x+r1.width-90+3,r1.y+r1.height+18);

                        if (exchange.getEffectiveSecondaryPlayersBalanceDelta()>0)
                            g.setColor(new Color(15, 182, 23));
                        else
                            g.setColor(new Color(190, 25, 0));

                        g.fill3DRect(r2.x+r2.width-90,r2.y+r2.height,90,23,false);

                        g.setColor(Color.white);

                        g.drawString((exchange.getEffectiveSecondaryPlayersBalanceDelta()>0?"+":"")+((int)exchange.getEffectiveSecondaryPlayersBalanceDelta()),r2.x+r2.width-90+3,r2.y+r2.height+18);

                    } catch (Exchange.TransferNotCommittedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }

            }

            public void showTransaction(Transfer transfer,Exchange exchange){
                this.transfer = transfer;
                this.exchange = exchange;
                this.validate();
                this.repaint();

            }
        }

        private Panel panel;

        public void update(){

        }

        public void showTransaction(Transfer transfer, Exchange exchange){
            if (panel!=null)
                panel.showTransaction(transfer,exchange);
        }

        public JPanel getPanel(){
            panel = new Panel(new RadialLayout());

            panel.setBackground(new Color(66, 219, 222));

            ArrayList<PlayerInterface> players = new ArrayList<PlayerInterface>(getPlayers());

            if (players.size()==2){

                panel.add(new JPanel());

                panel.add(players.get(0).getGUIView());

                panel.add(new JPanel());

                panel.add(players.get(1).getGUIView());

            }else{

                Iterator<PlayerInterface> playerInterfaceIterator = getPlayers().iterator();

                while (playerInterfaceIterator.hasNext()){
                    panel.add(playerInterfaceIterator.next().getGUIView());
                }
            }

            return panel;
        }

    }

    public PlayersGame(String gameId, Player playersSelf) {
        super(gameId,playersSelf);
        initializeGameStates();
        this.playersSelf = playersSelf;
    }

    protected void acknowledgeTransaction(final Transfer t){
        //showTransferInGUI(t);
        new Thread(){
            public void run(){
                try{
                    Thread.sleep(2500);
                }catch(InterruptedException ie){

                }
                getPlayersSelf().sendMessageWithRole(
                        playersSelf.getAgentWithRole("investment_game", getGameId(), "coordinator"),
                        new ActMessage((t.getType()== Transfer.TYPE_A?"processed_transfer_A":"processed_transfer_B"),getPlayersSelf().getPlayersName()),
                        "player");
            }
        }.start();
    }

    public void showTransferInGUI(Transfer transfer){
        if (gui != null){

            try {
                gui.showTransaction(transfer,getCurrentRoundExchange());
            } catch (GameNotStartedYetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }
    }

    private void initializeGameStates(){

        gameStates.put(

                "waiting_0",

                new GameState<PlayersGame>("waiting_0",this){

                    @Override
                    public void onEnterState() {
                        super.onEnterState();
                    }

                    @Override
                    public void onExitState() {
                        game.playersSelf.sendMessageWithRole(
                                game.playersSelf.getAgentWithRole("investment_game", game.getGameId(), "coordinator"),
                                new ActMessage("ready_to_play",game.playersSelf.getPlayersName()),
                                "player");
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("game_starts")){
                            game.getPlayersSelf().getLogger().log(Level.INFO,"  --->  (0) PLAYER "+game.getPlayersSelf().getPlayersName()+message.getContent());
                            String[] playerInfos = message.getContent().split("##");
                            Pattern playerInfoPattern = Pattern.compile("<player>\\s+([^<>\\s]+)\\s+<has>\\s+(\\d+)\\s+<credits>\\s+<and_looks_like>\\s+(.+)");
                            for (int i=0;i<playerInfos.length;i++){
                                Matcher m = playerInfoPattern.matcher(playerInfos[i]);
                                if (m.matches()){
                                    game.getPlayersSelf().getLogger().log(Level.INFO,"  --->  PLAYER "+game.getPlayersSelf().getPlayersName()+" add model player "+m.group(1)+" / "+m.group(3));
                                    ModelPlayer player = new PlayersModelPlayer(game,m.group(1),m.group(3));
                                    //player.setCreditBalance(Double.parseDouble(m.group(2)));
                                    game.addPlayer(player);
                                    game.getPlayer(m.group(1)).setCreditBalance(Double.parseDouble(m.group(2)));
                                }
                            }

                            if (game.getPlayersSelf().hasGUI()){
                                game.getPlayersSelf().getFrame().getContentPane().add(game.getGUIView());
                                game.getPlayersSelf().getFrame().pack();
                            }

                            return gameStates.get("waiting_3");

                        }
                        return this;
                    }
                }
        );

        gameStates.put(

                "my_turn_A",

                new GameState<PlayersGame>("my_turn_A",this){

                    private String messageContent;

                    @Override
                    public void onEnterState() {
                        game.playersSelf.onMyTurnA();
                    }

                    @Override
                    public void onExitState() {
                        game.playersSelf.sendMessageWithRole(
                                game.playersSelf.getAgentWithRole("investment_game", game.getGameId(), "coordinator"),
                                new ActMessage("decide_on_transfer_A",messageContent),
                                "player");
                        game.getPlayersSelf().getLogger().log(Level.INFO,game.playersSelf.getPlayersName()+" exited state "+"my_turn_A");
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("decide_on_transfer_A")){

                            Transfer transfer = TransferFactory.transferFromMessage(message,game);

                            if (transfer instanceof Exchange.TransferA){

                                Exchange.TransferA transferA = (Exchange.TransferA)transfer;

                                try {
                                    game.commit(transferA);
                                    this.messageContent = message.getContent();
                                    return gameStates.get("waiting_1");
                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferAlreadyCommittedException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                        return this;
                    }
                }
        );

        gameStates.put(

                "my_turn_B",

                new GameState<PlayersGame>("my_turn_B",this){

                    private String messageContent;

                    @Override
                    public void onEnterState() {
                        game.playersSelf.onMyTurnB();
                    }

                    @Override
                    public void onExitState() {
                        game.playersSelf.sendMessageWithRole(
                                game.playersSelf.getAgentWithRole("investment_game", game.getGameId(), "coordinator"),
                                new ActMessage("decide_on_transfer_B",messageContent),
                                "player");
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("decide_on_transfer_B")){
                            Transfer transfer = TransferFactory.transferFromMessage(message,game);

                            if (transfer instanceof Exchange.TransferB){

                                Exchange.TransferB transferB = (Exchange.TransferB)transfer;

                                try {
                                    game.commit(transferB);
                                    this.messageContent = message.getContent();
                                    return gameStates.get("waiting_4");
                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferAlreadyCommittedException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                } catch (Exchange.PrimaryTransferNotCommittedYetException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                        return this;
                    }
                }
        );

        gameStates.put(

                "waiting_1",

                new GameState<PlayersGame>("waiting_1",this){
                    @Override
                    public void onEnterState() {
                        super.onEnterState();
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("info_transfer_A")){

                            Transfer transfer = TransferFactory.transferFromMessage(message,game);

                            if (transfer instanceof Exchange.TransferA){

                                Exchange.TransferA transferA = (Exchange.TransferA)transfer;

                                try {

                                    game.confirmExecute(transferA);

                                    showTransferInGUI(transferA);

                                    acknowledgeTransaction(transferA);

                                    return gameStates.get("waiting_2");

                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferNotCommittedException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                        return this;
                    }
                }

        );

        gameStates.put(

                "waiting_2",

                new GameState<PlayersGame>("waiting_2",this){

                    @Override
                    public void onEnterState() {
                        super.onEnterState();
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("info_transfer_B")){

                            Transfer transfer = TransferFactory.transferFromMessage(message,game);

                            if (transfer instanceof Exchange.TransferB){

                                Exchange.TransferB transferB = (Exchange.TransferB)transfer;

                                try {

                                    game.commit(transferB);

                                    showTransferInGUI(transferB);

                                    game.confirmExecute(transferB);

                                    acknowledgeTransaction(transferB);

                                    return gameStates.get("waiting_3");

                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferAlreadyCommittedException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                } catch (Exchange.PrimaryTransferNotCommittedYetException e) {
                                    e.printStackTrace();
                                } catch (GameIsOverException e) {
                                    e.printStackTrace();
                                } catch (RoundNotFinalizedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferNotCommittedException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                        return this;
                    }
                }

        );

        gameStates.put(

                "waiting_3",

                new GameState<PlayersGame>("waiting_3",this){

                    @Override
                    public void onEnterState() {
                        super.onEnterState();
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().startsWith("info_transfer_")){

                            Transfer transfer = TransferFactory.transferFromMessage(message,game);

                            if (transfer instanceof Exchange.TransferA){

                                Exchange.TransferA transferA = (Exchange.TransferA) transfer;

                                try {

                                    game.commit(transferA);

                                    showTransferInGUI(transferA);

                                    game.confirmExecute(transferA);

                                    acknowledgeTransaction(transferA);

                                    return gameStates.get("waiting_3");

                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferAlreadyCommittedException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferNotCommittedException e) {
                                    e.printStackTrace();
                                }

                            }

                            if (transfer instanceof Exchange.TransferB){

                                Exchange.TransferB transferB = (Exchange.TransferB) transfer;

                                try {

                                    game.commit(transferB);

                                    showTransferInGUI(transferB);

                                    game.confirmExecute(transferB);

                                    acknowledgeTransaction(transferB);

                                    return gameStates.get("waiting_3");

                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferAlreadyCommittedException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                } catch (Exchange.PrimaryTransferNotCommittedYetException e) {
                                    e.printStackTrace();
                                } catch (GameIsOverException e) {
                                    e.printStackTrace();
                                } catch (RoundNotFinalizedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferNotCommittedException e) {
                                    e.printStackTrace();
                                }

                            }

                        }

                        if (message.getAction().equals("your_turn_A")){
                            return gameStates.get("my_turn_A");
                        }
                        if (message.getAction().equals("your_turn_B")){
                            return gameStates.get("my_turn_B");
                        }
                        if (message.getAction().equals("game_over")){
                            return gameStates.get("game_over");
                        }
                        return this;
                    }
                }

        );

        gameStates.put(

                "waiting_4",

                new GameState<PlayersGame>("waiting_4",this){
                    @Override
                    public void onEnterState() {
                        super.onEnterState();
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("info_transfer_B")){

                            Transfer transfer = TransferFactory.transferFromMessage(message, game);

                            if (transfer instanceof Exchange.TransferB){

                                Exchange.TransferB transferB = (Exchange.TransferB)transfer;

                                try {

                                    showTransferInGUI(transferB);

                                    game.confirmExecute(transferB);

                                    acknowledgeTransaction(transferB);

                                    return gameStates.get("waiting_3");

                                } catch (GameNotStartedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.ConstraintViolationException e) {
                                    e.printStackTrace();
                                } catch (GameIsOverException e) {
                                    e.printStackTrace();
                                } catch (RoundNotFinalizedYetException e) {
                                    e.printStackTrace();
                                } catch (Exchange.TransferNotCommittedException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                        return this;
                    }
                }

        );

        gameStates.put(

                "game_over",

                new GameState<PlayersGame>("game_over",this){

                    @Override
                    public void onEnterState() {
                        try {
                            game.endGame();
                        } catch (GameNotStartedYetException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        return this;
                    }
                }

        );
    }

    public Player getPlayersSelf() {
        return playersSelf;
    }

    @Override
    protected void initialize() {
        setGameState(gameStates.get("waiting_0"));
    }

    public JPanel getGUIView(){

        if (gui == null){
            gui = new GUI();
        }

        return gui.getPanel();
    }

    public void enableModeSelectPlayer(boolean enable){
        Iterator<PlayerInterface> playerInterfaceIterator = getPlayers().iterator();
        while (playerInterfaceIterator.hasNext()){
            playerInterfaceIterator.next().setSelectable(enable);
        }
    }

    public void selectPlayer(PlayerInterface player){
        playersSelf.selectPlayer(player);
    }

}
