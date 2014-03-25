package investmentGame;

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

                        PlayerInterface modelPlayer = new ModelPlayer(message.getContent(),null);

                        game.addPlayer(modelPlayer);

                        modelPlayer.setCreditBalance(100);

                        sendReply(message, new ActMessage("welcome"));

                        if (game.allPlayersHaveJoined()){
                            logger.log(Level.INFO,"All players have joined "+game.getGameId()+" ... I will start the game in a few moments.");
                            pause(500);
                            game.start();
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

                    Pattern requestGameSpecPattern = Pattern.compile("(\\d+)\\s+<players>\\s+<thereof>\\s+(\\d+)\\s+<computer_players>");

                    Matcher m = requestGameSpecPattern.matcher(message.getContent());

                    if (m.matches()){
                        int numberOfPlayersTotal = Integer.parseInt(m.group(1));
                        int numberOfComputerPlayersToInvite = Integer.parseInt(m.group(2));

                        CoordinatorsGame game = initializeGame(numberOfPlayersTotal,numberOfComputerPlayersToInvite);

                        games.put(game.getGameId(),game);

                        sendReply(message, new ActMessage("game_initialized", game.getGameId()));
                    }else{
                        //TODO throw exception?
                    }

                }

            }



        }


    }

    private CoordinatorsGame initializeGame(int numberOfPlayersTotal,int numberOfComputerPlayersToInvite){

        if (numberOfPlayersTotal>=2 && (numberOfComputerPlayersToInvite<numberOfPlayersTotal)){

            final String gameId = "game_"+games.size();

            createGroup("investment_game",gameId);

            requestRole("investment_game",gameId,"coordinator");

            CoordinatorsGame game = new CoordinatorsGame(this,gameId,numberOfPlayersTotal,60);

            for (int i=0;i<numberOfComputerPlayersToInvite;i++){
                final Player player = new ComputerPlayer(true);
                launchAgent(player);
                (new Thread(){
                    @Override
                    public void run() {
                        player.joinGame(gameId);
                    }
                }).start();

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

        setLogLevel(Level.INFO);
        createGroupIfAbsent("investment_game", "tout_le_monde");
        requestRole("investment_game","tout_le_monde","coordinator");

        pause(500);

    }

    public void pauseAWhile(int msec){
        pause(msec);
    }

}
