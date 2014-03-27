package investmentGame.actor;

import investmentGame.Configuration;
import investmentGame.actor.Player;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.message.ActMessage;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 24/03/14
 * Time: 20:27
 * To change this template use File | Settings | File Templates.
 */
public class JeromesAssistant extends Agent{

    private GUI gui;

    private GameSpecification gameSpecification;

    public static class GameSpecification{

        private int rounds = 20;

        private List<PlayerSpecification> playerSpecifications = new ArrayList<PlayerSpecification>();

        public static abstract class PlayerSpecification{

            private String pictureFileName;
            private String name;

            public PlayerSpecification(String name, String pictureFileName){
                this.name = name;
                this.pictureFileName = pictureFileName;
            }

            public String getPictureFileName() {
                return pictureFileName;
            }

            public String getName() {
                return name;
            }
        }

        public static class ComputerPlayerSpecification extends PlayerSpecification{

            public ComputerPlayerSpecification(String name, String pictureFileName){
                super(name,pictureFileName);
            }

            public String toString(){
                return "Computer-Spieler "+getName();
            }

        }

        public static class HumanPlayerSpecification extends PlayerSpecification{

            public HumanPlayerSpecification(String name) {
                super(name,null);
            }

            public String toString(){
                return "Humaner Spieler "+getName();
            }
        }

        public void addPlayerSpecification(PlayerSpecification playerSpecification){
            playerSpecifications.add(playerSpecification);
        }

        public int getRounds() {
            return rounds;
        }

        public List<PlayerSpecification> getPlayerSpecifications() {
            return playerSpecifications;
        }

        public void setRounds(int rounds) {
            this.rounds = rounds;
        }

    }


    public class GUI{

        JFrame frame;
        JPanel contentPane;
        JTextPane infoPane;
        JPanel optionPanel;
        JList<GameSpecification.PlayerSpecification> playersList;
        JButton startButton;

        Component activeComponent;

        public GUI(JFrame frame,JPanel contentPane){
            this.frame = frame;
            this.contentPane = contentPane;
            infoPane = new JTextPane();
            contentPane.add(infoPane,BorderLayout.SOUTH);

            showMainMenu();
        }

        private void setActiveComponent(final Component component){

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (activeComponent!=null)
                        contentPane.remove(activeComponent);
                    activeComponent = component;
                    contentPane.add(activeComponent,BorderLayout.CENTER);
                    contentPane.updateUI();
                }
            });

        }

        private void showMainMenu(){

            if (optionPanel==null){
                optionPanel = new JPanel();
                optionPanel.setLayout(new BoxLayout(optionPanel,BoxLayout.X_AXIS));

                JButton newGame = new JButton("Neues Spiel");
                newGame.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showNewGameOptions();
                    }
                });

                optionPanel.add(newGame);

            }

            setActiveComponent(optionPanel);

        }

        private void showNewGameOptions(){

            createNewGameSpecification();

            JPanel gameOptionPanel = new JPanel(new BorderLayout());

            JPanel mainOptions = new JPanel(new BorderLayout());

            startButton = new JButton("Starte Spiel");
            startButton.setEnabled(false);

            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread() {
                        @Override
                        public void run() {
                            startGame();
                        }
                    }.start();
                    setActiveComponent(optionPanel);
                }
            });

            mainOptions.add(startButton,BorderLayout.EAST);

            JButton cancelButton = new JButton("Abbrechen");

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setActiveComponent(optionPanel);
                }
            });

            mainOptions.add(cancelButton,BorderLayout.WEST);

            gameOptionPanel.add(mainOptions,BorderLayout.SOUTH);

            JPanel parameterPanel = new JPanel();
            parameterPanel.setLayout(new BoxLayout(parameterPanel,BoxLayout.Y_AXIS));

            JPanel roundsPanel = new JPanel(new GridLayout(1,2));

            roundsPanel.add(new JLabel("Anzahl Runden"));

            final JTextField roundsInput = new JTextField(""+getGameSpecification().getRounds());

            roundsInput.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try{
                        int rounds = Integer.parseInt(roundsInput.getText());
                        if (rounds>0)
                            getGameSpecification().setRounds(rounds);
                        else
                            roundsInput.setText(""+getGameSpecification().getRounds());
                    }catch (NumberFormatException nfe){
                        roundsInput.setText(""+getGameSpecification().getRounds());
                    }
                }
            });

            roundsPanel.add(roundsInput);

            parameterPanel.add(roundsPanel);

            JPanel playersPanel = new JPanel(new BorderLayout());

            JPanel playersPanelButtons = new JPanel(new GridLayout(1,2));

            JButton addHumanPlayerButton = new JButton("Neuer Humaner Spieler");

            addHumanPlayerButton.addActionListener(new ActionListener() {

                JTextField nameInputField;

                @Override
                public void actionPerformed(ActionEvent e) {
                    final JDialog dialog = new JDialog(frame);

                    dialog.setTitle("Neuer Humaner Spieler");

                    dialog.setBounds(frame.getBounds().x,frame.getBounds().y,300,200);

                    JPanel dialogPane = new JPanel(new BorderLayout());

                    JPanel dialogButtonPane = new JPanel(new GridLayout(1,2));

                    final JButton okButton = new JButton("OK");

                    JButton cancelButton = new JButton("Abbrechen");

                    cancelButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dialog.dispose();
                        }
                    });

                    okButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            getGameSpecification().addPlayerSpecification(new GameSpecification.HumanPlayerSpecification(nameInputField.getText().trim()));
                            Vector<GameSpecification.PlayerSpecification> listspec = new Vector<GameSpecification.PlayerSpecification>();
                            Iterator<GameSpecification.PlayerSpecification> plIt = getGameSpecification().getPlayerSpecifications().iterator();
                            while (plIt.hasNext()){
                                listspec.add(plIt.next());
                            }
                            playersList.setListData(listspec);
                            if (listspec.size()>=2){
                                startButton.setEnabled(true);
                            }
                            dialog.dispose();
                        }
                    });

                    okButton.setEnabled(true);

                    dialogButtonPane.add(cancelButton);
                    dialogButtonPane.add(okButton);

                    dialogPane.add(dialogButtonPane,BorderLayout.SOUTH);

                    JPanel mainPane = new JPanel();
                    mainPane.setLayout(new BoxLayout(mainPane,BoxLayout.Y_AXIS));

                    JPanel namePane = new JPanel(new BorderLayout());

                    JLabel nameLabel = new JLabel("Name des Spielers");

                    namePane.add(nameLabel,BorderLayout.WEST);

                    nameInputField = new JTextField("Heinrich-Egon-Rüdiger");

                    nameInputField.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyTyped(KeyEvent e) {
                            okButton.setEnabled(nameInputField.getText().trim().length()>=2);
                        }
                    });

                    namePane.add(nameInputField,BorderLayout.EAST);

                    namePane.setMaximumSize(new Dimension(300, 40));

                    mainPane.add(namePane);

                    dialogPane.add(mainPane,BorderLayout.CENTER);

                    dialog.setContentPane(dialogPane);

                    dialog.show();


                }
            });

            JButton addComputerPlayerButton = new JButton("Neuer Computer-Spieler");

            addComputerPlayerButton.addActionListener(new ActionListener() {

                JList<File> pictureList;

                @Override
                public void actionPerformed(ActionEvent e) {

                    final JDialog dialog = new JDialog(frame);

                    dialog.setTitle("Neuer Computer-Spieler");

                    dialog.setBounds(frame.getBounds().x,frame.getBounds().y,300,500);

                    JPanel dialogPane = new JPanel(new BorderLayout());

                    JPanel dialogButtonPane = new JPanel(new GridLayout(1,2));

                    JButton okButton = new JButton("OK");

                    JButton cancelButton = new JButton("Abbrechen");

                    cancelButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dialog.dispose();
                        }
                    });

                    okButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            String picturePath = pictureList.getSelectedValue().getAbsolutePath();

                            getGameSpecification().addPlayerSpecification(new GameSpecification.ComputerPlayerSpecification("COMPUTER-SPIELER", picturePath));
                            Vector<GameSpecification.PlayerSpecification> listspec = new Vector<GameSpecification.PlayerSpecification>();
                            Iterator<GameSpecification.PlayerSpecification> plIt = getGameSpecification().getPlayerSpecifications().iterator();
                            while (plIt.hasNext()){
                                listspec.add(plIt.next());
                            }
                            playersList.setListData(listspec);
                            if (listspec.size()>=2){
                                startButton.setEnabled(true);
                            }
                            dialog.dispose();
                        }
                    });

                    dialogButtonPane.add(cancelButton);
                    dialogButtonPane.add(okButton);

                    dialogPane.add(dialogButtonPane,BorderLayout.SOUTH);

                    JPanel mainPane = new JPanel();
                    mainPane.setLayout(new BoxLayout(mainPane,BoxLayout.Y_AXIS));

                    JPanel picturePane = new JPanel(new BorderLayout());

                    picturePane.setBorder(new TitledBorder("Wie sieht der Spieler aus?"));

                    File pictureFolder = new File(Configuration.picturePath);

                    getLogger().log(Level.INFO," ... picture folder exists? "+(pictureFolder.exists()?"yes":"no"));

                    pictureList = new JList<File>(pictureFolder.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return (new ImageIcon(pathname.getAbsolutePath()).getImage() != null); //TODO check whether this works
                        }
                    }));

                    class PictureCellRenderer extends JLabel implements ListCellRenderer {
                        public PictureCellRenderer() {
                            setOpaque(true);
                        }

                        public Component getListCellRendererComponent(JList list,
                                                                      Object value,
                                                                      int index,
                                                                      boolean isSelected,
                                                                      boolean cellHasFocus) {

                            File file = (File)value;

                            setText(file.getName());

                            ImageIcon icon = new ImageIcon(file.getAbsolutePath());

                            Image image = icon.getImage();

                            if (icon.getIconHeight()>icon.getIconWidth()){
                                setIcon(new ImageIcon(image.getScaledInstance(-1,110,Image.SCALE_SMOOTH)));
                            }else{
                                setIcon(new ImageIcon(image.getScaledInstance(110,-1,Image.SCALE_SMOOTH)));
                            }

                            Color background;
                            Color foreground;

                            // check if this cell represents the current DnD drop location
                            JList.DropLocation dropLocation = list.getDropLocation();
                            if (dropLocation != null
                                    && !dropLocation.isInsert()
                                    && dropLocation.getIndex() == index) {

                                background = Color.BLUE;
                                foreground = Color.WHITE;

                                // check if this cell is selected
                            } else if (isSelected) {
                                background = new Color(151, 222, 212);
                                foreground = Color.WHITE;

                                // unselected, and not the DnD drop location
                            } else {
                                background = Color.WHITE;
                                foreground = Color.BLACK;
                            };

                            setBackground(background);
                            setForeground(foreground);

                            return this;
                        }
                    }

                    pictureList.setCellRenderer(new PictureCellRenderer());

                    pictureList.setFixedCellHeight(120);

                    JScrollPane pictureListScrollPane = new JScrollPane(pictureList);

                    picturePane.add(pictureListScrollPane,BorderLayout.CENTER);

                    picturePane.setPreferredSize(new Dimension(400,300));

                    mainPane.add(picturePane);

                    JPanel strategyPane = new JPanel(new BorderLayout());

                    strategyPane.setBorder(new TitledBorder("Strategie des Spielers"));

                    mainPane.add(strategyPane);

                    dialogPane.add(mainPane,BorderLayout.CENTER);

                    dialog.setContentPane(dialogPane);

                    dialog.show();


                }
            });


            playersPanelButtons.add(addHumanPlayerButton);
            playersPanelButtons.add(addComputerPlayerButton);

            playersPanel.add(playersPanelButtons,BorderLayout.SOUTH);

            playersList = new JList<GameSpecification.PlayerSpecification>();

            playersList.setPreferredSize(new Dimension(400,400));

            playersPanel.add(playersList,BorderLayout.CENTER);

            parameterPanel.add(playersPanel);

            gameOptionPanel.add(parameterPanel,BorderLayout.CENTER);


            setActiveComponent(gameOptionPanel);

        }

    }

    public JeromesAssistant(){

        setName("Jérômes Assistent");

    }

    protected void createNewGameSpecification(){
        gameSpecification = new GameSpecification();
    }

    public GameSpecification getGameSpecification() {
        return gameSpecification;
    }

    @Override
    protected void live() {

        ActMessage message;

//        while ((message=(ActMessage)waitNextMessage())!=null){
//
//        }
        while (true){
            pause(10000);
        }

    }

    @Override
    protected void activate() {

        setLogLevel(Level.FINEST);
        createGroupIfAbsent("investment_game", "tout_le_monde");
        requestRole("investment_game","tout_le_monde","jeromes_assistant");

        pause(500);
    }

    @Override
    public void setupFrame(JFrame frame) {

        frame.setPreferredSize(new Dimension(400,600));

        JPanel contentPane = new JPanel(new BorderLayout());

        frame.getContentPane().add(contentPane);

        gui = new GUI(frame,contentPane);

    }

    public void startGame(){

        //launch coordinator if none found in group tout_le_monde

        AgentAddress coordinatorAddressPre = getAgentWithRole("investment_game", "tout_le_monde", "coordinator");

        if (coordinatorAddressPre==null){
            Coordinator coordinator = new Coordinator();

            launchAgent(coordinator);

            pause(1000);
        }

        Set<Player> players = new HashSet<Player>();
        Player primaryPlayer = null;

        //now launch all human player avatar agents, each of them will be initialized as agent having a GUI frame
        Iterator<GameSpecification.PlayerSpecification> playerSpecs = gameSpecification.getPlayerSpecifications().iterator();

        int launchedHumanPlayers = 0;

        while (playerSpecs.hasNext()){
            GameSpecification.PlayerSpecification spec = playerSpecs.next();

            if (spec instanceof GameSpecification.HumanPlayerSpecification){

                HumanPlayerAvatar humanPlayerAvatar = new HumanPlayerAvatar(spec.getName());

                launchAgent(humanPlayerAvatar,true);

                players.add(humanPlayerAvatar);

                launchedHumanPlayers++;

                if (null == primaryPlayer){
                    primaryPlayer=humanPlayerAvatar;
                }

            }
        }

        //launch computer player agents. If no human players have been launched, the first computer player will display a GUI frame.
        playerSpecs = gameSpecification.getPlayerSpecifications().iterator();

        boolean first = true;

        while (playerSpecs.hasNext()){
            GameSpecification.PlayerSpecification spec = playerSpecs.next();

            if (spec instanceof GameSpecification.ComputerPlayerSpecification){

                ComputerPlayer computerPlayer = new ComputerPlayer(spec.getPictureFileName());

                launchAgent(computerPlayer,launchedHumanPlayers==0 && first);

                players.add(computerPlayer);

                if (null == primaryPlayer){
                    primaryPlayer=computerPlayer;
                }

                first = false;

            }
        }

        //request new game from coordinator
        AgentAddress coordinatorAddress;

        while((coordinatorAddress = getAgentWithRole("investment_game","tout_le_monde","coordinator"))==null){
            pause(750);
        }

        String requestGameSpec = "<new_game> <for> "+gameSpecification.getPlayerSpecifications().size()+" <players> <having> "+gameSpecification.getRounds()+" <rounds> <invite> 0 <computer_players>";

        getLogger().log(Level.INFO,"pos0");

        ActMessage reply = (ActMessage)sendMessageWithRoleAndWaitForReply(coordinatorAddress,new ActMessage("request_game",requestGameSpec),"jeromes_assistant");

        getLogger().log(Level.INFO,"pos1");

        if (reply.getAction().equals("game_initialized")){

            getLogger().log(Level.INFO,"pos2");

            final String gameId = reply.getContent();

            //let players join the game
            Iterator<Player> playerIterator = players.iterator();

            while (playerIterator.hasNext()){

                getLogger().log(Level.INFO,"pos3");

                final Player player = playerIterator.next();
                final boolean isPrimaryPlayer = player.equals(primaryPlayer);

                new Thread(){
                    @Override
                    public void run() {
                        player.joinGame(gameId,isPrimaryPlayer,player.getPicturePath());
                    }
                }.start();

            }
            getLogger().log(Level.INFO,"pos4");

        }else{
            //TODO throw Exception
        }

    }
}
