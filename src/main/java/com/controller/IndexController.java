package com.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.config.URLConstant;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.dingtalk.api.response.OapiUserGetResponse;
import com.dingtalk.api.response.OapiUserGetuserinfoResponse;
import com.dingtalk.api.response.OapiUserSimplelistResponse;
import com.taobao.api.ApiException;
import com.util.*;
import org.apache.http.client.utils.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业内部E应用 实现了最简单的免密登录（免登）与组织同步功能
 */
@RestController
public class IndexController {
    private static final Logger bizLogger = LoggerFactory.getLogger(IndexController.class);

    @Value("${elafs.host}")
    private String elafsHost;

    @Value("${domain.name}")
    private String domainName;

    /**
     * 欢迎页面,通过url访问，判断后端服务是否启动
     */
    @RequestMapping(value = "/welcome", method = RequestMethod.GET)
    public String welcome() {
        return "welcome";
    }

    /**
     * 钉钉用户登录，显示当前登录用户的userId和名称
     *
     * @param requestAuthCode 免登临时code
     */
    @RequestMapping(value = "/ding/login", method = RequestMethod.POST)
    @ResponseBody
    public String dingLogin(@RequestParam(value = "authCode") String requestAuthCode) throws ApiException {
        String dingConfiUrl = elafsHost + "/m/api/dingConfig.cobo";
        HashMap<String, String> configParam = new HashMap<>();
        configParam.put("site", domainName);
        String accessToken = "";
        String domain_id = "";
        try {
            String configResponse = HttpClientUtil.doPost(dingConfiUrl, configParam, "UTF-8");
            JSONObject confiJson = (JSONObject) JSON.parse(configResponse);
            Integer status = confiJson.getInteger("status");
            if (confiJson != null && status != null && status.intValue()==0) {
                accessToken = String.valueOf(confiJson.get("accessToken"));
                domain_id = String.valueOf(confiJson.get("domain_id"));
            } else {
                bizLogger.info("获取配置信息失败!");
                ServiceResult serviceResult = ServiceResult.failure("-1", "获取配置信息失败!" + requestAuthCode);
                return JSON.toJSONString(serviceResult);
            }
        } catch (Exception e) {
            bizLogger.info("访问配置信息失败!");
            ServiceResult serviceResult = ServiceResult.failure("-1", "访问配置信息失败!" + requestAuthCode);
            return JSON.toJSONString(serviceResult);
        }

        //获取用户信息
        DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_GET_USER_INFO);
        OapiUserGetuserinfoRequest oapiRequest = new OapiUserGetuserinfoRequest();
        oapiRequest.setCode(requestAuthCode);
        oapiRequest.setHttpMethod("GET");

        OapiUserGetuserinfoResponse oapiResponse;
        oapiResponse = client.execute(oapiRequest, accessToken);
        bizLogger.info(String.valueOf(oapiResponse));
        //3.查询得到当前用户的userId
        // 获得到userId之后应用应该处理应用自身的登录会话管理（session）,避免后续的业务交互（前端到应用服务端）每次都要重新获取用户身份，提升用户体验
        String userId = oapiResponse.getUserid();
        if (StringUtils.isBlank(userId)) {
            bizLogger.info("can not found ding's user with authCode=" + requestAuthCode);
            ServiceResult serviceResult = ServiceResult.failure("-1", "获取钉钉用户userId失败" + requestAuthCode);
            return JSON.toJSONString(serviceResult);
        }

        //4.获取详细信息
        OapiUserGetResponse oapiUserGetResponse = getUserName(accessToken, userId);
        String name = StringUtils.nullToBlank(oapiUserGetResponse.getName());
        String email = StringUtils.nullToBlank(oapiUserGetResponse.getEmail());
        String mobile = StringUtils.nullToBlank(oapiUserGetResponse.getMobile());
        String avatar = StringUtils.nullToBlank(oapiUserGetResponse.getAvatar());
        String hireDate = TimeUtils.datetimeToString(oapiUserGetResponse.getHiredDate());
        String position = oapiUserGetResponse.getPosition();
        bizLogger.info("ding user's name=" + name + ", email=" + email + ",mobile=" + mobile + ",avatar=" +
                avatar + ", hireDate=" + hireDate + ",position=" + position);
        String dingSSOUrl =  elafsHost + "/m/ding/sso.cobo";
        HashMap<String, String> ssoParam = new HashMap<>();
        ssoParam.put("name", name);
        ssoParam.put("email", email);
        ssoParam.put("mobile", mobile);
        ssoParam.put("avatar", avatar);
        ssoParam.put("hireDate", hireDate);
        ssoParam.put("domain_id", domain_id);
        ssoParam.put("userId", userId);
        ssoParam.put("site", domainName);
        String ssoResponse = HttpClientUtil.doPost(dingSSOUrl, ssoParam, "UTF-8");
        bizLogger.info(ssoResponse);
        return ssoResponse;
    }

    /**
     * 同步钉钉组织
     *
     */
    @RequestMapping(value = "/ding/rsync", method = RequestMethod.GET)
    public String rsyncOrg() throws ApiException {
        String rsyncOrgUrl =  elafsHost + "/m/api/dingOrg.cobo";
        HashMap<String, String> rsyncOrgParam = new HashMap<>();
        rsyncOrgParam.put("site", domainName);
        rsyncOrgParam.put("param", "rsync");
        String ssoResponse = HttpClientUtil.doPost(rsyncOrgUrl, rsyncOrgParam, "UTF-8");
        String msgs = "钉钉组织架构更新失败";
        try {
            JSONObject confiJson = (JSONObject) JSON.parse(ssoResponse);
            msgs = String.valueOf(confiJson.get("msg"));
        } catch (Exception e) {
            bizLogger.info(e.getMessage());
        }
        return msgs;
    }

    /**
     * 获取用户姓名
     *
     * @param accessToken
     * @param userId
     * @return
     */
    private OapiUserGetResponse getUserName(String accessToken, String userId) {
        try {
            DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_USER_GET);
            OapiUserGetRequest request = new OapiUserGetRequest();
            request.setUserid(userId);
            request.setHttpMethod("GET");
            OapiUserGetResponse response = client.execute(request, accessToken);
            bizLogger.info(JSON.toJSONString(response));
            return response;
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        }
    }
}


