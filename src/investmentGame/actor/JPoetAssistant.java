package investmentGame.actor;

import investmentGame.Reality;
import madkit.kernel.Agent;
import madkit.message.ActMessage;

import java.util.logging.Level;

/**
 * Created by jpoet on 28.03.14.
 */
public class JPoetAssistant extends Agent {
    public JPoetAssistant(){
        setName("JPoetAssistant");
    }

    @Override
    protected void activate() {

        setLogLevel(Level.FINEST);
        // the assistant creates the world and the room while he appears
        createGroupIfAbsent(Reality.World, Reality.Room1);
        // he gets a role
        requestRole(Reality.World, Reality.Room1, "jpoet_assistant");

        // launch - give birth to - Pet
        launchAgent(new Cat("Flori"), true);
        launchAgent(new Cat("Katjuscha"), true);


        pause(2000);
    }

    @Override
    protected void live(){
        while (true){
            pause(60000);
        }
    }
    public void hearNoise(ActMessage msg){
        logger.log(Level.INFO, "JPoet hears " + msg.getContent());
    }

}
