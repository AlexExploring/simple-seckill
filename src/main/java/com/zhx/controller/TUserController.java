package com.zhx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhx.pojo.TUser;
import com.zhx.service.TUserService;
import com.zhx.utils.MD5Util;
import com.zhx.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class TUserController {

    @Autowired
    private TUserService tUserService;

    /**
     * 用户信息-用于测试
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public RespBean info(TUser user) {
        return RespBean.success(user);
    }

    /**
     * 用于批量生成用户
     */
    @GetMapping("/createUser/{count}")
    public void CreateUser(@PathVariable Integer count) throws IOException {
        List<TUser> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            TUser tUser = new TUser();
            tUser.setId(13000000000L + i);
            tUser.setNickname("user" + i);
            tUser.setSalt("1a2b3c4d");
            //tUser.setPassword(MD5Util.inputPassToDBPass("123456",tUser.getSalt()));
            tUser.setPassword("b7797cce01b4b131b433b6acf4add449");
            users.add(tUser);
        }
        //将生成的用户批量插入到数据库中
        tUserService.saveBatch(users);

        users = tUserService.list();

        //登录，生成UserTicket
        String urlString = "http://localhost:8080/login/doLogin";
        File file = new File("D:\\jmeter-test\\config.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.seek(0);
        for (int i = 0; i < users.size(); i++) {
            TUser tUser = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            String params = "mobile=" + tUser.getId() + "&password="+MD5Util.inputPassToFromPass("123456");
            outputStream.write(params.getBytes());
            outputStream.flush();
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                byteArrayOutputStream.write(buff, 0, len);
            }
            inputStream.close();
            byteArrayOutputStream.close();
            String respone = new String(byteArrayOutputStream.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            RespBean respBean = mapper.readValue(respone, RespBean.class);

            String userTicket = (String) respBean.getObject();
            System.out.println("create userTicket:" + tUser.getId());
            String row = tUser.getId() + "," + userTicket;
            randomAccessFile.seek(randomAccessFile.length());
            randomAccessFile.write(row.getBytes());
            randomAccessFile.write("\r\n".getBytes());
            System.out.println("write to file :" + tUser.getId());
        }
        randomAccessFile.close();
        System.out.println();
    }
}
