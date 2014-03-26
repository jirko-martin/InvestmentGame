package investmentGame;

import madkit.message.ActMessage;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 25/03/14
 * Time: 11:20
 * To change this template use File | Settings | File Templates.
 */
public class HumanPlayerAvatar extends Player{

    private GUI gui;

    private class GUI{

        private static final int IN_TURN_A = 1;
        private static final int IN_TURN_B = 2;

        JLabel playersName;
        JLabel creditBalance;

        JPanel panel;

        JPanel controlPanel,controlPanelA,controlPanelB,controlPanelC;

        private int inTurn = 0;

        public GUI(){
            playersName = getPicturePath()!=null ? new JLabel(getPlayersName(),new ImageIcon(getScaledPicture(150)),JLabel.LEFT) : new JLabel(getPlayersName());
            creditBalance = new JLabel(""+getCreditBalance());
            creditBalance.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 20));
            creditBalance.setForeground(Color.BLUE);
        }

        public void update(){
            creditBalance.setText(""+getCreditBalance());
        }

        public JPanel getPanel(){
            if (panel==null){

                panel = new JPanel(new BorderLayout());

                panel.setBackground(Color.WHITE);

                panel.setBorder(new LineBorder(new Color(15, 255, 26), 5));

                panel.add(playersName,BorderLayout.NORTH);

                JPanel accountPanel = new JPanel(new BorderLayout());
                creditBalance.setBackground(Color.WHITE);
                accountPanel.setBackground(Color.WHITE);

                accountPanel.add(creditBalance,BorderLayout.CENTER);

                panel.add(accountPanel,BorderLayout.SOUTH);

                controlPanel = new JPanel(new BorderLayout());

                controlPanel.setPreferredSize(new Dimension(210,120));

                controlPanel.setBackground(Color.WHITE);

                panel.add(controlPanel,BorderLayout.CENTER);
            }

            return panel;
        }

        public void onMyTurnA(){

            inTurn = IN_TURN_A;

            controlPanelA = new JPanel(new BorderLayout());

            controlPanel.add(controlPanelA,BorderLayout.CENTER);

            JLabel questionLabel = new JLabel("Wähle bitte einen anderen Spieler aus!");

            controlPanelA.add(questionLabel, BorderLayout.CENTER);

            controlPanel.updateUI();

            game.enableModeSelectPlayer(true);
        }

        public void onMyTurnA2ndStage(final PlayerInterface playerSelected){

            game.enableModeSelectPlayer(false);

            controlPanel.remove(controlPanelA);

            controlPanel.updateUI();

            controlPanel.revalidate();

            controlPanelB = new JPanel(new BorderLayout());

            JTextArea question = new JTextArea("Wie viel möchtest Du an Spieler \n"+playerSelected.getPlayersName()+" \nüberweisen?",3,23);
            question.setLineWrap(true);

            controlPanelB.add(question,BorderLayout.NORTH);

            JPanel inputPanel = new JPanel(new BorderLayout());

            final JTextField amountInput = new JTextField("0",7);

            inputPanel.add(amountInput,BorderLayout.WEST);

            JButton okButton = new JButton("OK");

            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        final int value = Integer.parseInt(amountInput.getText().trim());
                        if (value>=0){
                            new Thread() {
                                @Override
                                public void run() {
                                    controlPanel.remove(controlPanelB);
                                    controlPanel.updateUI();
                                    panel.revalidate();
                                    panel.updateUI();

                                    decideOnTransfer(new Transaction(Transaction.TYPE_A, HumanPlayerAvatar.this, playerSelected, value));
                                }
                            }.start();
                            //pause(1000);
                            return;
                        }
                    }catch (NumberFormatException nfe){

                    }
                    JOptionPane.showMessageDialog(controlPanelB,"Ungültige Eingabe. Versuche es noch einmal!","Eingabefehler",JOptionPane.WARNING_MESSAGE);

                    amountInput.setText("0");

                }
            });

            inputPanel.add(okButton,BorderLayout.EAST);

            controlPanelB.add(inputPanel,BorderLayout.SOUTH);

            controlPanel.add(controlPanelB,BorderLayout.CENTER);

            controlPanel.updateUI();

            controlPanel.revalidate();

            amountInput.grabFocus();

            getLogger().log(Level.INFO,"onMyTurnA2ndStage ... panel bounds : "+panel.getBounds());

        }

        public void onMyTurnB(){

            inTurn = IN_TURN_B;

            controlPanelC = new JPanel(new BorderLayout());

            controlPanel.add(controlPanelC,BorderLayout.CENTER);

            JTextArea question = new JTextArea("Wie viel möchtest Du \nzurück überweisen?",3,23);
            question.setLineWrap(true);

            controlPanelC.add(question,BorderLayout.NORTH);

            JPanel inputPanel = new JPanel(new BorderLayout());

            final JTextField amountInput = new JTextField("0",7);

            inputPanel.add(amountInput,BorderLayout.WEST);

            JButton okButton = new JButton("OK");

            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        final int value = Integer.parseInt(amountInput.getText().trim());
                        if (value>=0){
                            new Thread() {
                                @Override
                                public void run() {
                                    controlPanel.remove(controlPanelC);
                                    controlPanel.updateUI();
                                    panel.revalidate();
                                    panel.updateUI();

                                    decideOnTransfer(new Transaction(Transaction.TYPE_B, HumanPlayerAvatar.this, game.playerAtTurnA, value));
                                }
                            }.start();
                            //pause(1000);
                            return;
                        }
                    }catch (NumberFormatException nfe){

                    }
                    JOptionPane.showMessageDialog(controlPanelB,"Ungültige Eingabe. Versuche es noch einmal!","Eingabefehler",JOptionPane.WARNING_MESSAGE);

                    amountInput.setText("0");

                }
            });

            inputPanel.add(okButton,BorderLayout.EAST);

            controlPanelC.add(inputPanel,BorderLayout.SOUTH);

            controlPanel.updateUI();

            amountInput.grabFocus();

        }

        public void selectPlayer(PlayerInterface player){
            if (IN_TURN_A == inTurn){
                onMyTurnA2ndStage(player);
            }
        }

    }


    public HumanPlayerAvatar(String playersName){
        super(playersName,null);
    }

    @Override
    public void setCreditBalance(double balance) {

        super.setCreditBalance(balance);

        if (gui != null){
            System.err.println("human player "+getPlayersName()+" has gui and will update view to new credit balance "+getCreditBalance()+" now");
            gui.update();
        }else{
            System.err.println("human player "+getPlayersName()+" has NO (!) gui and will not update view to new credit balance "+getCreditBalance()+" now");
        }
    }

    @Override
    public JPanel getGUIView() {

        if (gui == null){
            gui = new GUI();
        }

        return gui.getPanel();

    }

    @Override
    public void onMyTurnA() {
        if (gui!=null){
            gui.onMyTurnA();
        }
    }

    @Override
    public void onMyTurnB() {
        if (gui!=null){
            gui.onMyTurnB();
        }
    }

    @Override
    public void selectPlayer(PlayerInterface player) {
        if (gui != null){
            gui.selectPlayer(player);
        }
    }

    public void decideOnTransfer(Transaction transaction){
        String transferDesc = "<player> "+getPlayersName()+" <decides_to_transfer> "+((int)transaction.getCreditsTransferred())+" <credits> <to> <player> "+transaction.getRecipient().getPlayersName();
        receiveMessage(new ActMessage("decide_on_transfer_"+(transaction.getType()==Transaction.TYPE_A?"A":"B"),transferDesc));
    }

    @Override
    public Transaction transferA(PlayerInterface recipient, double credits) {
        Transaction t = super.transferA(recipient, credits);
        if (gui != null){
            System.err.println("human player "+getPlayersName()+" has gui and will update view to new credit balance "+getCreditBalance()+" now");
            gui.update();
        }
        return t;
    }

    @Override
    public Transaction transferB(PlayerInterface recipient, double credits) {
        Transaction t = super.transferB(recipient, credits);
        if (gui != null){
            System.err.println("human player "+getPlayersName()+" has gui and will update view to new credit balance "+getCreditBalance()+" now");
            gui.update();
        }
        return t;
    }
}
