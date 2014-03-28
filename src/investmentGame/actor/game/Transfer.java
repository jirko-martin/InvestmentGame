package investmentGame.actor.game;

import investmentGame.actor.game.PlayerInterface;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 23/03/14
 * Time: 21:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class Transfer<SenderType extends PlayerInterface,RecipientType extends PlayerInterface> {

    public static class InvalidTransferException extends Exception{
        public InvalidTransferException(String message){
            super(message);
        }
    }

    public static class TransferAlreadyAppliedException extends Exception{

    }

    public static int TYPE_A = 1;
    public static int TYPE_B = 2;

    private int type;
    private SenderType sender;
    private RecipientType recipient;
    private double creditsTransferred;

    private boolean applied = false;

    public Transfer(int type, SenderType sender, RecipientType recipient, double creditsTransferred) throws InvalidTransferException {

        if (sender.equals(recipient)){
            throw new InvalidTransferException("sender and recipient in a transfer must not be the same");
        }

        if (creditsTransferred<0){
            throw new InvalidTransferException("the amount of a transfer needs to be greater than 0.0");
        }

        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.creditsTransferred = creditsTransferred;
    }

    public int getType() {
        return type;
    }

    public SenderType getSender() {
        return sender;
    }

    public void setSender(SenderType sender){
        this.sender = sender;
    }

    public RecipientType getRecipient() {
        return recipient;
    }

    public double getCreditsTransferred() {
        return creditsTransferred;
    }

    public String toString(){
        return "TRANSFER {<"+describeTransferType()+">("+(applied?"applied":"not_applied")+")[ from = ("+sender+") , to = ("+recipient+") , amount = ("+creditsTransferred+") ]}";
    }

    public boolean equals(Transfer other){
        return other.getSender().equals(getSender())
                && other.getRecipient().equals(getRecipient())
                && ((int)other.getCreditsTransferred())==((int)getCreditsTransferred());
    }

    public void apply(Game game) throws InvalidTransferException, TransferAlreadyAppliedException {

        if (!isApplied()){

            game.getPlayer(getSender()).makeTransfer(this);

            game.getPlayer(getRecipient()).receiveTransfer(this);

            applied = true;

        }else{
            throw new TransferAlreadyAppliedException();
        }
    }

    public boolean isApplied(){
        return applied;
    }

    public abstract double getSendersBalanceDelta();

    public abstract double getRecipientsBalanceDelta();

    public abstract String describeTransferType();

}
