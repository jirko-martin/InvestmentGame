package investmentGame.actor.game;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 23/03/14
 * Time: 11:11
 * To change this template use File | Settings | File Templates.
 */
public interface PlayerInterface{

    public Transfer transferA(PlayerInterface recipient,double credits);

    public Transfer transferB(PlayerInterface recipient,double credits);

    public void setCreditBalance(double balance);

    public double getCreditBalance();

    public String getPlayersName();

    public String getPicturePath();

    public String getStringDescription();

    public JPanel getGUIView();

    public Image getScaledPicture(int width);

    public Game getGame();

    public void setSelectable(boolean selectable);

}
