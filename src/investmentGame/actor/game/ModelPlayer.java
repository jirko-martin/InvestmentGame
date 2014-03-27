package investmentGame.actor.game;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 23/03/14
 * Time: 11:15
 * To change this template use File | Settings | File Templates.
 */
public class ModelPlayer<GameType extends Game> implements PlayerInterface {

    private GameType game;

    private String name;
    private String picturePath;

    private double creditBalance;

    private GUI gui;

    private Image picture;

    protected boolean selectable = false;

    private class GUI{

        JLabel playersName;
        JLabel creditBalance;

        JPanel panel;

        public GUI(){
            playersName = getPicturePath()!=null ? new JLabel(getPlayersName(),new ImageIcon(getScaledPicture(110)),JLabel.LEFT) : new JLabel(getPlayersName());
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

                panel.setBorder(new LineBorder(new Color(218, 255, 113), 5));

                panel.add(playersName,BorderLayout.NORTH);

                JPanel accountPanel = new JPanel(new BorderLayout());
                creditBalance.setBackground(Color.WHITE);
                accountPanel.setBackground(Color.WHITE);

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

    public ModelPlayer(GameType game,String name, String picturePath) {
        this.game =  game;
        this.name = name;
        this.picturePath = picturePath;
    }

    public void makeTransfer(Transfer transfer) throws Transfer.InvalidTransferException{
        try{
            setCreditBalance(getCreditBalance()+transfer.getSendersBalanceDelta());
        }catch (OverdrawnException e){
            throw new Transfer.InvalidTransferException("account balance of player "+getPlayersName()+" is not sufficient for making transfer "+transfer);
        }
    }

    public void receiveTransfer(Transfer transfer){
        setCreditBalance(getCreditBalance()+transfer.getRecipientsBalanceDelta());
    }

    @Override
    public void setCreditBalance(double balance) {
        System.err.println(game.agent.toString()+" : enter ModelPlayer.setCreditBalance for "+getPlayersName());
        if (balance<0){
            throw new OverdrawnException(balance);
        }

        this.creditBalance = balance;

        if (gui != null){
            System.err.println(game.agent.toString()+" : update ModelPlayer.setCreditBalance for "+getPlayersName());
            gui.update();
        }
        System.err.println(game.agent.toString()+" : exit ModelPlayer.setCreditBalance for "+getPlayersName());
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
    public String getPicturePath() {
        return picturePath;
    }

    @Override
    public String getStringDescription() {
        return "<player> "+getPlayersName()+" <has> "+((int)getCreditBalance())+" <credits> <and_looks_like> "+getPicturePath();
    }

    @Override
    public JPanel getGUIView() {

        game.agent.getLogger().log(Level.INFO,"getGUIView() (ModelPlayer) on "+getPlayersName());

        if (gui == null){
            gui = new GUI();
        }

        return gui.getPanel();

    }

    @Override
    public Image getScaledPicture(int width) {
        if (picture == null)
            picture = new ImageIcon(getPicturePath()).getImage();
        return picture.getScaledInstance(width,-1,Image.SCALE_SMOOTH);
    }

    @Override
    public GameType getGame() {
        return game;
    }

    @Override
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    public boolean canMakeTransfer(Transfer transfer) {
        return getCreditBalance()>=transfer.getCreditsTransferred();
    }

    public String toString(){
        return this.getClass().getName()+"["+game.agent+" -- "+getPlayersName()+"]";
    }


}
