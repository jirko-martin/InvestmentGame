package investmentGame.actor;

import investmentGame.Configuration;
import investmentGame.actor.game.Exchange;
import investmentGame.actor.game.Game;
import investmentGame.actor.game.PlayerInterface;
import investmentGame.actor.game.Transfer;
import madkit.message.ActMessage;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 25/03/14
 * Time: 11:20
 * To change this template use File | Settings | File Templates.
 */
public class HumanPlayerAvatar extends Player {

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

            final JTextField amountInput = new JTextField("",7);
            amountInput.setText(""+(int)(getCreditBalance()/2));

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

                                    try {
                                        game.simulateCommit(new Exchange.TransferA(HumanPlayerAvatar.this,playerSelected,value,Configuration.transferAMultiplier));
                                    } catch (Game.GameNotStartedYetException e1) {
                                        e1.printStackTrace();
                                        throw new RuntimeException(e1);
                                    } catch (Exchange.TransferAlreadyCommittedException e1) {
                                        e1.printStackTrace();
                                        throw new RuntimeException(e1);
                                    } catch(Exchange.InsufficientCreditBalance e1){
                                        JOptionPane.showMessageDialog(panel,"Du kannst leider nicht "+(int)e1.getAmountAttemptedToTransfer()+" Punkte an "+playerSelected.getPlayersName()+" überweisen, da Du nur "+(int)e1.getBalance()+" hast.","Eingabefehler",JOptionPane.WARNING_MESSAGE);
                                        amountInput.setText(""+(int)(getCreditBalance()/2));
                                        return;
                                    }catch (Exchange.ConstraintViolationException e1) {
                                        e1.printStackTrace();
                                        throw new RuntimeException(e1);
                                    } catch (Transfer.InvalidTransferException e1) {
                                        e1.printStackTrace();
                                        throw new RuntimeException(e1);
                                    }

                                    controlPanel.remove(controlPanelB);
                                    controlPanel.updateUI();
                                    panel.revalidate();
                                    panel.updateUI();

                                    try {
                                        decideOnTransfer(/*new Transfer(Transfer.TYPE_A, HumanPlayerAvatar.this, playerSelected, value)*/new Exchange.TransferA(HumanPlayerAvatar.this,playerSelected,value, Configuration.transferAMultiplier));
                                    } catch (Transfer.InvalidTransferException e1) {
                                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }
                                }
                            }.start();
                            //pause(1000);
                            return;
                        }else{
                            JOptionPane.showMessageDialog(panel,"Bitte gib eine positive Zahl ein!","Eingabefehler",JOptionPane.WARNING_MESSAGE);
                            amountInput.setText(""+(int)(getCreditBalance()/2));
                        }
                    }catch (NumberFormatException nfe){
                        JOptionPane.showMessageDialog(panel,"Bitte gib eine Zahl ein!","Eingabefehler",JOptionPane.WARNING_MESSAGE);
                        amountInput.setText(""+(int)(getCreditBalance()/2));
                    }
                    //JOptionPane.showMessageDialog(controlPanelB,"Ungültige Eingabe. Versuche es noch einmal!","Eingabefehler",JOptionPane.WARNING_MESSAGE);

                    //amountInput.setText("0");

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

                                    try {
                                        PlayerInterface sender = game.getCurrentRoundExchange().getTransferA().getSender();

                                        try {
                                            game.simulateCommit(new Exchange.TransferB(HumanPlayerAvatar.this,sender,value));
                                        } catch (Game.GameNotStartedYetException e1) {
                                            e1.printStackTrace();
                                            throw new RuntimeException(e1);
                                        } catch (Exchange.TransferAlreadyCommittedException e1) {
                                            e1.printStackTrace();
                                            throw new RuntimeException(e1);
                                        } catch(Exchange.InsufficientCreditBalance e1){
                                            JOptionPane.showMessageDialog(panel,"Du kannst leider nicht "+(int)e1.getAmountAttemptedToTransfer()+" Punkte zurück überweisen, da Du nur "+(int)e1.getBalance()+" hast.","Geht nicht",JOptionPane.WARNING_MESSAGE);
                                            amountInput.setText(""+(int)(e1.getBalance()/2));
                                            return;
                                        } catch(Exchange.AmountBGreaterThanAmountReceivedInTransferA e1){
                                            JOptionPane.showMessageDialog(panel,"Du kannst maximal "+e1.getAmountReceivedTransferA()+" Punkte zurück überweisen (so viel wie Du gerade von "+sender.getPlayersName()+" bekommen hast)","Geht nicht",JOptionPane.WARNING_MESSAGE);
                                            amountInput.setText(""+(int)(e1.getAmountReceivedTransferA()/2));
                                            return;
                                        }catch (Exchange.ConstraintViolationException e1) {
                                            e1.printStackTrace();
                                            throw new RuntimeException(e1);
                                        } catch (Exchange.PrimaryTransferNotCommittedYetException e1) {
                                            e1.printStackTrace();
                                            throw new RuntimeException(e1);
                                        } catch (Transfer.InvalidTransferException e1) {
                                            e1.printStackTrace();
                                            throw new RuntimeException(e1);
                                        }

                                    } catch (Exchange.TransferNotCommittedException e1) {
                                        e1.printStackTrace();
                                        throw new RuntimeException(e1);
                                    } catch (Game.GameNotStartedYetException e1) {
                                        e1.printStackTrace();
                                        throw new RuntimeException(e1);
                                    }

                                    controlPanel.remove(controlPanelC);
                                    controlPanel.updateUI();
                                    panel.revalidate();
                                    panel.updateUI();

                                    try {
                                        decideOnTransfer(/*new Transfer(Transfer.TYPE_B, HumanPlayerAvatar.this, game.getPlayerAtTurnA(), value)*/new Exchange.TransferB(HumanPlayerAvatar.this,game.getCurrentRoundExchange().getTransferA().getSender(),value));
                                    } catch (Transfer.InvalidTransferException e1) {
                                        e1.printStackTrace();
                                    } catch (Exchange.TransferNotCommittedException e1) {
                                        e1.printStackTrace();
                                    } catch (Game.GameNotStartedYetException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }.start();
                            //pause(1000);
                            return;
                        }else{
                            JOptionPane.showMessageDialog(panel,"Bitte gib eine positive Zahl ein!","Eingabefehler",JOptionPane.WARNING_MESSAGE);
                            amountInput.setText("0");
                        }
                    }catch (NumberFormatException nfe){
                        JOptionPane.showMessageDialog(panel,"Bitte gib eine Zahl ein!","Eingabefehler",JOptionPane.WARNING_MESSAGE);
                        amountInput.setText("0");
                    }
                    //JOptionPane.showMessageDialog(controlPanelB,"Ungültige Eingabe. Versuche es noch einmal!","Eingabefehler",JOptionPane.WARNING_MESSAGE);

                    //amountInput.setText("0");

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

    public void decideOnTransfer(Transfer transfer){
        String transferDesc = "<player> "+getPlayersName()+" <decides_to_transfer> "+((int) transfer.getCreditsTransferred())+" <credits> <to> <player> "+ transfer.getRecipient().getPlayersName();
        receiveMessage(new ActMessage("decide_on_transfer_"+(transfer.getType()== Transfer.TYPE_A?"A":"B"),transferDesc));
    }

    @Override
    public void receiveTransfer(Transfer transfer) {
        super.receiveTransfer(transfer);

        if (gui != null){
            gui.update();
        }

    }

    @Override
    public void makeTransfer(Transfer transfer) throws Transfer.InvalidTransferException {
        super.makeTransfer(transfer);

        if (gui != null){
            gui.update();
        }
    }
}
