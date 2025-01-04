package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.module.im.controller.admin.message.vo.ImMessageListByNoReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.ImMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.ImMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImMessageDO;

import java.util.List;

/**
 * 消息 Service 接口
 *
 * @author 芋道源码
 */
public interface ImMessageService {

    /**
     * 获得历史消息
     *
     * @param listReqVO   分页查询
     * @return 消息分页
     */
    List<ImMessageDO> getMessageList(ImMessageListReqVO listReqVO, Long loginUserId);

    /**
     * 获得历史消息
     *
     * @param listReqVO   分页查询
     * @return 消息分页
     */
    List<ImMessageDO> getMessageListByConversationNo(ImMessageListByNoReqVO listReqVO);

    /**
     * 拉取消息-大于 seq 的消息
     *
     * @param loginUserId 登录用户编号
     * @param sequence    序列号
     * @param size        数量
     * @return 消息列表
     */
    List<ImMessageDO> pullMessageList(Long loginUserId, Long sequence, Integer size);

    /**
     * 发送消息
     *
     * @param loginUserId 登录用户编号
     * @param message     消息
     * @return 消息编号
     */
    ImMessageDO sendMessage(Long loginUserId, ImMessageSendReqVO message);
}