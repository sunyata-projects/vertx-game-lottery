package org.sunyata.game.game.regular.phase;


import org.sunyata.game.game.GamePhaseModel;

/**
 * Created by leo on 17/5/16.
 */
public class RegularClearPhaseModel extends GamePhaseModel<RegularClearPhaseData> {

    public RegularClearPhaseModel() {

    }

    public RegularClearPhaseModel(String gameInstanceId, String phaseName, int orderBy) {
        super(gameInstanceId,phaseName, orderBy);
    }

}
