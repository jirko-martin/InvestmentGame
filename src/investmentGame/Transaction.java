package investmentGame;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 23/03/14
 * Time: 21:06
 * To change this template use File | Settings | File Templates.
 */
public class Transaction {

    public static int TYPE_A = 1;
    public static int TYPE_B = 2;

    private int type;
    private PlayerInterface sender;
    private PlayerInterface recipient;
    private double creditsTransferred;

    public Transaction(int type, PlayerInterface sender, PlayerInterface recipient, double creditsTransferred) {
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.creditsTransferred = creditsTransferred;
    }

    public int getType() {
        return type;
    }

    public PlayerInterface getSender() {
        return sender;
    }

    public void setSender(PlayerInterface sender){
        this.sender = sender;
    }

    public PlayerInterface getRecipient() {
        return recipient;
    }

    public double getCreditsTransferred() {
        return creditsTransferred;
    }

    public String toString(){
        return "Transaction[ from = ("+sender+") , to = ("+recipient+") , amount = ("+creditsTransferred+") ]";
    }
}
