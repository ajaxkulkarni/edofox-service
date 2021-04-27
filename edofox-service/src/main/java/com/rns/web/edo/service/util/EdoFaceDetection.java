package com.rns.web.edo.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Instance;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.Parent;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoTest;
import com.rns.web.edo.service.domain.ext.EdoFaceScore;
import com.rns.web.edo.service.domain.jpa.EdoProctorImages;
import com.rns.web.edo.service.domain.jpa.EdoTestStatusEntity;

public class EdoFaceDetection implements Runnable {
	
	static Float similarityThreshold = 70F;
	static AmazonRekognition rekognitionClient;
	
	private SessionFactory sessionFactory;
	private List<EdoStudent> students;
	private EdoTest test;
	private Integer jobNo;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void setStudents(List<EdoStudent> students) {
		this.students = students;
	}
	
	public void setJobNo(Integer jobNo) {
		this.jobNo = jobNo;
	}
	
	public void setTest(EdoTest test) {
		this.test = test;
	}
	
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
		
		/*String sourceImage = "F:\\Resoneuronance\\Edofox\\Document\\Director_Pic.jpg";
		String targetImage = "F:\\Resoneuronance\\Edofox\\Document\\logo.jpg";
		//String largeImage = "H:\\Engagement Photos\\IMG_0182.jpg";
		//String targetImage = "H:\\Engagement Photos\\compressed.jpg";
		//EdoImageUtil.compressImage(new FileInputStream(targetImage), targetImage, 0.5f);
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

		System.out.println("Score is " + compareFaceImages(sourceImageBytes, targetImageBytes).getRemarks());*/
		
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		System.out.println(list.subList(0, 3));
	}
	
	public static void detectObjects(ByteBuffer bytes) {
		DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(bytes))
                .withMaxLabels(10).withMinConfidence(75F);

        try {
            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();

            System.out.println("Detected labels ");
            for (Label label : labels) {
                System.out.println("Label: " + label.getName());
                System.out.println("Confidence: " + label.getConfidence().toString() + "\n");

                List<Instance> instances = label.getInstances();
                System.out.println("Instances of " + label.getName());
                if (instances.isEmpty()) {
                    System.out.println("  " + "None");
                } else {
                    for (Instance instance : instances) {
                        System.out.println("  Confidence: " + instance.getConfidence().toString());
                        System.out.println("  Bounding box: " + instance.getBoundingBox().toString());
                    }
                }
                System.out.println("Parent labels for " + label.getName() + ":");
                List<Parent> parents = label.getParents();
                if (parents.isEmpty()) {
                    System.out.println("  None");
                } else {
                    for (Parent parent : parents) {
                        System.out.println("  " + parent.getName());
                    }
                }
                System.out.println("--------------------");
                System.out.println();
               
            }
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }
	}

	public static EdoFaceScore compareFaceImages(ByteBuffer sourceImageBytes, ByteBuffer targetImageBytes) {
		EdoFaceScore fscore = new EdoFaceScore();
		try {
			
			Image source = new Image().withBytes(sourceImageBytes);
			Image target = new Image().withBytes(targetImageBytes);

			CompareFacesRequest request = new CompareFacesRequest().withSourceImage(source).withTargetImage(target).withSimilarityThreshold(similarityThreshold);

			// Call operation
			CompareFacesResult compareFacesResult = rekognitionClient.compareFaces(request);

			// Display results
			List<CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
			float score = 0f;
			for (CompareFacesMatch match : faceDetails) {
				/*ComparedFace face = match.getFace();
				BoundingBox position = face.getBoundingBox();
				System.out.println("Face at " + position.getLeft().toString() + " " + position.getTop() + " matches with " + match.getSimilarity().toString()
						+ "% confidence.");*/
				score = match.getSimilarity();
				break;

			}
			
			//detectObjects(targetImageBytes);
			
			List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();
			if(CollectionUtils.isNotEmpty(uncompared)) {
				//System.out.println("There was " + uncompared.size() + " face(s) that did not match");
				fscore.setRemarks("Multiple people found");
				fscore.setScore(0f);
				return fscore;
			} else {
				fscore.setScore(score);
				return fscore;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fscore.setScore(0f);
			fscore.setRemarks("Invalid images");
			return fscore;
		}
		
	}

	public void run() {
		Session session = null;
		try {
			session = this.sessionFactory.openSession();
			calculateProctoringScore(session);
		} catch (Exception e) {
			
		} finally {
			CommonUtils.closeSession(session);
		}
		
	}

	public void calculateProctoringScore(Session session) throws MalformedURLException, IOException {
		if(CollectionUtils.isNotEmpty(students) && test != null) {
			LoggingUtil.logMessage(" #### Started proctoring JOB " + jobNo + " for test " + test.getId() + " and students " + students.size(), LoggingUtil.saveTestLogger);
			Transaction tx = session.beginTransaction();
			for(EdoStudent student: students) {
				List<EdoProctorImages> images = session.createCriteria(EdoProctorImages.class)
														.add(Restrictions.eq("testId", test.getId()))
														.add(Restrictions.eq("studentId", student.getId())).list();
				if(CollectionUtils.isNotEmpty(images)) {
					StringBuilder remarks = new StringBuilder();
					for(EdoProctorImages img: images) {
						if(StringUtils.isNotBlank(img.getImageUrl()) && StringUtils.isNotBlank(student.getProctorImageRef())) {
							//FileInputStream fileInputStream = new FileInputStream(sourceImage);
							InputStream sourceStream = new URL(student.getProctorImageRef()).openStream();
							ByteBuffer sourceImageBytes = ByteBuffer.wrap(IOUtils.toByteArray(sourceStream));
							InputStream targetStream = new URL(img.getImageUrl()).openStream();
							ByteBuffer targetImageBytes = ByteBuffer.wrap(IOUtils.toByteArray(targetStream));
							EdoFaceScore score = compareFaceImages(sourceImageBytes, targetImageBytes);
							if(score != null) {
								img.setScore(score.getScore());
								img.setRemarks(score.getRemarks());
								if(StringUtils.isNotBlank(score.getRemarks()) && !StringUtils.contains(remarks, score.getRemarks())) {
									remarks.append(score.getRemarks()).append(",");
								}
							} else {
								img.setScore(0f);
							}
							CommonUtils.closeStream(sourceStream);
							CommonUtils.closeStream(targetStream);
							
						}
					}
					if(StringUtils.isNotBlank(remarks.toString())) {
						List<EdoTestStatusEntity> entity = session.createCriteria(EdoTestStatusEntity.class)
																.add(Restrictions.eq("testId", test.getId()))
																.add(Restrictions.eq("studentId", student.getId())).list();
						if(CollectionUtils.isNotEmpty(entity)) {
							entity.get(0).setProctoringRemarks(remarks.toString());
						}
						
					}
					LoggingUtil.logMessage("Calculated proctoring score for " + student.getId() + " with " + remarks, LoggingUtil.saveTestLogger);
				}
				
			}
			tx.commit();
			LoggingUtil.logMessage(" #### Completed proctoring JOB " + jobNo + " for test " + test.getId() + " and students " + students.size(), LoggingUtil.saveTestLogger);
			
		}
	}

}
