package com.atguigu.gmall.manager.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author wangyanjie
 * @create 2018-08-26 - 17:11
 */
@RestController
public class FileUploadController {

    //从配置文件中取值  注意：必须是在spring容器中才能使用
    @Value("${fileServer.url}")

    // fileUrl = http://192.168.67.204
    private String fileUrl;

    @RequestMapping(value = "fileUpload",method = RequestMethod.POST)
    public String fileUpLoad(@RequestParam("file") MultipartFile file) throws IOException, MyException {

        String imgUrl = fileUrl;
        if(file != null){
            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer,null);
            //获取文件名称
            String fileName = file.getOriginalFilename();
            //获取后缀名
            String extName = StringUtils.substringAfterLast(fileName, ".");
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            imgUrl = fileUrl;
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                imgUrl+="/"+path;
                System.out.println("imgUrl:"+imgUrl);
            }
        }

        //return "https://m.360buyimg.com/babel/jfs/t5137/20/1794970752/352145/d56e4e94/591417dcN4fe5ef33.jpg";
        return imgUrl;
    }
}
