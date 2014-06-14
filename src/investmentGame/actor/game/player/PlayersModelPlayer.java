package investmentGame.actor.game.player;

import investmentGame.actor.game.ModelPlayer;
import investmentGame.swing.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 25/03/14
 * Time: 19:05
 * To change this template use File | Settings | File Templates.
 */
public class PlayersModelPlayer extends ModelPlayer<PlayersGame> {

    public PlayersModelPlayer(PlayersGame game, String name, URI picturePath) {
        super(game, name, picturePath);
    }

    @Override
    public JPanel getGUIView() {
        final JPanel panel = super.getGUIView();
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectable){
                    //panel.setBorder(new LineBorder(new Color(218, 255, 113), 5));
                    ((RoundedPanel)panel).setShadowColor(new Color(0, 0, 0, 130));
                    ((RoundedPanel)panel).setShadowAlpha(100);
                    ((RoundedPanel)panel).setStrokeSize(1);
                    panel.repaint();
                    getGame().getPlayersSelf().selectPlayer(PlayersModelPlayer.this);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectable){
                    //panel.setBorder(new LineBorder(Color.RED, 5));
                    ((RoundedPanel)panel).setShadowColor(new Color(255, 155, 84, 255));
                    ((RoundedPanel)panel).setShadowAlpha(255);
                    panel.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectable){
                    //panel.setBorder(new LineBorder(new Color(218, 255, 113), 5));
                    ((RoundedPanel)panel).setShadowColor(new Color(0, 0, 0, 130));
                    ((RoundedPanel)panel).setShadowAlpha(100);
                    ((RoundedPanel)panel).setStrokeSize(1);
                    panel.repaint();
                }
            }
        });
        return panel;
    }

}
