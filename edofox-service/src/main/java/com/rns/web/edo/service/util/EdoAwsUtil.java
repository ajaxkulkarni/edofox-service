package com.rns.web.edo.service.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;


public class EdoAwsUtil {
	
	public static void main(String[] args) throws IOException {
        String fileObjKeyName = "testFile2";
        String fileName = "F:\\home\\service\\questionData\\894322_solution.png.png";

        //uploadToAws(fileObjKeyName, fileName);
    }

	public static String uploadToAws(String fileObjKeyName, String filePath, InputStream content, String contentType, String folderName) {
		try {
        	Regions clientRegion = Regions.AP_SOUTH_1;
            String bucketName = "edofox/" + folderName;
            
            //This code expects that you have AWS credentials set up per:
            // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
        	AWSCredentials awsCreds = new BasicAWSCredentials(EdoPropertyUtil.getProperty(EdoPropertyUtil.AWS_KEY), EdoPropertyUtil.getProperty(EdoPropertyUtil.AWS_SECRET));
			
        	AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();
            
            // Upload a text string as a new object.
            //s3Client.putObject(bucketName, stringObjKeyName, "Uploaded String Object");
            //s3Client.put

            // Upload a file as a new object with ContentType and title specified.
            PutObjectRequest request;
            if(content != null) {
            	request = new PutObjectRequest(bucketName, fileObjKeyName, content, null);
            } else {
            	request = new PutObjectRequest(bucketName, fileObjKeyName, new File(filePath));
            }
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.addUserMetadata("title", fileObjKeyName);
            request.setMetadata(metadata);
            s3Client.putObject(request);
            return EdoPropertyUtil.getProperty(EdoPropertyUtil.AWS_URL) + folderName + "/" + fileObjKeyName; 
        } catch (Exception e) {
        	LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
        }
		
		return null;
	}

}
