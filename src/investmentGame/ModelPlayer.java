package investmentGame;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 23/03/14
 * Time: 11:15
 * To change this template use File | Settings | File Templates.
 */
public class ModelPlayer implements PlayerInterface{

    private String name;
    private ImageIcon picture;

    private double creditBalance;

    private GUI gui;



    public class GUI{

        JLabel playersName;
        JLabel creditBalance;

        JPanel panel;

        public GUI(){
            playersName = new JLabel(getPlayersName());
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

                panel.add(playersName,BorderLayout.NORTH);

                JPanel accountPanel = new JPanel(new BorderLayout());

                accountPanel.add(creditBalance,BorderLayout.CENTER);

                panel.add(accountPanel,BorderLayout.EAST);
            }

            return panel;
        }

    }

    public static class OverdrawnException extends RuntimeException{

        private double amount;

        public OverdrawnException(double amount){
            this.amount = amount;
        }

        public double getAmount() {
            return amount;
        }
    }

    public ModelPlayer(String name, ImageIcon picture) {
        this.name = name;
        this.picture = picture;
    }

    @Override
    public Transaction transferA(PlayerInterface recipient, double credits) throws OverdrawnException{
        this.setCreditBalance(this.getCreditBalance() - credits);
        recipient.setCreditBalance(recipient.getCreditBalance() + (3 * credits));
        return new Transaction(Transaction.TYPE_A,this,recipient,credits);
    }

    @Override
    public Transaction transferB(PlayerInterface recipient, double credits) throws OverdrawnException{
        this.setCreditBalance(this.getCreditBalance() - credits);
        recipient.setCreditBalance(recipient.getCreditBalance() + credits);
        return new Transaction(Transaction.TYPE_B,this,recipient,credits);
    }

    @Override
    public void setCreditBalance(double balance) {

        if (balance<0){
            throw new OverdrawnException(balance);
        }

        this.creditBalance = balance;

        if (gui != null){
            gui.update();
        }
    }

    @Override
    public double getCreditBalance() {
        return creditBalance;
    }

    @Override
    public String getPlayersName() {
        return name;
    }

    @Override
    public ImageIcon getPicture() {
        return picture;
    }

    @Override
    public String getStringDescription() {
        return "<player> "+getPlayersName()+" <has> "+((int)getCreditBalance())+" <credits>";
    }

    @Override
    public JPanel getGUIView() {

        if (gui == null){
            gui = new GUI();
        }

        return gui.getPanel();

    }


}
