package investmentGame;

import madkit.kernel.Agent;
import madkit.message.ActMessage;
import sun.tools.jconsole.JConsole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
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

        private Set<PlayerSpecification> playerSpecificationSet = new HashSet<PlayerSpecification>();

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

        public void addPlayerSpecification(PlayerSpecification playerSpecification){
            playerSpecificationSet.add(playerSpecification);
        }

        public int getRounds() {
            return rounds;
        }

        public Set<PlayerSpecification> getPlayerSpecificationSet() {
            return playerSpecificationSet;
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

            JButton startButton = new JButton("Starte Spiel");
            //startButton.setEnabled(false);

            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //To change body of implemented methods use File | Settings | File Templates.
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

            JButton addComputerPlayerButton = new JButton("Neuer Computer-Spieler");

            addComputerPlayerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    final JDialog dialog = new JDialog(frame);

                    dialog.setBounds(frame.getBounds().x,frame.getBounds().y,300,300);

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
                            getGameSpecification().addPlayerSpecification(new GameSpecification.ComputerPlayerSpecification("COMPUTER-SPIELER","---"));
                            Vector<GameSpecification.PlayerSpecification> listspec = new Vector<GameSpecification.PlayerSpecification>();
                            Iterator<GameSpecification.PlayerSpecification> plIt = getGameSpecification().getPlayerSpecificationSet().iterator();
                            while (plIt.hasNext()){
                                listspec.add(plIt.next());
                            }
                            playersList.setListData(listspec);
                            dialog.dispose();
                        }
                    });

                    dialogButtonPane.add(cancelButton);
                    dialogButtonPane.add(okButton);

                    dialogPane.add(dialogButtonPane,BorderLayout.SOUTH);

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

        while ((message=(ActMessage)waitNextMessage())!=null){

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

        Coordinator coordinator = new Coordinator();

        launchAgent(coordinator);

        pause(1000);

        Iterator<GameSpecification.PlayerSpecification> playerSpecs = gameSpecification.getPlayerSpecificationSet().iterator();

        boolean first = true;

        while (playerSpecs.hasNext()){
            GameSpecification.PlayerSpecification spec = playerSpecs.next();

            if (spec instanceof GameSpecification.ComputerPlayerSpecification){

                ComputerPlayer computerPlayer = new ComputerPlayer(true);

                launchAgent(computerPlayer,first);

            }

            first=false;
        }

    }
}
