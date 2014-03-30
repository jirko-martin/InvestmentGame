package investmentGame.actor;

import investmentGame.Reality;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import madkit.message.MessageFilter;
import madkit.message.StringMessage;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by jpoet on 28.03.14.
 */
class Cat extends Agent {
    public String name;

    private List<AgentAddress> livingAgentAddresses;

    public Cat(String name){
        this.name = name;
        setName(name);
    }

    private int MiauCounter = 1;

    @Override
    protected void activate(){
        setLogLevel(Level.INFO);

        // it gets a role
        requestRole(Reality.World, Reality.Room1, "disturber");
        // wait for all agents
        pause(2000);

    }

    protected void broadcastMessageToMyRoom(Message message){
        // get all roles at my room
        Iterator<String> it;
        it = getExistingRoles(Reality.World, Reality.Room1).iterator();
        while( it.hasNext()) {
            String role = it.next();
            if (isRole(Reality.World, Reality.Room1, role)){
                logger.log(Level.FINEST, "Cat " + this.name + " broadcast message to role " + role);
                broadcastMessage(Reality.World, Reality.Room1, role, message);
            }
        }
    }

    private int factor = 1000;

    @Override
    protected void live(){
        while(true){
            if (!isMessageBoxEmpty()){
                StringMessage message = (StringMessage) nextMessage(new MessageFilter() {
                    @Override
                    public boolean accept(Message message) {
                        if (message instanceof StringMessage) {
                            return true;
                        }
                        return false;
                    }
                });
                hearNoise(message);
            }

            //stub
            this.recognizeTouch();
            pause(100);
        }
    }

    // attributes which can be triggered from outside
    double noiseThreshold = 100;

    public void recognizeTouch(){
        // make noise sometimes
        logger.log(Level.FINEST, "Cat " + this.name + " recognized a touch ");
        // make noise randomly

        if (Math.random()*factor < noiseThreshold){
            this.makeNoise();
            //reduce threshold
            if (noiseThreshold <= 10 ){
                noiseThreshold = 100;
            }else{
                noiseThreshold = noiseThreshold - 10;
            }

        }
    }


    // abilities a cat has, and can only be triggered through inner state, but trigger messages to outer world
    private void makeNoise() {
        StringMessage msg = new StringMessage("miau ("+MiauCounter +")");
        logger.log(Level.INFO, "Katze "+name+" mauzt. ["+msg.getContent()+"]");
        broadcastMessageToMyRoom(msg);
        MiauCounter++;
    }

    public void hearNoise(StringMessage msg){
        logger.log(Level.INFO, this.name+" hoert " + msg.getContent() + " von "+ msg.getSender().toString());
    }


}
