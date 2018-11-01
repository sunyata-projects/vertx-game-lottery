package org.sunyata.game.game.classic;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.sunyata.game.WebRequest;
import org.sunyata.game.contract.GameIds;
import org.sunyata.game.game.Bet;
import org.sunyata.game.game.GameManager;
import org.sunyata.game.game.regular.GameRegularModel;
import org.sunyata.game.lottery.contract.protobuf.common.Common;
import org.sunyata.game.server.OctopusPacketRequest;
import org.sunyata.game.server.message.OctopusRawMessage;
import org.sunyata.game.service.UserLocationInfo;
import org.sunyata.lottery.edy.common.enums.RegularWagerType;
import org.sunyata.lottery.edy.common.vo.GameData;
import org.sunyata.lottery.edy.common.vo.WagerResponse;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author leo
 */
@Component(GameIds.Classic)
public class ClassicBet implements Bet ,ApplicationContextAware {

    @Autowired
    GameManager gameManager;
    private ApplicationContext applicationContext;

    @Override
    public byte[] doBet(String gameCycleId, OctopusPacketRequest request, UserLocationInfo userLocationInfo) throws
            Exception {
        OctopusRawMessage rawMessage = request.getMessage().getRawMessage();
        Common.BetRequestMsg betRequestMsg = Common.BetRequestMsg.parseFrom(rawMessage.getBody());
        int userInGatewayId = request.getMessage().getDataId();
        int gatewayServerId = request.getMessage().getSourceServerId();
        int gameId = betRequestMsg.getGameType();
        int amt = betRequestMsg.getAmt();
        RegularWagerType regularWagerType = RegularWagerType.getByDesc(String.valueOf(amt));
        WebRequest webRequest = applicationContext.getBean("WebRequest_" + gameId, WebRequest.class);
        WagerResponse wagerResponse = webRequest.wager(userLocationInfo.getChannelId(), userLocationInfo
                .getAccountId(), String.valueOf(gameId), gameCycleId, "", new GameData());
        if (wagerResponse == null || !wagerResponse.isSuccess()) {
            throw new Exception("下注失败");
        }
        Common.DealResponseMsg.Builder builder = Common.DealResponseMsg.newBuilder();
        String centerId = wagerResponse.getGameData().getString("centerId");
        String center = wagerResponse.getGameData().getString("center");
        List<String> centerStringList = Arrays.asList(center.split(","));
        List<Integer> centerList = centerStringList.stream().map(Integer::valueOf).collect(Collectors.toList());
        builder.addAllCenterCard(centerList);
        GameRegularModel gameModel = (GameRegularModel) gameManager.getGameModel(userLocationInfo.getChannelId(),
                userLocationInfo
                        .getAccountId());
        gameModel.addDealPhase(centerId, centerList);
        Common.DealResponseMsg build = builder.build();

        return build.toByteArray();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
