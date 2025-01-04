package cn.iocoder.yudao.module.im.service.conversation;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.im.controller.admin.conversation.vo.ImConversationCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.conversation.vo.ImConversationUpdateLastReadTimeReqVO;
import cn.iocoder.yudao.module.im.controller.admin.conversation.vo.ImConversationUpdatePinnedReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.conversation.ImConversationDO;
import cn.iocoder.yudao.module.im.dal.mysql.conversation.ImConversationMapper;
import cn.iocoder.yudao.module.im.enums.conversation.ImConversationTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * IM 会话 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImConversationServiceImpl implements ImConversationService {

    private final String IM_CONVERSATION_ADD = "im-conversation-add";

    @Resource
    private ImConversationMapper imConversationMapper;

    @Resource
    WebSocketMessageSender webSocketMessageSender;

    @Override
    public List<ImConversationDO> getConversationList(Long loginUserId) {
        // 根据loginUserId判断targetId， 自己不能做targetId
        // 如果loginUserId和targetId相同，则需要调换userId和targetId
        List<ImConversationDO> conversationList = imConversationMapper.selectList();

        // 过滤和自己无关的会话
        List<ImConversationDO> conversationFilteredList  = conversationList.stream().filter(item -> loginUserId.equals(item.getUserId()) || loginUserId.equals(item.getTargetId())).toList();
        // 遍历判断loginUserId和targetId相同，相同则将userId设置为targetId， targetId设置为userId
        conversationFilteredList.forEach(item -> {
            if (item.getTargetId().equals(loginUserId)) {
                Long targetId = item.getTargetId();
                Long userId = item.getUserId();
                item.setTargetId(userId);
                item.setUserId(targetId);
            }
        });
        return conversationFilteredList;
    }

    @Override
    public void updatePinned(Long loginUserId, ImConversationUpdatePinnedReqVO updateReqVO) {
        // 1. 获得会话编号
        String no = ImConversationTypeEnum.generateConversationNo(loginUserId, updateReqVO.getTargetId(), updateReqVO.getType());
        // 2. 查询会话
        ImConversationDO conversation = imConversationMapper.selectByNo(no);
        if (conversation == null) {
            // 2.1. 不存在，则插入
            conversation = insertConversation(no, loginUserId, updateReqVO.getTargetId(), updateReqVO.getType());
        }
        // 3. 更新会话
        conversation.setPinned(updateReqVO.getPinned());
        imConversationMapper.updateById(conversation);
        // 4. 做对应更新的 notify 推送
    }

    private ImConversationDO insertConversation(String no, Long userId, Long targetId, Integer type) {
        ImConversationDO imConversationDO = new ImConversationDO();
        imConversationDO.setNo(no);
        imConversationDO.setUserId(userId);
        imConversationDO.setTargetId(targetId);
        imConversationDO.setType(type);
        imConversationMapper.insert(imConversationDO);
        return imConversationDO;
    }

    @Override
    public void updateLastReadTime(Long loginUserId, ImConversationUpdateLastReadTimeReqVO updateReqVO) {
        // 1. 获得会话编号
        String no = ImConversationTypeEnum.generateConversationNo(loginUserId, updateReqVO.getTargetId(), updateReqVO.getType());
        // 2. 查询会话
        ImConversationDO conversation = imConversationMapper.selectByNo(no);
        if (conversation == null) {
            // 2.1. 不存在，则插入
            conversation = insertConversation(no, loginUserId, updateReqVO.getTargetId(), updateReqVO.getType());
        }
        // 3. 更新会话
        conversation.setLastReadTime(updateReqVO.getLastReadTime());
        imConversationMapper.updateById(conversation);
        // 4. 做对应更新的 notify 推送
    }

    @Override
    public ImConversationDO createConversation(Long loginUserId, ImConversationCreateReqVO createReqVO) {
        // 1. 获得会话编号
        String no = ImConversationTypeEnum.generateConversationNo(loginUserId, createReqVO.getTargetId(), createReqVO.getType());
        // 2. 查询会话
        ImConversationDO conversation = imConversationMapper.selectByNo(no);

        if (conversation == null) {
            // 2.1. 不存在，则插入
            conversation = insertConversation(no, loginUserId, createReqVO.getTargetId(), createReqVO.getType());
        }

        // 发送打开会话的通知，并推送会话实体
        // 给自己发送创建会话成功的通知， 方便多端登录的时候保持会话同时更新
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), getLoginUserId(),
                IM_CONVERSATION_ADD, conversation);

        // 给接受者发送创建会话的通知
        // TODO：[dylan] 接受者，在接收到消息的时候本地发现没有回话，就按照会话编号创建一个，因此可以不需要发送这个通知
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), createReqVO.getTargetId(),
                IM_CONVERSATION_ADD, conversation);
        return conversation;
    }

}