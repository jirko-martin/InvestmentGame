package investmentGame.actor.game.coordinator;

import investmentGame.actor.game.PlayerInterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 01/06/14
 * Time: 13:44
 * To change this template use File | Settings | File Templates.
 */
public class FinalDisplay {

    private class FinalDisplayPanel extends JPanel{

        private SortedSet<PlayerInterface> winners;
        private SortedSet<PlayerInterface> losers;

        private java.util.List<Image> winnersImages = new LinkedList<Image>();
        private java.util.List<Image> losersImages = new LinkedList<Image>();

        private int maxRowCount;

        public FinalDisplayPanel(SortedSet<PlayerInterface> winners, SortedSet<PlayerInterface> losers){
            super();
            this.winners = winners;
            this.losers = losers;

            if (this.winners.size()>this.losers.size())
                maxRowCount = winners.size();
            else
                maxRowCount = losers.size();

            maxRowCount = Math.max(maxRowCount,2);

            setPreferredSize(new Dimension(maxRowCount*200,530));

            for (PlayerInterface winner : winners) {
                try {
                    System.out.println(winner.getPicturePath().getSchemeSpecificPart());
                    String path = winner.getPicturePath().getSchemeSpecificPart();
                    if (path.contains(".jar!")){
                        path = path.substring(path.lastIndexOf(".jar!")+5);
                    }
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is == null){
                        is = new FileInputStream(path);
                    }
                    winnersImages.add(ImageIO.read(is).getScaledInstance(-1, 150, Image.SCALE_SMOOTH));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (PlayerInterface loser : losers) {
                try {
                    System.out.println(loser.getPicturePath().getSchemeSpecificPart());
                    String path = loser.getPicturePath().getSchemeSpecificPart();
                    if (path.contains(".jar!")){
                        path = path.substring(path.lastIndexOf(".jar!")+5);
                    }
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is == null){
                        is = new FileInputStream(path);
                    }
                    losersImages.add(ImageIO.read(is).getScaledInstance(-1, 150, Image.SCALE_SMOOTH));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);

            Rectangle s = this.getBounds();

            Rectangle winnersRect = new Rectangle(s.x, s.y, s.width, s.height/2);
            Rectangle losersRect = new Rectangle(s.x, s.y+s.height/2, s.width, s.height/2);

            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(new Color(90, 255, 85));
            g2d.fillRoundRect(winnersRect.x, winnersRect.y, winnersRect.width, winnersRect.height, 10, 10);
            g2d.setColor(new Color(255, 143, 161));
            g2d.fillRoundRect(losersRect.x, losersRect.y, losersRect.width, losersRect.height, 10, 10);

            //draw winners:

            int offset = 10;

            int rank = 1;

            Iterator<PlayerInterface> playersIterator = winners.iterator();

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font(Font.SANS_SERIF,Font.BOLD,25));

            g2d.drawString("GEWINNER-GRUPPE", winnersRect.x + 5, winnersRect.y + 25);

            g2d.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
            for (int i=0;i<winnersImages.size();i++,rank++){
                PlayerInterface player = playersIterator.next();
                g2d.drawImage(winnersImages.get(i), offset, winnersRect.y + 30, this);
                int imageHeight = winnersImages.get(i).getHeight(this);
                int imageWidth = winnersImages.get(i).getWidth(this);

                //draw text
                //n. Platz
                int yPos = winnersRect.y + 30 + imageHeight + 10;
                g2d.drawString(""+rank+". Platz"+(rank==1?" (Bester)":""), offset, yPos);
                yPos += 20;
                //player's name
                g2d.drawString(player.getPlayersName(),offset,yPos);
                yPos += 20;
                //credits
                g2d.drawString(""+(int)player.getCreditBalance()+" Punkte",offset,yPos);
                offset += imageWidth + 30;
            }
            //draw losers:

            offset = 10;

            playersIterator = losers.iterator();

            g2d.setFont(new Font(Font.SANS_SERIF,Font.BOLD,25));

            g2d.drawString("VERLIERER-GRUPPE",losersRect.x+5,losersRect.y+25);

            g2d.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
            for (int i=0;i<losersImages.size();i++,rank++){
                PlayerInterface player = playersIterator.next();
                g2d.drawImage(losersImages.get(i), offset, losersRect.y + 30, this);
                int imageHeight = losersImages.get(i).getHeight(this);
                int imageWidth = losersImages.get(i).getWidth(this);
                //draw text
                //n. Platz
                int yPos = losersRect.y + 30 + imageHeight + 10;
                g2d.drawString(""+rank+". Platz"+(i==losersImages.size()-1?" (Schlechtester)":""), offset, yPos);
                yPos += 20;
                //player's name
                g2d.drawString(player.getPlayersName(),offset,yPos);
                yPos += 20;
                //credits
                g2d.drawString(""+(int)player.getCreditBalance()+" Punkte",offset,yPos);
                offset += imageWidth + 30;
            }
        }

    }

    private KMeansPlayerGroups kMeansPlayerGroups;

    public FinalDisplay(KMeansPlayerGroups kMeansPlayerGroups){
        this.kMeansPlayerGroups = kMeansPlayerGroups;
    }

    public void show(){
        this.kMeansPlayerGroups.cluster();

        KMeansPlayerGroups.Cluster[] clusters = this.kMeansPlayerGroups.getClusters();

        KMeansPlayerGroups.Cluster winners = clusters[0];
        KMeansPlayerGroups.Cluster losers = clusters[1];

        if (winners.getMean()<losers.getMean()){
            KMeansPlayerGroups.Cluster swap = winners;
            winners = losers;
            losers = swap;
        }

        JPanel panel = new FinalDisplayPanel(winners.getPlayersInClusterRanked(),losers.getPlayersInClusterRanked());

        JFrame display = new JFrame();

        display.setSize(new Dimension(300, 500));

        display.getContentPane().add(panel);

        display.pack();

        display.setVisible(true);

    }

}
