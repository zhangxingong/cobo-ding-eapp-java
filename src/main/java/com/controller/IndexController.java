package com.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.config.URLConstant;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.taobao.api.ApiException;
import com.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
        bizLogger.info("welcome");
        return "welcome";
    }

    /**
     * 钉钉用户登录，显示当前登录用户的userId和名称
     *
     * @param requestAuthCode 免登临时code
     */
    @RequestMapping(value = "/ding/login", method = RequestMethod.POST)
    @ResponseBody
    public String dingLogin(@RequestParam(value = "authCode") String requestAuthCode) throws Exception {
        String dingConfiUrl = elafsHost + "/m/api/dingConfig.cobo";
        String domain_id = "";
        String sso_secret = "";
        String accessToken = "";
        ServiceResult dingCofigResult = getDingCofig(dingConfiUrl, domainName, requestAuthCode);
        if (dingCofigResult.isSuccess()) {
            HashMap<String, String> resultMap = (HashMap<String, String>) dingCofigResult.getResult();
            domain_id = resultMap.get("domain_id");
            accessToken = resultMap.get("accessToken");
            sso_secret = resultMap.get("sso_secret");
        }else{
            return JSON.toJSONString(dingCofigResult);
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
        String encryptdata = email.trim() + "|" + System.currentTimeMillis();
        String sign = CryptoUtils.encryptBase64(encryptdata, sso_secret);
        bizLogger.info("ding user's name=" + name + ", email=" + email + ",mobile=" + mobile + ",avatar=" +
                avatar + ", hireDate=" + hireDate + ",position=" + position);

        String dingSSOUrl =  elafsHost + "/m/ding/sso/login.cobo";
        HashMap<String, String> ssoParam = new HashMap<>();
        ssoParam.put("name", name);
        ssoParam.put("email", email);
        ssoParam.put("mobile", mobile);
        ssoParam.put("avatar", avatar);
        ssoParam.put("hireDate", hireDate);
        ssoParam.put("domain_id", domain_id);
        ssoParam.put("userId", userId);
        ssoParam.put("site", domainName);
        ssoParam.put("sign", sign);
        String ssoResponse = HttpClientUtil.doPost(dingSSOUrl, ssoParam, "UTF-8");
        bizLogger.info(ssoResponse);
        return ssoResponse;
    }

    /**
     * 同步钉钉组织
     *
     */
    @RequestMapping(value = "/ding/rsync", method = RequestMethod.GET)
    public String rsyncOrg() throws Exception {
        //获取accessToken,注意正是代码要有异常流处理
        String accessToken = "";
        String domain_id = "";
        String sso_secret = "";
        String dingConfiUrl = elafsHost + "/m/api/dingConfig.cobo";
        ServiceResult dingCofigResult = getDingCofig(dingConfiUrl, domainName, "");
        if (dingCofigResult.isSuccess()) {
            HashMap<String, String> resultMap = (HashMap<String, String>) dingCofigResult.getResult();
            accessToken = resultMap.get("accessToken");
            domain_id = resultMap.get("domain_id");
            sso_secret = resultMap.get("sso_secret");
        }else{
            return dingCofigResult.getMessage();
        }
        JSONObject result = new JSONObject();
        JSONObject parent  = new JSONObject();
        result.put("dingOrg", parent);
        result.put("domain_id", domain_id);
        parent.put("id", "1");
        parent.put("parentId", "");
        parent.put("name", "组织根节点");
        //获取钉钉组织结构
        updateChildrenOrg(accessToken, "1", result, parent); //1代表跟部门
        String jsonParam = JSON.toJSONString(result);
        bizLogger.info(jsonParam);

        String rsyncOrgUrl =  elafsHost + "/m/api/dingOrg.cobo";
        HashMap<String, String> rsyncOrgParam = new HashMap<>();
        rsyncOrgParam.put("param", "rsync");
        rsyncOrgParam.put("site", domainName);
        rsyncOrgParam.put("dingOrg", jsonParam);
        String ssoResponse = HttpClientUtil.doPost(rsyncOrgUrl, rsyncOrgParam, "UTF-8");
        String msgs = "钉钉组织架构更新失败";
        try {
            JSONObject confiJson = (JSONObject) JSON.parse(ssoResponse);
            msgs = String.valueOf(confiJson.get("msg"));
        } catch (Exception e) {
            bizLogger.info(e.getMessage());
        }
        bizLogger.info(msgs);
        return msgs;
    }

    /**
     * 查看同步组织结果
     *
     */
    @RequestMapping(value = "/ding/rsync/show", method = RequestMethod.GET)
    public String vieRsyncOrg() throws Exception {
        String rsyncOrgUrl =  elafsHost + "/m/api/dingOrg.cobo";
        HashMap<String, String> rsyncOrgParam = new HashMap<>();
        rsyncOrgParam.put("site", domainName);
        String ssoResponse = HttpClientUtil.doPost(rsyncOrgUrl, rsyncOrgParam, "UTF-8");
        return ssoResponse;
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

    private Map<String, String> getAppKeySecret(String secret, String decryptKey) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        String param = CryptoUtils.decryptBase64(secret, decryptKey);
        String app_key = StringUtils.nullToBlank(param.substring(0, param.indexOf("|"))).trim();
        String app_secret = StringUtils.nullToBlank(param.substring(param.indexOf("|") + 1));
        resultMap.put("app_key", app_key);
        resultMap.put("app_secret", app_secret);
        return resultMap;
    }

    private ServiceResult getDingCofig(String dingConfiUrl, String domainName, String requestAuthCode) throws Exception {
        HashMap<String, String> configParam = new HashMap<>();
        configParam.put("site", domainName);
        String secret = "";
        String domain_id = "";
        String sso_secret = "";
        try {
            String configResponse = HttpClientUtil.doPost(dingConfiUrl, configParam, "UTF-8");
            JSONObject confiJson = (JSONObject) JSON.parse(configResponse);
            Integer status = confiJson.getInteger("status");
            if (confiJson != null && status != null && status.intValue()==0) {
                secret = String.valueOf(confiJson.get("secret"));
                domain_id = String.valueOf(confiJson.get("domain_id"));
                sso_secret = String.valueOf(confiJson.get("sso_secret"));
            } else {
                bizLogger.info("获取配置信息失败!");
                return ServiceResult.failure("-1", "获取配置信息失败!" + requestAuthCode);
            }
        } catch (Exception e) {
            bizLogger.info("访问配置信息失败!");
            return ServiceResult.failure("-1", "访问配置信息失败!" + requestAuthCode);
        }

        String accessToken = AccessTokenUtil.getToken(getAppKeySecret(secret, sso_secret));
        if (StringUtils.isBlank(accessToken)) {
            bizLogger.info("accessToken 获取失败");
            return ServiceResult.failure("-1", "accessToken 获取失败!" + requestAuthCode);
        }
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("secret", secret);
        paramMap.put("domain_id", domain_id);
        paramMap.put("sso_secret", sso_secret);
        paramMap.put("accessToken", accessToken);
        return ServiceResult.success(paramMap);
    }

    private static void updateChildrenOrg(String accessToken, String parent_id, JSONObject result, JSONObject parent) {
        JSONObject current = parent;
        List<OapiDepartmentListResponse.Department> listDepartment = listDepartment(accessToken, false, parent_id);
        if (listDepartment != null)
            for (OapiDepartmentListResponse.Department department : listDepartment) {
                String name = StringUtils.nullToBlank(department.getName());
                Long parentid = department.getParentid();
                Long ding_id = department.getId();
                JSONArray arr = current.getJSONArray("children");
                if (arr == null) {
                    arr = new JSONArray();
                    current.put("children", arr);
                }
                JSONObject child  = new JSONObject();
                arr.add(child);
                child.put("id", ding_id);
                child.put("parentId", parentid);
                child.put("name", name);

                updateChildrenOrg(accessToken, String.valueOf(ding_id), result, child);
            }
        if (CollectionUtils.isEmpty(listDepartment)) {//叶子部门
            long offset = 0;
            long size = 100;
            updateOrgUser(accessToken, parent_id, offset, size, result, parent);
        }
    }

    private static void updateOrgUser(String accessToken, String parent_id, long offset, long size, JSONObject result, JSONObject parent){
        JSONObject jo = parent;
        JSONArray arr  = new JSONArray();
        jo.put("children", arr);
        OapiUserListResponse oapiResponse = viewDepartment(accessToken, Long.valueOf(parent_id).longValue(), offset, size);
        if (oapiResponse == null)
            return;
        List<OapiUserListResponse.Userlist> userlist = oapiResponse.getUserlist();
        if (userlist != null)
            for (OapiUserListResponse.Userlist userlistObj : userlist) {
                String userid = StringUtils.nullToBlank(userlistObj.getUserid());
                String name = StringUtils.nullToBlank(userlistObj.getName());
                String email = StringUtils.nullToBlank(userlistObj.getEmail());
                String mobile = StringUtils.nullToBlank(userlistObj.getMobile());
                String avatar = StringUtils.nullToBlank(userlistObj.getAvatar());
                String hireDate = TimeUtils.datetimeToString(userlistObj.getHiredDate());
                JSONObject child = new JSONObject();
                arr.add(child);
                child.put("userid", userid);
                child.put("name", name);
                child.put("email", email);
                child.put("mobile", mobile);
                child.put("avatar", avatar);
                child.put("hireDate", hireDate);
            }

        if (oapiResponse.getHasMore()) {
           updateOrgUser(accessToken, parent_id, offset + size, size, result, jo);
        }
    }

    /**
     * 获取所有部门信息
     *
     * @param userId
     * @param accessToken
     * @param org_id
     * @return
     */
    private static List<OapiDepartmentListResponse.Department> listDepartment(String accessToken, boolean fetch_child, String org_id) {
        try {
            DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_DEPARTMENT_LIST);
            OapiDepartmentListRequest request = new OapiDepartmentListRequest();
            request.setHttpMethod("GET");
            request.setId(org_id);
            request.setFetchChild(fetch_child);
            OapiDepartmentListResponse response = client.execute(request, accessToken);
            return response.getDepartment();
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取所有部门信息
     *
     * @param accessToken
     * @param userId
     * @return
     */
    private static OapiUserListResponse viewDepartment(String accessToken, long departmentId, long offset, long size) {
        try {
            DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_DEPARTMENT_view);
            OapiUserListRequest request = new OapiUserListRequest();
            request.setDepartmentId(departmentId);
            request.setOffset(offset);
            request.setSize(size);
            request.setOrder("entry_desc");
            request.setHttpMethod("GET");
            OapiUserListResponse response = client.execute(request, accessToken);
            return response;
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        }
    }
}


