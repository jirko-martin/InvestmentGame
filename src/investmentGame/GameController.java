package investmentGame;

import madkit.kernel.Madkit;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 17:04
 * To change this template use File | Settings | File Templates.
 */
public class GameController {

    public static void main(String[] args){

        new Madkit(
                Madkit.Option.launchAgents.toString(),
                Coordinator.class.getName() + ",true,1;"
                + ComputerPlayer.class.getName() + ",true,1;"
//                JeromesAssistant.class.getName() + ",true,1;"
        );

    }
}
