package com.rns.web.edo.service.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.api.client.util.Data;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.rns.web.edo.service.domain.EdoStudent;
import com.rns.web.edo.service.domain.EdoStudentFirebase;
import com.rns.web.edo.service.domain.EdoTest;

public class EdoFirebaseUtil {
	
	static FirebaseDatabase database = null;
	private static Firestore db;
	private static FirebaseApp app;
	
	static {
		// Fetch the service account key JSON file contents
		FileInputStream serviceAccount;
		try {
			/*serviceAccount = new FileInputStream("/home/service/properties/edofox-key.json");
			// Initialize the app with a service account, granting admin privileges
			
			GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("/home/service/properties/edofox-key.json"));
			
			FirebaseOptions options = new FirebaseOptions.Builder()
				    .setCredentials(credentials)
				    .setDatabaseUrl("https://pvivekanand-android-app-devenv.firebaseio.com")
				    .build();
			
			Collection<String> scopes = new ArrayList<String>();
			scopes.add("https://www.googleapis.com/auth/cloud-platform");
			scopes.add("https://www.googleapis.com/auth/datastore");
			scopes.add("https://www.googleapis.com/auth/firebase");
			System.out.println(scopes);
			credentials = credentials.createScoped(scopes);
			
			FirestoreOptions options2 = FirestoreOptions.newBuilder()
			        .setProjectId("pvivekanand-android-app-devenv")
			        .setCredentials(credentials)
			        .setDatabaseId("(default)")
			        .setTimestampsInSnapshotsEnabled(true)
			        .build();
			
			app = FirebaseApp.initializeApp(options);
			database = FirebaseDatabase.getInstance();
			db = options2.getService();
			System.out.println(credentials.getRequestMetadata());*/
			
			
			serviceAccount = new FileInputStream("/home/service/properties/edofox-key.json");
			GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
			
			FirestoreOptions options = 
					  FirestoreOptions.newBuilder()
					  .setTimestampsInSnapshotsEnabled(true)
					  //.setCredentials(credentials)
					  .setProjectId("pvivekanand-android-app-devenv")
					  .setDatabaseId("(default)")
					  .build();
			
			db = options.getService();
			/*FirebaseOptions options = new FirebaseOptions.Builder()
			    .setCredentials(credentials)
			    .build();*/
			//FirebaseApp.initializeApp(options);

			//db = FirestoreClient.getFirestore();
			System.out.println("Firebase initialized!");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void updateStudent(EdoStudent student, String instituteId) {
		try {
			EdoStudentFirebase studentFirebase = new EdoStudentFirebase();
			new NullAwareBeanUtils().copyProperties(studentFirebase, student);
			CollectionReference studentsCollection = db.collection("students");
			ApiFuture<QuerySnapshot> docRef = studentsCollection.whereEqualTo("phone", student.getPhone()).whereEqualTo("instituteId", instituteId).get();
			List<QueryDocumentSnapshot> documents = docRef.get().getDocuments();
			
			if(CollectionUtils.isNotEmpty(documents)) {
				for (QueryDocumentSnapshot document : documents) {
					Map<String, Object> request = prepareStudentRequest(student);
					ApiFuture<WriteResult> result = document.getReference().set(request, SetOptions.merge());
					LoggingUtil.logMessage("Updated student : " + result.get().getUpdateTime() + " name=> " + student.getName());
				}
			} else {
				student.setInstituteId(instituteId);
				Map<String, Object> request = prepareStudentRequest(student);
				ApiFuture<DocumentReference> result = studentsCollection.add(request);
				LoggingUtil.logMessage("Update student : " + result.get() + " name=> " + student.getName());
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
	}

	private static Map<String, Object> prepareStudentRequest(EdoStudent student) {
		Map<String, Object> request = new HashMap<String, Object>();
		if(StringUtils.isNotBlank(student.getName())) {
			request.put("Name", student.getName());
		}
		if(StringUtils.isNotBlank(student.getEmail())) {
			request.put("email", student.getEmail());
		}
		if(StringUtils.isNotBlank(student.getPhone())) {
			request.put("StudentContact", student.getPhone());
		}
		if(StringUtils.isNotBlank(student.getRollNo())) {
			request.put("RollNo", student.getRollNo());
		}
		if(StringUtils.isNotBlank(student.getParentMobileNo())) {
			request.put("ParentContact", student.getParentMobileNo());
		}
		if(StringUtils.isNotBlank(student.getProfilePic())) {
			request.put("profilePicURL", student.getProfilePic());
		}
		if(StringUtils.isNotBlank(student.getInstituteId())) {
			request.put("instituteId", student.getInstituteId());
		}
		return request;
	}
	
	/*public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		EdoStudent student = new EdoStudent();
		student.setName("Ajinkya Kulkarni");
		student.setEmail("ajinkyashiva2@gmail.com");
		student.setPhone("+919923283604");
		student.setRollNo("123456");
		updateStudent(student, "7V0l67417NlGshXzCtY6");
		System.out.println("Done!");
		
	}*/
	
	public static void updateStudentResult(EdoStudent student, EdoTest test, String instituteId) {
		try {
			CollectionReference studentsCollection = db.collection("students");
			ApiFuture<QuerySnapshot> docRef = studentsCollection.whereEqualTo("phone", student.getPhone()).whereEqualTo("instituteId", instituteId).get();
			List<QueryDocumentSnapshot> documents = docRef.get().getDocuments();
			
			if(CollectionUtils.isNotEmpty(documents)) {
				for (QueryDocumentSnapshot document : documents) {
					CollectionReference examsCollection = document.getReference().collection("Exams");
					ApiFuture<QuerySnapshot> apiFuture = examsCollection.whereEqualTo("testId", test.getId()).get();
					if(CollectionUtils.isNotEmpty(apiFuture.get().getDocuments())) {
						for(QueryDocumentSnapshot examDocument : apiFuture.get().getDocuments()) {
							Map<String, Object> request = getExamResult(student, test);
							examDocument.getReference().set(request, SetOptions.merge());
						}
					} else {
						ApiFuture<DocumentReference> ref = examsCollection.add(getExamResult(student, test));
						LoggingUtil.logMessage("Added exam result " + ref.get() + " for student " + student.getName());
					}
					//ApiFuture<WriteResult> result = apiFuture;
					//LoggingUtil.logMessage("Updated student : " + result.get().getUpdateTime() + " name=> " + student.getName());
				}
			} else {
				LoggingUtil.logMessage("Student not found =>" + student.getName());
			}
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
		}
		
	}

	private static Map<String, Object> getExamResult(EdoStudent student, EdoTest test) {
		Map<String, Object> request = new HashMap<String, Object>();
		request.put("TestName", test.getName());
		request.put("dateAdded", new Date());
		request.put("examDate", test.getStartDate());
		request.put("examID", test.getFirebaseId());
		request.put("testId", test.getId());
		request.put("studentId", student.getId());
		request.put("score", test.getScore());
		request.put("total", test.getTotalMarks());
		if(student.getAnalysis() != null) {
			request.put("rank", student.getAnalysis().getRank());
			request.put("appearedStudents", student.getAnalysis().getTotalStudents());
		}
		return request;
	}

}