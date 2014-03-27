package investmentGame.actor.game;

import investmentGame.Configuration;
import madkit.message.ActMessage;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 27/03/14
 * Time: 12:08
 * To change this template use File | Settings | File Templates.
 */
public class TransferFactory {

    public static Transfer transferFromMessage(ActMessage message, Game game){

        if (message.getAction().startsWith("info_transfer")){

            Pattern contentPattern = Pattern.compile("([^<>]+)\\s+<sends>\\s+(\\d+)\\s+<to>\\s+([^<>]+)");
            Matcher contentMatcher = contentPattern.matcher(message.getContent());

            if (contentMatcher.matches()){

                String sendersName = contentMatcher.group(1);
                double credits = Double.parseDouble(contentMatcher.group(2));
                String recipientsName = contentMatcher.group(3);

                if (message.getAction().equals("info_transfer_A")){
                    try {
                        return new Exchange.TransferA(game.getPlayer(sendersName),game.getPlayer(recipientsName),credits, Configuration.transferAMultiplier);
                    } catch (Transfer.InvalidTransferException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                if (message.getAction().equals("info_transfer_B")){
                    try {
                        return new Exchange.TransferB(game.getPlayer(sendersName),game.getPlayer(recipientsName),credits);
                    } catch (Transfer.InvalidTransferException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                throw new RuntimeException("info_transfer message refers to an unknown transfer type -- cannot create a Transfer object based on it");

            }else{
                throw new RuntimeException("info_transfer message's content cannot be parsed");
            }

        }

        if (message.getAction().startsWith("decide_on_transfer")){
            Pattern transferDescriptionPattern = Pattern.compile("<player>\\s+([^<>\\s]+)\\s+<decides_to_transfer>\\s+(\\d+)\\s+<credits>\\s+<to>\\s+<player>\\s+([^<>\\s]+)");
            Matcher contentMatcher = transferDescriptionPattern.matcher(message.getContent());

            if (contentMatcher.matches()){

                String sendersName = contentMatcher.group(1);
                double credits = Double.parseDouble(contentMatcher.group(2));
                String recipientsName = contentMatcher.group(3);

                if (message.getAction().equals("decide_on_transfer_A")){
                    try {
                        return new Exchange.TransferA(game.getPlayer(sendersName),game.getPlayer(recipientsName),credits, Configuration.transferAMultiplier);
                    } catch (Transfer.InvalidTransferException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }

                if (message.getAction().equals("decide_on_transfer_B")){
                    try {
                        return new Exchange.TransferB(game.getPlayer(sendersName),game.getPlayer(recipientsName),credits);
                    } catch (Transfer.InvalidTransferException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                throw new RuntimeException("decide_on_transfer message refers to an unknown transfer type -- cannot create a Transfer object based on it");

            }else{
                throw new RuntimeException("decide_on_transfer message's content cannot be parsed");
            }

        }

        throw new RuntimeException("ActMessage has as an inappropriate action type -- cannot create a Transfer object based on it");

    }

    public static ActMessage getInfoMessageForTransfer(Exchange.TransferA transfer){

        return new ActMessage("info_transfer_A",transfer.getSender().getPlayersName()+" <sends> "+((int)transfer.getCreditsTransferred())+" <to> "+transfer.getRecipient().getPlayersName());

    }

    public static ActMessage getInfoMessageForTransfer(Exchange.TransferB transfer){

        return new ActMessage("info_transfer_B",transfer.getSender().getPlayersName()+" <sends> "+((int)transfer.getCreditsTransferred())+" <to> "+transfer.getRecipient().getPlayersName());

    }

}
