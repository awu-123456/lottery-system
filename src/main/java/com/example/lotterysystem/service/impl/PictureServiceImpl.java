package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.PictureService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class PictureServiceImpl implements PictureService {
    @Value("${pic.local-path}")
    private String localPath;

    @Override
    public String savePicture(MultipartFile multipartFile) {
        File dir = new File(localPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = multipartFile.getOriginalFilename();
        assert filename != null;
        String suffix = filename.substring(filename.lastIndexOf("."));
        filename = UUID.randomUUID() + suffix;

        try {
            multipartFile.transferTo(new File(localPath + "/" + filename));
        } catch (IOException e) {
            throw new ServiceException(ServiceErrorCodeConstants.PIC_UPLOAD_ERROR);
        }

        return filename;
    }
}
