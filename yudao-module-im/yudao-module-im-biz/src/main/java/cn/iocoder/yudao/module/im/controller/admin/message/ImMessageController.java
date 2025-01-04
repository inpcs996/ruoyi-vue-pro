package cn.iocoder.yudao.module.im.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.*;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImMessageDO;
import cn.iocoder.yudao.module.im.service.message.ImMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - IM 消息")
@RestController
@RequestMapping("/im/message")
@Validated
public class ImMessageController {

    @Resource
    private ImMessageService imMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送消息")
    public CommonResult<ImMessageSendRespVO> sendMessage(@Valid @RequestBody ImMessageSendReqVO imMessageSendReqVO) {
        ImMessageDO message = imMessageService.sendMessage(getLoginUserId(), imMessageSendReqVO);
        return success(BeanUtils.toBean(message, ImMessageSendRespVO.class));
    }

    @GetMapping("/pull")
    @Operation(summary = "消息列表-拉取大于 sequence 的消息列表")
    @Parameter(name = "sequence", description = "序号", required = true, example = "1")
    @Parameter(name = "size", description = "条数", required = true, example = "10")
    public CommonResult<List<ImMessageRespVO>> pullMessageList(@RequestParam("sequence") Long sequence,
                                                               @RequestParam("size") Integer size) {
        List<ImMessageDO> messages = imMessageService.pullMessageList(getLoginUserId(), sequence, size);
        return success(BeanUtils.toBean(messages, ImMessageRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "消息列表-根据接收人和发送时间进行分页查询")
    public CommonResult<List<ImMessageRespVO>> getMessageList(@Valid ImMessageListReqVO listReqVO) {

        Long loginUserId = getLoginUserId();
        List<ImMessageDO> messagePage = imMessageService.getMessageList(listReqVO, loginUserId);
        return success(BeanUtils.toBean(messagePage, ImMessageRespVO.class));
    }

}