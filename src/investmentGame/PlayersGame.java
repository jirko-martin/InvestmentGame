package investmentGame;

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
public class PlayersGame extends Game{

    private Player playersSelf;

    public final Map<String,GameState<PlayersGame>> gameStates = new HashMap<String, GameState<PlayersGame>>();

    private GUI gui;

    public class GUI{

        public class Panel extends JPanel{

            Transaction transaction;

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
                g.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
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

                g.drawString(""+credits,x2,y2);

            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (transaction!=null){
                    Rectangle r1 = transaction.getSender().getGUIView().getBounds();
                    Rectangle r2 = transaction.getRecipient().getGUIView().getBounds();
                    
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
                    
                    drawArrow(g,p1.x,p1.y,p2.x,p2.y,transaction.getType()==Transaction.TYPE_A?Color.GREEN:Color.ORANGE,transaction.getCreditsTransferred());

                    (new Thread(){
                        public void run(){
                            try{
                                Thread.sleep(1000);
                                showTransaction(null);
                            }catch (InterruptedException e){

                            }
                        }
                    }).start();
                }

            }

            public void showTransaction(Transaction t){
                transaction = t;
                this.repaint();

            }
        }

        private Panel panel;

        public void update(){

        }

        public void showTransaction(Transaction transaction){
            if (panel!=null)
                panel.showTransaction(transaction);
        }

        public JPanel getPanel(){
            panel = new Panel(new RadialLayout());

            panel.setBackground(Color.WHITE);

            Iterator<PlayerInterface> playerInterfaceIterator = getPlayers().iterator();

            while (playerInterfaceIterator.hasNext()){
                panel.add(playerInterfaceIterator.next().getGUIView());
            }

            return panel;
        }

    }

    public PlayersGame(String gameId, Player playersSelf) {
        super(gameId,playersSelf);
        initializeGameStates();
        this.playersSelf = playersSelf;
    }

    protected void acknowledgeTransaction(Transaction t){
        getPlayersSelf().pauseAWhile(2000);
        getPlayersSelf().sendMessageWithRole(
                playersSelf.getAgentWithRole("investment_game", getGameId(), "coordinator"),
                new ActMessage((t.getType()==Transaction.TYPE_A?"processed_transfer_A":"processed_transfer_B"),getPlayersSelf().getPlayersName()),
                "player");
    }

    private void initializeGameStates(){

        gameStates.put(

                "waiting_0",

                new GameState<PlayersGame>("waiting_0",this){

                    @Override
                    public void onEnterState() {
                        super.onEnterState();    //To change body of overridden methods use File | Settings | File Templates.
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
                            Pattern playerInfoPattern = Pattern.compile("<player>\\s+([^<>\\s]+)\\s+<has>\\s+(\\d+)\\s+<credits>");
                            for (int i=0;i<playerInfos.length;i++){
                                Matcher m = playerInfoPattern.matcher(playerInfos[i]);
                                if (m.matches()){
                                    game.getPlayersSelf().getLogger().log(Level.INFO,"  --->  PLAYER "+game.getPlayersSelf().getPlayersName()+" add model player "+m.group(1));
                                    ModelPlayer player = new ModelPlayer(m.group(1),null);
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
                            this.messageContent = message.getContent();
                            return gameStates.get("waiting_1");
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
                            this.messageContent = message.getContent();
                            return gameStates.get("waiting_4");
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
                        super.onEnterState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("info_transfer_A")){

                            Transaction transaction= game.transfer(message);

                            acknowledgeTransaction(transaction);

                            if (transaction != null){
                                game.playerAtTurnA = transaction.getSender();

                                agent.getLogger().log(Level.INFO,"  *** player's "+game.getPlayersSelf().getPlayersName()+" playerAtTurnA is now "+game.playerAtTurnA.getPlayersName());

                                return gameStates.get("waiting_2");
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
                        super.onEnterState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("info_transfer_B")){

                            Transaction t = game.transfer(message);

                            acknowledgeTransaction(t);

                            return gameStates.get("waiting_3");

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
                        super.onEnterState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().startsWith("info_transfer_")){

                            Transaction t = game.transfer(message);

                            acknowledgeTransaction(t);

                            return gameStates.get("waiting_3");

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
                        super.onEnterState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        if (message.getAction().equals("info_transfer_B")){

                            Transaction t = game.transfer(message);

                            acknowledgeTransaction(t);

                            return gameStates.get("waiting_3");

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
                        super.onEnterState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onExitState() {
                        super.onExitState();    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    public GameState processMessageEvent(ActMessage message) {
                        return super.processMessageEvent(message);    //To change body of overridden methods use File | Settings | File Templates.
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

    @Override
    public Transaction transfer(ActMessage message) {
        Transaction t = super.transfer(message);
        if (gui != null)
            gui.showTransaction(t);
        return t;
    }
}
