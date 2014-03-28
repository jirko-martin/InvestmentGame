package investmentGame.actor.game;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 26/03/14
 * Time: 12:44
 * To change this template use File | Settings | File Templates.
 */
public class Exchange {

    public static class TransferA extends Transfer{

        private int multiplier;

        public TransferA(PlayerInterface sender, PlayerInterface recipient, double creditsTransferred, int multiplier) throws InvalidTransferException {
            super(TYPE_A, sender, recipient, creditsTransferred);
            this.multiplier = multiplier;
        }

        public int getMultiplier() {
            return multiplier;
        }

        public double getSendersBalanceDelta() {
            return -1.0 * getCreditsTransferred();
        }

        public double getRecipientsBalanceDelta() {
            return +1.0 * ((double)getMultiplier()) * getCreditsTransferred();
        }

        @Override
        public String describeTransferType() {
            return "TYPE_A";
        }
    }

    public static class TransferB extends Transfer{

        public TransferB(PlayerInterface sender, PlayerInterface recipient, double creditsTransferred) throws InvalidTransferException {
            super(TYPE_B, sender, recipient, creditsTransferred);
        }

        public double getSendersBalanceDelta() {
            return -1.0 * getCreditsTransferred();
        }

        public double getRecipientsBalanceDelta() {
            return +1.0 * getCreditsTransferred();
        }

        @Override
        public String describeTransferType() {
            return "TYPE_B";
        }
    }

    public class TransferNotCommittedException extends Exception{

    }

    public class PrimaryTransferNotCommittedYetException extends Exception{

    }

    public class TransferAlreadyCommittedException extends Exception{

    }

    public class ConstraintViolationException extends Exception{
        public ConstraintViolationException(String message){
            super(message);
        }
    }

    public class AmountBGreaterThanAmountReceivedInTransferA extends ConstraintViolationException{

        private double amountB;
        private double amountReceivedTransferA;

        public AmountBGreaterThanAmountReceivedInTransferA(double amountB, double amountReceivedTransferA) {
            super("Amount transferred in transferB must be smaller or equal than the amount received by secondary player in transferA");
            this.amountB = amountB;
            this.amountReceivedTransferA = amountReceivedTransferA;
        }

        public double getAmountReceivedTransferA() {
            return amountReceivedTransferA;
        }

        public double getAmountB() {
            return amountB;
        }
    }

    public class InsufficientCreditBalance extends ConstraintViolationException{

        private double amount;
        private double balance;

        public InsufficientCreditBalance(double amount, double balance) {
            super("Sender cannot make transfer -- insufficient credit balance");
            this.amount = amount;
            this.balance = balance;
        }

        public double getAmountAttemptedToTransfer() {
            return amount;
        }

        public double getBalance() {
            return balance;
        }
    }

    private TransferA transferA;
    private TransferB transferB;

    private Game game;

    public Exchange(Game game){
        this.game = game;
    }

    public TransferA getTransferA() throws TransferNotCommittedException {

        if (transferAIsCommitted()){
            return transferA;
        }else{
            throw new TransferNotCommittedException();
        }

    }

    protected void setTransferA(TransferA transferA, boolean simulate) throws TransferAlreadyCommittedException {

        if (!transferAIsCommitted()){

            if (!simulate)
                this.transferA = transferA;

        }else{
            throw new TransferAlreadyCommittedException();
        }

    }

    public TransferB getTransferB() throws TransferNotCommittedException {

        if (transferBIsCommitted()){
            return transferB;
        }else{
            throw new TransferNotCommittedException();
        }

    }

    protected void setTransferB(TransferB transferB, boolean simulate) throws TransferAlreadyCommittedException, PrimaryTransferNotCommittedYetException, ConstraintViolationException {

        if (!transferBIsCommitted()){
            if (transferAIsCommitted()){

                if (transferB.getSender().equals(transferA.getRecipient())){

                    if (transferB.getRecipient().equals(transferA.getSender())){

                        if (transferB.getCreditsTransferred()<=transferA.getRecipientsBalanceDelta()){

                            if (!simulate)
                                this.transferB = transferB;

                        }else{
                            throw new AmountBGreaterThanAmountReceivedInTransferA(transferB.getCreditsTransferred(),transferA.getRecipientsBalanceDelta());
                        }

                    }else{
                        throw new ConstraintViolationException("Recipient of transferB must be the sender of transferA");
                    }

                }else{
                    throw new ConstraintViolationException("Sender of transferB must be the recipient of transferA");
                }

            }else{
                throw new PrimaryTransferNotCommittedYetException();
            }
        }else{
            throw new TransferAlreadyCommittedException();
        }

    }

    public boolean transferAIsCommitted(){
        return !(transferA==null);
    }

    public boolean transferBIsCommitted(){
        return !(transferB==null);
    }

    public void commit(TransferA transferA, boolean simulate) throws TransferAlreadyCommittedException, ConstraintViolationException {
        if (game.getPlayer(transferA.getSender()).canMakeTransfer(transferA)){
            setTransferA(transferA,simulate);
        }else{
            throw new InsufficientCreditBalance(transferA.getCreditsTransferred(),game.getPlayer(transferA.getSender()).getCreditBalance());
        }
    }

    public void commit(TransferA transferA) throws TransferAlreadyCommittedException, ConstraintViolationException {
        commit(transferA,false);
    }

    public void commit(TransferB transferB,boolean simulate) throws TransferAlreadyCommittedException, PrimaryTransferNotCommittedYetException, ConstraintViolationException {
        setTransferB(transferB, simulate);
    }

    public void commit(TransferB transferB) throws TransferAlreadyCommittedException, PrimaryTransferNotCommittedYetException, ConstraintViolationException {
        commit(transferB,false);
    }

    public boolean isComplete(){
        return (transferAIsCommitted() && transferBIsCommitted());
    }

    public double getEffectivePrimaryPlayersBalanceDelta(){
        double delta = 0;

        try{
            delta += getTransferA().getSendersBalanceDelta();
            delta += getTransferB().getRecipientsBalanceDelta();
        }catch (TransferNotCommittedException nce){
            //alright
        }

        return delta;
    }

    public double getEffectiveSecondaryPlayersBalanceDelta(){
        double delta = 0;

        try{
            delta += getTransferA().getRecipientsBalanceDelta();
            delta += getTransferB().getSendersBalanceDelta();
        }catch (TransferNotCommittedException nce){
            //alright
        }

        return delta;
    }

    public void confirmExecuteTransfer(TransferA transferA) throws TransferNotCommittedException, ConstraintViolationException {
        if (transferAIsCommitted()){
            if (this.transferA.equals(transferA)){

                if (!this.transferA.isApplied()){
                    try{

                        this.transferA.apply(game);

                    }catch(Transfer.InvalidTransferException e){
                        e.printStackTrace();
                    } catch (Transfer.TransferAlreadyAppliedException e) {
                        e.printStackTrace();
                    }
                }else{
                    throw new ConstraintViolationException("transferA "+transferA+" has already been executed");
                }

            }else{
                throw new ConstraintViolationException("attempt to confirmExecute transferA "+transferA+" which is different from transferA committed "+this.transferA);
            }
        }else{
            throw new TransferNotCommittedException();
        }
    }

    public void confirmExecuteTransfer(TransferB transferB) throws TransferNotCommittedException, ConstraintViolationException {
        if (transferBIsCommitted()){
            if (this.transferB.equals(transferB)){

                if (this.transferA.isApplied()){

                    if (!this.transferB.isApplied()){
                        try{

                            this.transferB.apply(game);

                        }catch(Transfer.InvalidTransferException e){
                            e.printStackTrace();
                        } catch (Transfer.TransferAlreadyAppliedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        throw new ConstraintViolationException("transferB "+transferB+" has already been executed");
                    }

                }else{
                    throw new ConstraintViolationException("transferA must be executed before transferB can be executed");
                }

            }else{
                throw new ConstraintViolationException("attempt to confirmExecute transferB "+transferB+" which is different from transferB committed "+this.transferB);
            }
        }else{
            throw new TransferNotCommittedException();
        }
    }

    public void confirmExecuteTransfer(Transfer transfer){
        throw new RuntimeException("UNREGISTERED TRANSFER CLASS");
    }

    public boolean transferAWasExecuted(){
        return transferAIsCommitted() && this.transferA.isApplied();
    }

    public boolean transferBWasExecuted(){
        return transferBIsCommitted() && this.transferB.isApplied();
    }

    public boolean isCompleteAndExecuted(){
        return transferAWasExecuted() && transferBWasExecuted();
    }

}
