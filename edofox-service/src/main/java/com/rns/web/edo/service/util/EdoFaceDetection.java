package com.rns.web.edo.service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.Image;

public class EdoFaceDetection {
	
	static Float similarityThreshold = 70F;
	static AmazonRekognition rekognitionClient;
	
	static {
		Regions clientRegion = Regions.AP_SOUTH_1;
		//This code expects that you have AWS credentials set up per:
        // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html
    	AWSCredentials awsCreds = new BasicAWSCredentials(EdoPropertyUtil.getProperty(EdoPropertyUtil.AWS_KEY), EdoPropertyUtil.getProperty(EdoPropertyUtil.AWS_SECRET));
		

		rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(clientRegion)
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
	}

	public static void main(String[] args) throws Exception {
		
		String sourceImage = "F:\\Resoneuronance\\Edofox\\Document\\Director_Pic.jpg";
		String targetImage = "F:\\Resoneuronance\\Edofox\\Document\\logo.jpg";
		//String targetImage = "H:\\Engagement Photos\\Photographer\\IMG_0412.jpg";
		ByteBuffer sourceImageBytes = null;
		ByteBuffer targetImageBytes = null;
		

		// Load source and target images and create input parameters
		try {
			InputStream inputStream = new FileInputStream(new File(sourceImage));
			sourceImageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
		} catch (Exception e) {
			System.out.println("Failed to load source image " + sourceImage);
			System.exit(1);
		}
		try {
			InputStream inputStream = new FileInputStream(new File(targetImage));
			targetImageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
		} catch (Exception e) {
			System.out.println("Failed to load target images: " + targetImage);
			System.exit(1);
		}

		compareFaceImages(sourceImageBytes, targetImageBytes);
	}

	public static float compareFaceImages(ByteBuffer sourceImageBytes, ByteBuffer targetImageBytes) {
		Image source = new Image().withBytes(sourceImageBytes);
		Image target = new Image().withBytes(targetImageBytes);

		CompareFacesRequest request = new CompareFacesRequest().withSourceImage(source).withTargetImage(target).withSimilarityThreshold(similarityThreshold);

		// Call operation
		CompareFacesResult compareFacesResult = rekognitionClient.compareFaces(request);

		// Display results
		List<CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
		float score = 0f;
		for (CompareFacesMatch match : faceDetails) {
			ComparedFace face = match.getFace();
			BoundingBox position = face.getBoundingBox();
			System.out.println("Face at " + position.getLeft().toString() + " " + position.getTop() + " matches with " + match.getSimilarity().toString()
					+ "% confidence.");
			score = match.getSimilarity();
			break;

		}
		List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();
		if(CollectionUtils.isNotEmpty(uncompared)) {
			System.out.println("There was " + uncompared.size() + " face(s) that did not match");
			return score;
		} else {
			return score;
		}

	}

}
