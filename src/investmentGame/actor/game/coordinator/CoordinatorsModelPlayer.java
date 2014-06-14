package investmentGame.actor.game.coordinator;

import investmentGame.actor.game.ModelPlayer;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 25/03/14
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */
public class CoordinatorsModelPlayer extends ModelPlayer<CoordinatorsGame> {
    public CoordinatorsModelPlayer(CoordinatorsGame game, String name, URI picturePath) {
        super(game, name, picturePath);
    }
}
