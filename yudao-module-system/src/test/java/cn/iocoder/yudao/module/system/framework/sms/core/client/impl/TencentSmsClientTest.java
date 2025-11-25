package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.google.common.collect.Lists;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

/**
 * {@link TencentSmsClient} 的单元测试
 *
 * @author shiwp
 */
public class TencentSmsClientTest extends BaseMockitoUnitTest {

    private final SmsChannelProperties properties = new SmsChannelProperties()
            .setApiKey(randomString() + " " + randomString()) // 随机一个 apiKey，避免构建报错
            .setApiSecret(randomString()) // 随机一个 apiSecret，避免构建报错
            .setSignature("芋道源码");

    @InjectMocks
    private TencentSmsClient smsClient = new TencentSmsClient(properties);

    @Test
    public void testDoSendSms_success() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            Long sendLogId = randomLongId();
            String mobile = randomString();
            String apiTemplateId = randomString();
            List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                    new KeyValue<>("1", 1234), new KeyValue<>("2", "login"));
            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("""
                                    {
                                        "Response": {
                                            "SendStatusSet": [
                                                {
                                                    "SerialNo": "5000:1045710669157053657849499619",
                                                    "PhoneNumber": "+8618511122233",
                                                    "Fee": 1,
                                                    "SessionContext": "test",
                                                    "Code": "Ok",
                                                    "Message": "send success",
                                                    "IsoCode": "CN"
                                                },
                                            ],
                                            "RequestId": "a0aabda6-cf91-4f3e-a81f-9198114a2279"
                                        }
                                    }\
                                    """);

            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                    apiTemplateId, templateParams);
            // 断言
            assertTrue(result.getSuccess());
            assertEquals("5000:1045710669157053657849499619", result.getSerialNo());
            assertEquals("a0aabda6-cf91-4f3e-a81f-9198114a2279", result.getApiRequestId());
            assertEquals("send success", result.getApiMsg());
        }
    }

    @Test
    public void testDoSendSms_fail_01() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            Long sendLogId = randomLongId();
            String mobile = randomString();
            String apiTemplateId = randomString();
            List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                    new KeyValue<>("1", 1234), new KeyValue<>("2", "login"));

            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("""
                                    {
                                        "Response": {
                                            "SendStatusSet": [
                                                {
                                                    "SerialNo": "5000:1045710669157053657849499619",
                                                    "PhoneNumber": "+8618511122233",
                                                    "Fee": 1,
                                                    "SessionContext": "test",
                                                    "Code": "ERROR",
                                                    "Message": "send success",
                                                    "IsoCode": "CN"
                                                },
                                            ],
                                            "RequestId": "a0aabda6-cf91-4f3e-a81f-9198114a2279"
                                        }
                                    }\
                                    """);

            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                    apiTemplateId, templateParams);
            // 断言
            assertFalse(result.getSuccess());
            assertEquals("5000:1045710669157053657849499619", result.getSerialNo());
            assertEquals("a0aabda6-cf91-4f3e-a81f-9198114a2279", result.getApiRequestId());
            assertEquals("send success", result.getApiMsg());
        }
    }

    @Test
    public void testDoSendSms_fail_02() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            Long sendLogId = randomLongId();
            String mobile = randomString();
            String apiTemplateId = randomString();
            List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                    new KeyValue<>("1", 1234), new KeyValue<>("2", "login"));

            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("{\"Response\":{\"Error\":{\"Code\":\"AuthFailure.SecretIdNotFound\",\"Message\":\"The SecretId is not found, please ensure that your SecretId is correct.\"},\"RequestId\":\"2a88f82a-261c-4ac6-9fa9-c7d01aaa486a\"}}");

            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                    apiTemplateId, templateParams);
            // 断言
            assertFalse(result.getSuccess());
            assertEquals("2a88f82a-261c-4ac6-9fa9-c7d01aaa486a", result.getApiRequestId());
            assertEquals("AuthFailure.SecretIdNotFound", result.getApiCode());
            assertEquals("The SecretId is not found, please ensure that your SecretId is correct.", result.getApiMsg());
        }
    }

    @Test
    public void testParseSmsReceiveStatus() {
        // 准备参数
        String text = """
                [
                    {
                        "user_receive_time": "2015-10-17 08:03:04",
                        "nationcode": "86",
                        "mobile": "13900000001",
                        "report_status": "SUCCESS",
                        "errmsg": "DELIVRD",
                        "description": "用户短信送达成功",
                        "sid": "12345",
                        "ext": {"logId":"67890"}
                    }
                ]""";

        // 调用
        List<SmsReceiveRespDTO> statuses = smsClient.parseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        assertTrue(statuses.getFirst().getSuccess());
        assertEquals("DELIVRD", statuses.getFirst().getErrorCode());
        assertEquals("13900000001", statuses.getFirst().getMobile());
        assertEquals(LocalDateTime.of(2015, 10, 17, 8, 3, 4), statuses.getFirst().getReceiveTime());
        assertEquals("12345", statuses.getFirst().getSerialNo());
    }

    @Test
    public void testGetSmsTemplate() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            String apiTemplateId = "1122";

            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("""
                            {     "Response": {
                                    "DescribeTemplateStatusSet": [
                                        {
                                            "TemplateName": "验证码",
                                            "TemplateId": 1122,
                                            "International": 0,
                                            "ReviewReply": "审批备注",
                                            "CreateTime": 1617379200,
                                            "TemplateContent": "您的验证码是{1}",
                                            "StatusCode": 0
                                        },
                                       \s
                                    ],
                                    "RequestId": "f36e4f00-605e-49b1-ad0d-bfaba81c7325"
                                }}\
                            """);

            // 调用
            SmsTemplateRespDTO result = smsClient.getSmsTemplate(apiTemplateId);
            // 断言
            assertEquals("1122", result.getId());
            assertEquals("您的验证码是{1}", result.getContent());
            assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(), result.getAuditStatus());
            assertEquals("审批备注", result.getAuditReason());
        }
    }

    @Test
    public void testConvertSmsTemplateAuditStatus() {
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(),
                smsClient.convertSmsTemplateAuditStatus(0));
        assertEquals(SmsTemplateAuditStatusEnum.CHECKING.getStatus(),
                smsClient.convertSmsTemplateAuditStatus(1));
        assertEquals(SmsTemplateAuditStatusEnum.FAIL.getStatus(),
                smsClient.convertSmsTemplateAuditStatus(-1));
        assertThrows(IllegalArgumentException.class, () -> smsClient.convertSmsTemplateAuditStatus(3),
                "未知审核状态(3)");
    }

}
