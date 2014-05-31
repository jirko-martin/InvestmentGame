package investmentGame.actor;

import investmentGame.Configuration;
import investmentGame.actor.game.player.strategy.ChooseAmountStrategy;
import investmentGame.actor.game.player.strategy.SelectOpponentStrategy;
import investmentGame.swing.ImagePreviewPanel;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.message.ActMessage;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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

            private ChooseAmountStrategy chooseAmountStrategy;
            private SelectOpponentStrategy selectOpponentStrategy;

            public ComputerPlayerSpecification(String name, String pictureFileName,ChooseAmountStrategy chooseAmountStrategy, SelectOpponentStrategy selectOpponentStrategy){
                super(name,pictureFileName);
                this.chooseAmountStrategy = chooseAmountStrategy;
                this.selectOpponentStrategy = selectOpponentStrategy;
            }

            public String toString(){
                return "Computer-Spieler "+getName();
            }

            public ChooseAmountStrategy getChooseAmountStrategy() {
                return chooseAmountStrategy;
            }

            public SelectOpponentStrategy getSelectOpponentStrategy() {
                return selectOpponentStrategy;
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
        File pictureFolder = new File(Configuration.picturePath);
        Component activeComponent;

        public GUI(JFrame frame,JPanel contentPane){
            this.frame = frame;
            this.contentPane = contentPane;
            infoPane = new JTextPane();
            contentPane.add(infoPane,BorderLayout.SOUTH);

            showMainMenu();
        }

        public File getPictureFolder() {
            return pictureFolder;
        }

        public void setPictureFolder(File pictureFolder) {
            this.pictureFolder = pictureFolder;
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

            final JPanel gameOptionPanel = new JPanel(new BorderLayout());

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

            JPanel delayPanelA = new JPanel(new GridLayout(1,2));

            delayPanelA.add(new JLabel("Delay ACK Turn A (ms)"));

            final JTextField delayInputA = new JTextField(""+Configuration.Timings.delayAcknowledgeInfoTransferMSecTurnA);

            delayInputA.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try{
                        int delay = Integer.parseInt(delayInputA.getText());
                        if (delay>0)
                            Configuration.Timings.delayAcknowledgeInfoTransferMSecTurnA = delay;
                        else
                            delayInputA.setText(""+Configuration.Timings.delayAcknowledgeInfoTransferMSecTurnA);
                    }catch (NumberFormatException nfe){
                        delayInputA.setText(""+Configuration.Timings.delayAcknowledgeInfoTransferMSecTurnA);
                    }
                }
            });

            delayPanelA.add(delayInputA);

            parameterPanel.add(delayPanelA);

            JPanel delayPanelB = new JPanel(new GridLayout(1,2));

            delayPanelB.add(new JLabel("Delay ACK Turn B (ms)"));

            final JTextField delayInputB = new JTextField(""+Configuration.Timings.delayAcknowledgeInfoTransferMSecTurnB);

            delayInputB.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try{
                        int delay = Integer.parseInt(delayInputB.getText());
                        if (delay>0)
                            Configuration.Timings.delayAcknowledgeInfoTransferMSecTurnB = delay;
                        else
                            delayInputB.setText(""+Configuration.Timings.delayAcknowledgeInfoTransferMSecTurnB);
                    }catch (NumberFormatException nfe){
                        delayInputB.setText(""+Configuration.Timings.delayAcknowledgeInfoTransferMSecTurnB);
                    }
                }
            });

            delayPanelB.add(delayInputB);

            parameterPanel.add(delayPanelB);

            JPanel showForMSecPanel = new JPanel(new GridLayout(1,2));

            showForMSecPanel.add(new JLabel("Show Transfer for (ms)"));

            final JTextField showForMSecInput = new JTextField(""+Configuration.Timings.showTransferInGUIForMSec);

            showForMSecInput.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try{
                        int delay = Integer.parseInt(showForMSecInput.getText());
                        if (delay>0)
                            Configuration.Timings.showTransferInGUIForMSec = delay;
                        else
                            showForMSecInput.setText(""+Configuration.Timings.showTransferInGUIForMSec);
                    }catch (NumberFormatException nfe){
                        showForMSecInput.setText(""+Configuration.Timings.showTransferInGUIForMSec);
                    }
                }
            });

            showForMSecPanel.add(showForMSecInput);

            parameterPanel.add(showForMSecPanel);


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
                            for (GameSpecification.PlayerSpecification playerSpecification : getGameSpecification().getPlayerSpecifications()) {
                                listspec.add(playerSpecification);
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

                File pictureFile;

                String name;

                ImageIcon picture;

                ChooseAmountStrategy chooseAmountStrategy;
                
                SelectOpponentStrategy selectOpponentStrategy;

                JPanel strategyConfigurationPanelChooseAmount;
                JPanel strategyConfigurationPanelSelectOpponent;

                public File getPictureFile() {
                    return pictureFile;
                }

                public void setPictureFile(File pictureFile) {
                    this.pictureFile = pictureFile;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public ChooseAmountStrategy getChooseAmountStrategy() {
                    return chooseAmountStrategy;
                }

                public SelectOpponentStrategy getSelectOpponentStrategy() {
                    return selectOpponentStrategy;
                }

                public void setChooseAmountStrategy(ChooseAmountStrategy chooseAmountStrategy) {
                    this.chooseAmountStrategy = chooseAmountStrategy;
                }

                public void setSelectOpponentStrategy(SelectOpponentStrategy selectOpponentStrategy) {
                    this.selectOpponentStrategy = selectOpponentStrategy;
                }

                @Override
                public void actionPerformed(ActionEvent e) {

                    final JDialog dialog = new JDialog(frame);

                    dialog.setTitle("Neuer Computer-Spieler");

                    dialog.setBounds(frame.getBounds().x,frame.getBounds().y,1115,820);

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

                            String picturePath = ((getPictureFile()!=null && getPictureFile().exists()) ? getPictureFile().getAbsolutePath() : null);

                            getGameSpecification().addPlayerSpecification(new GameSpecification.ComputerPlayerSpecification(getName(),
                                                                                                                            picturePath, 
                                                                                                                            getChooseAmountStrategy(), 
                                                                                                                            getSelectOpponentStrategy()));
                            Vector<GameSpecification.PlayerSpecification> listspec = new Vector<GameSpecification.PlayerSpecification>();
                            for (GameSpecification.PlayerSpecification playerSpecification : getGameSpecification().getPlayerSpecifications()) {
                                listspec.add(playerSpecification);
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
                    mainPane.setLayout(new BorderLayout());

                    JPanel picturePane = new JPanel(new BorderLayout());

                    final JPanel picturePreviewPane = new JPanel(new BorderLayout());
                    picturePreviewPane.setBackground(Color.WHITE);

                    final JLabel pictureLabel = new JLabel("");
                    picturePreviewPane.add(pictureLabel,BorderLayout.CENTER);

                    picturePane.add(picturePreviewPane,BorderLayout.CENTER);

                    picturePane.setBorder(new TitledBorder("Wie sieht der Spieler aus?"));

                    getLogger().log(Level.INFO," ... picture folder exists? "+(pictureFolder.exists()?"yes":"no"));

                    JButton choosePictureButton = new JButton("Bild auswählen");

                    choosePictureButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser chooser = new JFileChooser();
                            ImagePreviewPanel preview = new ImagePreviewPanel();
                            chooser.setAccessory(preview);
                            chooser.addPropertyChangeListener(preview);

                            if (getPictureFolder().exists() && getPictureFolder().isDirectory()){
                                chooser.setCurrentDirectory(getPictureFolder());
                            }

                            chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                @Override
                                public boolean accept(File f) {

                                    String name = f.getAbsolutePath();

                                    return ((name != null) &&
                                            name.toLowerCase().endsWith(".jpg") ||
                                            name.toLowerCase().endsWith(".jpeg") ||
                                            name.toLowerCase().endsWith(".gif") ||
                                            name.toLowerCase().endsWith(".png"));
                                }

                                @Override
                                public String getDescription() {
                                    return "Bilder";
                                }
                            });

                            int returnVal = chooser.showOpenDialog(gameOptionPanel);

                            if (returnVal == JFileChooser.APPROVE_OPTION){

                                File pictureFile = chooser.getSelectedFile();

                                ImageIcon imageRaw = new ImageIcon(pictureFile.getAbsolutePath());

                                Image scaledImage;

                                if (imageRaw.getIconHeight()>0){

                                    setPictureFile(pictureFile);
                                    setPictureFolder(pictureFile.getParentFile());

                                    if (imageRaw.getIconHeight()>imageRaw.getIconWidth()){

                                        scaledImage = imageRaw.getImage().getScaledInstance(-1,250,Image.SCALE_SMOOTH);

                                    }else{

                                        scaledImage = imageRaw.getImage().getScaledInstance(250,-1,Image.SCALE_SMOOTH);

                                    }

                                    picture = new ImageIcon(scaledImage);

                                    pictureLabel.setIcon(picture);
                                    pictureLabel.setHorizontalAlignment(JLabel.CENTER);
                                    pictureLabel.setVerticalAlignment(JLabel.CENTER);

                                    pictureLabel.repaint();

                                }

                            }

                        }
                    });

                    picturePane.add(choosePictureButton,BorderLayout.SOUTH);

                    picturePane.setPreferredSize(new Dimension(400,300));

                    JPanel leftPane = new JPanel(new BorderLayout());

                    JPanel namePane = new JPanel(new BorderLayout());

                    namePane.add(new JLabel("Name"), BorderLayout.WEST);

                    setName("COMPUTER_PLAYER_"+ComputerPlayer.instanceCounter++);

                    final JTextField nameInput = new JTextField(getName());

                    namePane.add(nameInput, BorderLayout.CENTER);

                    nameInput.addFocusListener(new FocusListener() {
                        @Override
                        public void focusGained(FocusEvent e) {
                            //To change body of implemented methods use File | Settings | File Templates.
                        }

                        @Override
                        public void focusLost(FocusEvent e) {
                            setName(nameInput.getText());
                        }
                    });

                    leftPane.add(namePane, BorderLayout.NORTH);
                    leftPane.add(picturePane, BorderLayout.CENTER);

                    mainPane.add(leftPane,BorderLayout.WEST);
                    
                    JTabbedPane strategyPane = new JTabbedPane(JTabbedPane.TOP);

                    final JPanel chooseAmountStrategyPane = new JPanel(new BorderLayout());

                    chooseAmountStrategyPane.setBorder(new TitledBorder("Strategie des Spielers (Betrag überweisen)"));

                    List<ChooseAmountStrategy> chooseAmountStrategies = new LinkedList<ChooseAmountStrategy>();

                    for (int i=0;i<Configuration.chooseAmountStrategies.length;i++){
                        try {
                            Object strategyCandidate = Configuration.chooseAmountStrategies[i].newInstance();
                            if (strategyCandidate instanceof ChooseAmountStrategy){
                                chooseAmountStrategies.add((ChooseAmountStrategy)strategyCandidate);
                            }
                        } catch (InstantiationException e1) {
                            e1.printStackTrace();
                            throw new RuntimeException(e1);
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                            throw new RuntimeException(e1);
                        }
                    }

                    final JComboBox<ChooseAmountStrategy> chooseAmountStrategySelectionBox = new JComboBox<ChooseAmountStrategy>(new Vector<ChooseAmountStrategy>(chooseAmountStrategies));

                    chooseAmountStrategySelectionBox.setSelectedIndex(0);

                    ChooseAmountStrategy chooseAmountStrategySelected = chooseAmountStrategySelectionBox.getItemAt(0);

                    setChooseAmountStrategy(chooseAmountStrategySelected);

                    if (chooseAmountStrategySelected.isConfigurable()){
                        strategyConfigurationPanelChooseAmount = chooseAmountStrategySelected.getConfigurationPanel();
                        chooseAmountStrategyPane.add(strategyConfigurationPanelChooseAmount,BorderLayout.CENTER);
                    }

                    chooseAmountStrategySelectionBox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            ChooseAmountStrategy chooseAmountStrategySelected = (ChooseAmountStrategy)chooseAmountStrategySelectionBox.getSelectedItem();

                            setChooseAmountStrategy(chooseAmountStrategySelected);

                            if (strategyConfigurationPanelChooseAmount != null){
                                chooseAmountStrategyPane.remove(strategyConfigurationPanelChooseAmount);
                                chooseAmountStrategyPane.validate();
                                chooseAmountStrategyPane.repaint();
                                strategyConfigurationPanelChooseAmount = null;
                            }

                            if (chooseAmountStrategySelected.isConfigurable()){
                                strategyConfigurationPanelChooseAmount = chooseAmountStrategySelected.getConfigurationPanel();
                                chooseAmountStrategyPane.add(strategyConfigurationPanelChooseAmount,BorderLayout.CENTER);
                                chooseAmountStrategyPane.validate();
                                chooseAmountStrategyPane.repaint();
                            }
                        }
                    });

                    chooseAmountStrategyPane.add(chooseAmountStrategySelectionBox,BorderLayout.NORTH);

                    strategyPane.add("Amount Strategy",chooseAmountStrategyPane);

                    //--

                    final JPanel selectOpponentStrategyPane = new JPanel(new BorderLayout());

                    selectOpponentStrategyPane.setBorder(new TitledBorder("Strategie des Spielers (Mitspieler auswählen)"));

                    List<SelectOpponentStrategy> selectOpponentStrategies = new LinkedList<SelectOpponentStrategy>();

                    for (int i=0;i<Configuration.selectOpponentStrategies.length;i++){
                        try {
                            Object strategyCandidate = Configuration.selectOpponentStrategies[i].newInstance();
                            if (strategyCandidate instanceof SelectOpponentStrategy){
                                selectOpponentStrategies.add((SelectOpponentStrategy)strategyCandidate);
                            }
                        } catch (InstantiationException e1) {
                            e1.printStackTrace();
                            throw new RuntimeException(e1);
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                            throw new RuntimeException(e1);
                        }
                    }

                    final JComboBox<SelectOpponentStrategy> selectOpponentStrategySelectionBox = new JComboBox<SelectOpponentStrategy>(new Vector<SelectOpponentStrategy>(selectOpponentStrategies));

                    selectOpponentStrategySelectionBox.setSelectedIndex(0);

                    SelectOpponentStrategy selectOpponentStrategySelected = selectOpponentStrategySelectionBox.getItemAt(0);

                    setSelectOpponentStrategy(selectOpponentStrategySelected);

                    if (selectOpponentStrategySelected.isConfigurable()){
                        strategyConfigurationPanelSelectOpponent = chooseAmountStrategySelected.getConfigurationPanel();
                        chooseAmountStrategyPane.add(strategyConfigurationPanelSelectOpponent,BorderLayout.CENTER);
                    }

                    selectOpponentStrategySelectionBox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            SelectOpponentStrategy selectOpponentStrategySelected = (SelectOpponentStrategy)selectOpponentStrategySelectionBox.getSelectedItem();

                            setSelectOpponentStrategy(selectOpponentStrategySelected);

                            if (strategyConfigurationPanelSelectOpponent != null){
                                selectOpponentStrategyPane.remove(strategyConfigurationPanelSelectOpponent);
                                selectOpponentStrategyPane.validate();
                                selectOpponentStrategyPane.repaint();
                                strategyConfigurationPanelSelectOpponent = null;
                            }

                            if (selectOpponentStrategySelected.isConfigurable()){
                                strategyConfigurationPanelSelectOpponent = selectOpponentStrategySelected.getConfigurationPanel();
                                selectOpponentStrategyPane.add(strategyConfigurationPanelSelectOpponent,BorderLayout.CENTER);
                                selectOpponentStrategyPane.validate();
                                selectOpponentStrategyPane.repaint();
                            }
                        }
                    });

                    selectOpponentStrategyPane.add(selectOpponentStrategySelectionBox,BorderLayout.NORTH);

                    strategyPane.add("Select Opponent Strategy",selectOpponentStrategyPane);
                    
                    mainPane.add(strategyPane,BorderLayout.CENTER);

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

        while (true){
            pause(60000);
        }

    }

    @Override
    protected void activate() {

        setLogLevel(Level.INFO);
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

                ChooseAmountStrategy chooseAmountStrategy = ((GameSpecification.ComputerPlayerSpecification) spec).getChooseAmountStrategy();

                SelectOpponentStrategy selectOpponentStrategy = ((GameSpecification.ComputerPlayerSpecification) spec).getSelectOpponentStrategy();
                
                ComputerPlayer computerPlayer = new ComputerPlayer(spec.getName(),spec.getPictureFileName(), chooseAmountStrategy, selectOpponentStrategy);

                chooseAmountStrategy.setPlayer(computerPlayer);

                selectOpponentStrategy.setPlayer(computerPlayer);

                launchAgent(computerPlayer, launchedHumanPlayers == 0 && first);

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

        ActMessage reply = (ActMessage)sendMessageWithRoleAndWaitForReply(coordinatorAddress,new ActMessage("request_game",requestGameSpec),"jeromes_assistant");

        if (reply.getAction().equals("game_initialized")){

            final String gameId = reply.getContent();

            //let players join the game
            Iterator<Player> playerIterator = players.iterator();

            while (playerIterator.hasNext()){

                final Player player = playerIterator.next();
                final boolean isPrimaryPlayer = player.equals(primaryPlayer);

                new Thread(){
                    @Override
                    public void run() {
                        player.joinGame(gameId,isPrimaryPlayer,player.getPicturePath());
                    }
                }.start();

            }
        }else{
            //TODO throw Exception
        }

    }
}
