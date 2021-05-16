package com.rns.web.edo.service.util;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;


public class EdoAwsUtil {
	
	private static Regions clientRegion = Regions.AP_SOUTH_1;
	
	private static AWSCredentials awsCreds = new BasicAWSCredentials(EdoPropertyUtil.getProperty(EdoPropertyUtil.AWS_KEY), EdoPropertyUtil.getProperty(EdoPropertyUtil.AWS_SECRET));
	
	private static AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(clientRegion)
            .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
            .build();
	
	public static void main(String[] args) throws IOException {
        String fileObjKeyName = "testFile2";
        String fileName = "F:\\home\\service\\questionData\\894322_solution.png.png";

        //uploadToAws(fileObjKeyName, fileName);
        
        videoProctoringFiles("5795036");
    }

	public static String uploadToAws(String fileObjKeyName, String filePath, InputStream content, String contentType, String folderName) {
		try {
        	
            String bucketName = "edofox/" + folderName;
            
            //This code expects that you have AWS credentials set up per:
            // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
            
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
	
	public static List<String> videoProctoringFiles(String scheduleId) {
		ListObjectsV2Request req = new ListObjectsV2Request().withBucketName("edofox").withPrefix("impartus/" + scheduleId)/*.withDelimiter(DELIMITER)*/;
		ListObjectsV2Result listing = s3Client.listObjectsV2(req);
		/*for (String commonPrefix : listing.getCommonPrefixes()) {
		        System.out.println(commonPrefix);
		}*/
		List files = new ArrayList<String>();
		for (S3ObjectSummary summary: listing.getObjectSummaries()) {
		    //System.out.println(summary.getKey());
		    files.add(EdoPropertyUtil.getProperty(EdoPropertyUtil.AWS_URL) + summary.getKey());
		}
		return files;
	}

}