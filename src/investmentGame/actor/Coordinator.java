package investmentGame.actor;

import investmentGame.actor.game.coordinator.CoordinatorsGame;
import investmentGame.actor.game.coordinator.CoordinatorsModelPlayer;
import investmentGame.actor.game.PlayerInterface;
import madkit.kernel.Agent;
import madkit.message.ActMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 22/03/14
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */
public class Coordinator extends Agent{

    private Map<String, CoordinatorsGame> games;

    @Override
    protected void live() {

        while (true){

            ActMessage message = (ActMessage)waitNextMessage();

            String senderGroup = message.getSender().getGroup();

            if (senderGroup.startsWith("game_")){

                CoordinatorsGame game = games.get(senderGroup);

                if (game != null){

                    if (message.getAction().equals("hello_my_name_is")){

                        if (game.isStarted() || game.allPlayersHaveJoined()){
                            sendReply(message,new ActMessage("rejected"));
                        }

                        Pattern playerDescPattern = Pattern.compile("<(primary|secondary)>\\s+<player>\\s+([^<>\\s]+)\\s+<looking_like_that>(.+)");
                        Matcher m = playerDescPattern.matcher(message.getContent());

                        if (m.matches()){

                            boolean primaryPlayer = m.group(1).equals("primary");
                            String playersName = m.group(2);
                            String picturePath = m.group(3);

                            PlayerInterface modelPlayer = new CoordinatorsModelPlayer(game,playersName,picturePath);

                            game.addPlayer(modelPlayer,primaryPlayer);

                            modelPlayer.setCreditBalance(100);

                            sendReply(message, new ActMessage("welcome"));

                            if (game.allPlayersHaveJoined()){
                                logger.log(Level.INFO,"All players have joined "+game.getGameId()+" ... I will start the game in a few moments.");
                                pause(500);
                                game.start();
                            }
                        }else{
                            //TODO throw Exception?
                        }

                    }else{

                        if (game.isStarted()){

                            game.processMessageEvent(message);

                        }

                    }

                }

            }

            if (message.getAction().equals("request_game")){

                if (message.getSender().getRole().equals("player")
                        || message.getSender().getRole().equals("jeromes_assistant")){

                    Pattern requestGameSpecPattern = Pattern.compile("<new_game>\\s+<for>\\s+(\\d+)\\s+<players>\\s+<having>\\s+(\\d+)\\s+<rounds>\\s+<invite>\\s+(\\d+)\\s+<computer_players>");

                    Matcher m = requestGameSpecPattern.matcher(message.getContent());

                    if (m.matches()){
                        int numberOfPlayersTotal = Integer.parseInt(m.group(1));
                        int numberOfRounds = Integer.parseInt(m.group(2));
                        int numberOfComputerPlayersToInvite = Integer.parseInt(m.group(3));

                        CoordinatorsGame game = initializeGame(numberOfPlayersTotal,numberOfRounds,numberOfComputerPlayersToInvite);

                        games.put(game.getGameId(),game);

                        sendReply(message, new ActMessage("game_initialized", game.getGameId()));
                    }else{
                        //TODO throw exception?
                    }

                }

            }



        }


    }

    private CoordinatorsGame initializeGame(int numberOfPlayersTotal,int numberOfRounds,int numberOfComputerPlayersToInvite){

        if (numberOfPlayersTotal>=2 && (numberOfComputerPlayersToInvite<numberOfPlayersTotal)){

            final String gameId = "game_"+games.size();

            createGroup("investment_game",gameId);

            requestRole("investment_game",gameId,"coordinator");

            CoordinatorsGame game = new CoordinatorsGame(this,gameId,numberOfPlayersTotal,numberOfRounds);

            for (int i=0;i<numberOfComputerPlayersToInvite;i++){
                final Player player = new ComputerPlayer(null);
                launchAgent(player);
                new Thread(){
                    @Override
                    public void run() {
                        player.joinGame(gameId,false,"---");
                    }
                }.start();

            }

            return game;

        }else{
            //TODO throw exception?
        }

        return null;

    }

    @Override
    protected void activate() {

        games = new HashMap<String, CoordinatorsGame>();

        setLogLevel(Level.FINEST);
        createGroupIfAbsent("investment_game", "tout_le_monde");
        requestRole("investment_game","tout_le_monde","coordinator");

        pause(500);

    }

    public void pauseAWhile(int msec){
        pause(msec);
    }

}
