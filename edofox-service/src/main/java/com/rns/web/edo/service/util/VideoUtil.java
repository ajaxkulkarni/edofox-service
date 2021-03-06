package com.rns.web.edo.service.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONObject;

import com.clickntap.vimeo.Vimeo;
import com.clickntap.vimeo.VimeoException;
import com.clickntap.vimeo.VimeoResponse;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.rns.web.edo.service.bo.api.EdoFile;
import com.rns.web.edo.service.domain.jpa.EdoLiveSession;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class VideoUtil {

	public static String FFMPEG_BIN = "F:\\Resoneuronance\\Setups\\ffmpeg-20200403-52523b6-win64-static\\bin\\";
	private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;



	// ffmpeg -i F:\home\service\videos\1\v1.mp4 -c copy -bsf:v h264_mp4toannexb
	// -f mpegts v1.ts
	// ffmpeg -i F:\home\service\videos\1\v1.webm -preset veryfast
	// F:\home\service\videos\1\v1.mp4
	// ffmpeg -f concat -i F:\home\service\videos\1\list.txt -c:v libvpx -c:a
	// libvorbis F:\home\service\videos\1\finalvideo.webm
	public static boolean mergeFiles(String folderLocation, String outputFolder) throws IOException, InterruptedException {
		//ffmpeg
		//String oldCommand = " -y -f concat -safe 0 -i {folderLocation}list.txt -c copy -c:v libvpx {outputLocation}";
		String commandString = "ffmpeg -y -f concat -safe 0 -i {folderLocation}list.txt -y -acodec copy -vcodec copy {outputLocation}";
		//String commandString = "ffmpeg -version";
		
		/*Process process = createProcess(folderLocation, commandString, outputFolder);
		
		int exitVal = process.waitFor();
		if (exitVal == 0) {
			LoggingUtil.logMessage("Video output Success at " + folderLocation);
			//System.exit(0);
			return true;
		} else {
			// abnormal...
			LoggingUtil.logMessage("Video output failed at " + folderLocation);
			//Try with old command
			Process processWithOldCommand = createProcess(folderLocation, oldCommand, outputFolder);
			int result = processWithOldCommand.waitFor();
			if(result == 0) {
				LoggingUtil.logMessage("Video output success by old command at " + folderLocation);
				return true;
			} else {
				LoggingUtil.logMessage("Video output failed again at " + folderLocation);
				return false;
			}
		}*/

		commandString = StringUtils.replace(commandString, "{folderLocation}", folderLocation);
		commandString = StringUtils.replace(commandString, "{outputLocation}", outputFolder);
		
		//LoggingUtil.logMessage("Running command with runtime .." + oldCommand);
		
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		// -- Linux --

		// Run a shell command
		/*processBuilder.command("ffmpeg", "-y", "-f", "concat", "-safe", "0","-i",
				folderLocation + "list.txt", "-c", "copy", "-c:v", "libvpx", outputFolder);*/
		
		processBuilder.command("ffmpeg", "-y", "-f", "concat", "-safe", "0","-i",
				folderLocation + "list.txt", "-y", "-acodec", "copy", "-vcodec", "copy", outputFolder);
		
		LoggingUtil.logMessage("Commands=>" + processBuilder.command(), LoggingUtil.videoLogger);

		// Run a shell script
		// processBuilder.command("path/to/hello.sh");

		// -- Windows --

		// Run a command
		// processBuilder.command("cmd.exe", "/c", "dir C:\\Users\\mkyong");

		// Run a bat file
		// processBuilder.command("C:\\Users\\mkyong\\hello.bat");

		try {

			Process process = processBuilder.start();

			StringBuilder output = new StringBuilder();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

			int exitVal = process.waitFor();
			if (exitVal == 0) {
				System.out.println("Success!");
				LoggingUtil.logMessage(output.toString(), LoggingUtil.videoLogger);
				LoggingUtil.logMessage("Video output success by command at " + folderLocation, LoggingUtil.videoLogger);
				return true;
			} else {
				// abnormal...
				LoggingUtil.logMessage(output.toString(), LoggingUtil.videoLogger);
				LoggingUtil.logMessage("Video output failed by command at " + folderLocation, LoggingUtil.videoLogger);
				//Run again with another command
				processBuilder.command("ffmpeg", "-y", "-f", "concat", "-safe", "0","-i",
						folderLocation + "list.txt", "-c", "copy", "-c:v", "libvpx", outputFolder);
				//-y -acodec copy -vcodec copy
				
				LoggingUtil.logMessage("Commands again=>" + processBuilder.command(), LoggingUtil.videoLogger);
				
				Process process2 = processBuilder.start();

				StringBuilder output2 = new StringBuilder();

				BufferedReader reader2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));

				String line2;
				while ((line2 = reader2.readLine()) != null) {
					output2.append(line2 + "\n");
				}

				int exitVal2 = process2.waitFor();
				if (exitVal2 == 0) {
					LoggingUtil.logMessage(output2.toString(), LoggingUtil.videoLogger);
					LoggingUtil.logMessage("Video output success by second command at " + folderLocation, LoggingUtil.videoLogger);
					return true;
				} else {
					LoggingUtil.logMessage(output2.toString(), LoggingUtil.videoLogger);
					LoggingUtil.logMessage("Video output failed by command at " + folderLocation, LoggingUtil.videoLogger);
				}
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static Process createProcess(String folderLocation, String commandString, String outputFolder) throws IOException {
		commandString = StringUtils.replace(commandString, "{folderLocation}", folderLocation);
		commandString = StringUtils.replace(commandString, "{outputLocation}", outputFolder);
		
		LoggingUtil.logMessage("Running command with runtime .." + commandString);
		Process process = Runtime.getRuntime().exec(commandString, null, new File(folderLocation));

		StringBuilder output = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line + "\n");
			System.out.println(line);
		}
		System.out.println(output);
		return process;
	}

	/*public static void combine(String path) throws IOException {
		FFmpeg ffmpeg = new FFmpeg(FFMPEG_BIN);
		FFprobe ffprobe = new FFprobe(FFMPEG_BIN);
		FFmpegBuilder builder = new FFmpegBuilder();
		builder.setInput(path + "list.txt").addOutput(path + "output_coded.mp4");
		
		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

		// Run a one-pass encode
		executor.createJob(builder).run();

	}*/
	
	//https://vimeo.com/408810998
	public static VimeoResponse uploadFile(String path, String videoName, String videoDesciption) throws IOException, VimeoException {
		
		LoggingUtil.logMessage("Uploading file at " + path + " to vimeo .. ", LoggingUtil.videoLogger);
		
		Vimeo vimeo = new Vimeo(EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_UPLOAD_KEY)); 
		
		//add a video
	    boolean upgradeTo1080 = true;
	    String videoEndPoint = vimeo.addVideo(new File(path), upgradeTo1080);
	    
	    //get video info
	    VimeoResponse info = vimeo.getVideoInfo(videoEndPoint);
	    LoggingUtil.logMessage("Vimeo Response => " + info.toString(), LoggingUtil.videoLogger);
	    
	    //edit video
	    //String name = "Test video from java";
	    //String desc = "Test video from java";
	    String license = ""; //see Vimeo API Documentation
	    String privacyView = "disable"; //see Vimeo API Documentation
	    String privacyEmbed = "whitelist"; //see Vimeo API Documentation
	    boolean reviewLink = false;
	    vimeo.updateVideoMetadata(videoEndPoint, videoName, videoDesciption, license, privacyView, privacyEmbed, reviewLink);
	    vimeo.addEmbedPreset(videoEndPoint, "edofox-live");
	    //add video privacy domain
	    //vimeo.addVideoPrivacyDomain(videoEndPoint, "localhost");
		String defaultDomain = EdoPropertyUtil.getProperty(EdoPropertyUtil.HOST_NAME);
		//if(StringUtils.isBlank(defaultDomain)) {
		defaultDomain = "test.edofox.com";
		//}
		LoggingUtil.logMessage("Adding domain to privacy " + defaultDomain, LoggingUtil.videoLogger);
		vimeo.addVideoPrivacyDomain(videoEndPoint, defaultDomain);
		
	    //delete video
	    //TODO vimeo.removeVideo(videoEndPoint);
	    return info;
	}
	
	public static EdoFile getDownloadUrl(String url, String fileName, Integer instituteId) {
		EdoFile result = new EdoFile();
		List<EdoFile> files = new ArrayList<EdoFile>();
		String vimeoKey = EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_UPLOAD_KEY);
		if(instituteId == 9) {
			//For reliance
			vimeoKey = EdoPropertyUtil.getProperty("reliance.video.key");
		} else if  (instituteId == 4 || instituteId == 1037) {
			//For shahu
			vimeoKey = EdoPropertyUtil.getProperty("shahu.video.key");
		}
		Vimeo vimeo = new Vimeo(vimeoKey); 
		try {
			String videoId = StringUtils.replace(url, "https://vimeo.com/", "");
			if(StringUtils.contains(videoId, "vimeo")) {
				videoId = StringUtils.replace(url, "https://player.vimeo.com/video/", "");
			}
			VimeoResponse resp = vimeo.get("https://api.vimeo.com/videos/" + videoId);
			if(resp != null && resp.getJson() != null) {
				System.out.println(resp);
				String link = "";
				if(!resp.getJson().has("download")) {
					if(instituteId == 9 || instituteId == 4) {
						LoggingUtil.logMessage("Not found! recursion..", LoggingUtil.videoLogger);
						return getDownloadUrl(url, fileName, 0);
					}
					return null;
				}
				org.json.JSONArray array = resp.getJson().getJSONArray("download");
				if( array.length() > 0)  {
					for(int i = 0; i < array.length(); i++) {
						org.json.JSONObject jsonObject = array.getJSONObject(i);
						if(StringUtils.isNotBlank(jsonObject.getString("link"))) {
							EdoFile file = new EdoFile();
							link = jsonObject.getString("link");
							if(jsonObject.get("height") != null && jsonObject.getDouble("height") > 0) {
								file.setHeight(new Float(jsonObject.getDouble("height")));
							}
							if(jsonObject.get("size") != null  && jsonObject.getDouble("size") > 0) {
								file.setSize(new Float(jsonObject.getDouble("size")));
							}
							file.setContentType("video/mp4");
							file.setDownloadUrl(link);
							files.add(file);
						}
					}
				}
				//Fetch HLS URL for adaptive streaming
				org.json.JSONArray filesArray = resp.getJson().getJSONArray("files");
				if( filesArray.length() > 0)  {
					for(int i = 0; i < filesArray.length(); i++) {
						org.json.JSONObject jsonObject = filesArray.getJSONObject(i);
						if(StringUtils.isNotBlank(jsonObject.getString("link"))) {
							String quality = jsonObject.getString("quality");
							if(StringUtils.isNotBlank(quality) && StringUtils.equals(quality, "hls")) {
								link = jsonObject.getString("link");
								result.setHlsUrl(link);
							}
						}
					}
				}
				//return link;
				//Sort files based on size
				Collections.sort(files, new Comparator<EdoFile>() {

					public int compare(EdoFile o1, EdoFile o2) {
						if(o1.getSize() != null && o2.getSize() != null) {
							if(o1.getSize() > o2.getSize()) {
								return -1;
							} else if (o1.getSize() < o2.getSize()) {
								return 1;
							} else {
								return 0;
							}
						}
						return 1;
					}
				});
			}
			
			if(files.size() > 3) {
				//Pick best 3 options from the list of versions
				List<EdoFile> bestVersions = new ArrayList<EdoFile>();
				int i = 0;
				int middleValue = (files.size() / 2);
				if(middleValue <= 0) {
					middleValue = 1;
				}
				for(EdoFile file: files) {
					if(i == 0 || i == (files.size() - 1)) {
						bestVersions.add(file);
					}
					if(middleValue == i) {
						bestVersions.add(file);
					}
					i++;
				}
				files = bestVersions;
			}
			result.setFileName(fileName);
			result.setDownloadUrl(files.get(files.size() - 1).getDownloadUrl());
			result.setVersions(files);
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.videoLogger);
		}
		return result;
	}
	
	
	public static Float downloadRecordedFile(Integer channelId, Integer sessionId) {
		FileOutputStream fileOutputStream = null;
		try {
			String urlStr = EdoPropertyUtil.getProperty(EdoPropertyUtil.RECORDED_URL) + URLEncoder.encode(channelId + "-" + sessionId + ".mp4", "UTF-8");
			LoggingUtil.logMessage("Downloading recorded file from .." + urlStr, LoggingUtil.videoLogger);
			URL url = new URL(urlStr);
			String folderLocation = EdoConstants.VIDEOS_PATH + sessionId;
			File folder = new File(folderLocation);
			if(!folder.exists()) {
				folder.mkdirs();
			}
			String outputFile = folderLocation + "/recorded.mp4";
			fileOutputStream = new FileOutputStream(outputFile);
	        /*ReadableByteChannel rbc = Channels.newChannel(url.openStream());
	        fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	        fileOutputStream.close();
	        rbc.close();*/
			
			return getFileLength(urlStr); 
			
		} catch (Exception e) {
			LoggingUtil.logError("Could not download file  for " + sessionId, LoggingUtil.videoLogger);
		} finally {
			if(fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

	private static Float getFileLength(String urlStr) throws IOException, ClientProtocolException {
		HttpHead get = new HttpHead(urlStr);
		 
		//Get http client
		CloseableHttpClient httpClient = getCloseableHttpClient();
		 
		//Execute HTTP method
		CloseableHttpResponse res = httpClient.execute(get);
		 
		LoggingUtil.logMessage("Response from url " + res.getStatusLine().getStatusCode(), LoggingUtil.videoLogger);
		//Verify response
		if(res.getStatusLine().getStatusCode() == 200) {
			Header lengthHeader = res.getFirstHeader("Content-Length");
			if(lengthHeader != null && lengthHeader.getValue() != null) {
				return new Float(lengthHeader.getValue());
			}
		}
		return 0f;
	}

	private static CloseableHttpClient getCloseableHttpClient() {
		CloseableHttpClient httpClient = null;
	    try {
	        httpClient = HttpClients.custom().
	                setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
	                setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy()
	                {
						public boolean isTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
							return true;
						}
	                }).build()).build();
	    } catch (KeyManagementException e) {
	    	e.printStackTrace();
	    } catch (NoSuchAlgorithmException e) {
	    	e.printStackTrace();
	    } catch (KeyStoreException e) {
	    	e.printStackTrace();
	    }
	    return httpClient;
	}
	
	public static String callVimeoApi(String url, JSONObject request, String methodType) throws JsonGenerationException, JsonMappingException, IOException {
		ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client client = Client.create(config);

		WebResource webResource = client.resource(url);
		LoggingUtil.logMessage("Calling VIMEO URL :" + url + " request:" + request, LoggingUtil.videoLogger);

		
		
		Builder header = webResource.type("application/json").header("Authorization", "bearer " + EdoPropertyUtil.getProperty(EdoPropertyUtil.VIDEO_UPLOAD_KEY));
		if(methodType != null) {
			//�X-HTTP-Method-Override�: �PUT�
			header.header("X-HTTP-Method-Override", methodType);
		}
		
		ClientResponse response = header.post(ClientResponse.class, request);

		if (response.getStatus() != 200) {
			LoggingUtil.logMessage("Failed in vimeo URL : HTTP error code : " + response.getStatus(), LoggingUtil.videoLogger);
		}
		String output = response.getEntity(String.class);
		LoggingUtil.logMessage("Output from vimeo URL : " + response.getStatus() + ".... \n " + output, LoggingUtil.videoLogger);
		return output;
	
	}
	
	public static void createLiveEvent(EdoLiveSession liveSession) {
		
		try {
			
			JSONObject request = new JSONObject();
			JSONObject upload = new JSONObject();
			upload.put("approach", "live");
			request.put("upload", upload);
			//Create video placeholder in vimeo
			String response = callVimeoApi("https://api.vimeo.com/me/videos", request, null);
			if(StringUtils.isNotBlank(response)) {
				//Get video link from response
				JSONObject apiResponse = new JSONObject(response);
				if(apiResponse != null) {
					String uri = apiResponse.getString("uri");
					LoggingUtil.logMessage("Video Link created as " + apiResponse.getString("link") + " and URI " + uri);
					JSONObject patchRequest = new JSONObject();
					JSONObject live = new JSONObject();
					live.put("status", "ready");
					patchRequest.put("live", live);
					String liveEventResponse = callVimeoApi("https://api.vimeo.com" + uri, patchRequest, "PATCH");
					if(StringUtils.isNotBlank(liveEventResponse)) {
						JSONObject liveResp = new JSONObject(liveEventResponse);
						String rtmpUrl = liveResp.getString("link");
						String key = liveResp.getString("key");
						LoggingUtil.logMessage("Live event created as " + rtmpUrl + " and key " + key);
					}
				}
				//Call API to create live event
			}
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e), LoggingUtil.videoLogger);
		}
		
	}
	
	/*public static void upload(String filePath) throws IOException {
		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
				  "cloud_name", "edofox",
				  "api_key", "817386265822862",
				  "api_secret", "gxUY72bnB8r6WfExA4bFo7Qc23w"));
		
		Map response = cloudinary.uploader().upload(filePath, 
			    ObjectUtils.asMap("resource_type", "video"
			    "public_id", "my_folder/my_sub_folder/dog_closeup",
			    "eager", Arrays.asList(
			        new Transformation().width(300).height(300).crop("pad").audioCodec("none"),
			        new Transformation().width(160).height(100).crop("crop").gravity("south").audioCodec("none")),
			    "eager_async", true,
			    "eager_notification_url", "https://mysite.example.com/notify_endpoint"));
		
		System.out.println("Response=>" + response);

	}*/
	
	/*public static void uploadToStorage() {
		Region region = Region.US_WEST_2;
		s3 = S3Client.builder().region(region).build();


		String bucket = "bucket" + System.currentTimeMillis();
		String key = "key";

		createBucket(s3,bucket, region);

		// Put Object
		s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
		                .build(),
		        RequestBody.fromByteBuffer(getRandomByteBuffer(10_000)));
	}*/
	
	public static void uploadSftp(String path, String fileName) {
		//String localFile = "/home/mkyong/local/random.txt";
        //String remoteFile = "/usr/local/WowzaStreamingEngine-4.8.5/content/" + fileName;
        String remoteFile = "/opt/mediamanager/movies/" + fileName;
        Date date = new Date();
        Session jschSession = null;

        try {

            JSch jsch = new JSch();
            jsch.setKnownHosts("C:\\Users\\Admin\\.ssh\\known_hosts");
            jschSession = jsch.getSession("root", "89.47.165.216", 22);

            // authenticate using private key
            jsch.addIdentity("C:\\Users\\Admin\\.ssh\\id_rsa");

            // authenticate using password
            jschSession.setPassword("4iDI9v7eE4Vd");

            // 10 seconds session timeout
            jschSession.connect(SESSION_TIMEOUT);

            Channel sftp = jschSession.openChannel("sftp");

            // 5 seconds timeout
            sftp.connect(CHANNEL_TIMEOUT);

            ChannelSftp channelSftp = (ChannelSftp) sftp;

            // transfer file from local to remote server
            channelSftp.put(path, remoteFile);

            // download file from remote server to local
            // channelSftp.get(remoteFile, localFile);

            channelSftp.exit();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
            LoggingUtil.logMessage("Time taken for upload " + ((date.getTime() - new Date().getTime()) / 1000), LoggingUtil.videoLogger);
        }

        System.out.println("Done");
	}

	public static EdoFile getStreamingUrls(String video_url, String requestType) throws ClientProtocolException, IOException {
		if(StringUtils.isBlank(video_url)) {
			return null;
		}
		EdoFile file = new EdoFile();
		file.setVersions(new ArrayList<EdoFile>());
		file.setDownloadUrl(video_url);
		if(!StringUtils.equals(requestType, "player")) {
			//Prepare download URLs
			String lowQ = StringUtils.replace(video_url, ":1935/vod", "/mediamanager/cdn");
			lowQ = StringUtils.replace(lowQ, ".smil/playlist.m3u8", "");
			lowQ = StringUtils.replace(lowQ, ".mp4", "_240px.mp4");
			EdoFile lowFile = new EdoFile();
			lowFile.setDownloadUrl(lowQ);
			lowFile.setHeight(240f);
			lowFile.setSize(getFileLength(lowQ));
			String medQ = StringUtils.replace(lowQ, "240px", "360px");
			EdoFile medFile = new EdoFile();
			medFile.setDownloadUrl(medQ);
			medFile.setHeight(360f);
			medFile.setSize(getFileLength(medQ));
			String highQ = StringUtils.replace(lowQ, "240px", "720px");
			EdoFile highFile = new EdoFile();
			highFile.setDownloadUrl(highQ);
			highFile.setHeight(720f);
			highFile.setSize(getFileLength(highQ));
			
			file.getVersions().add(highFile);
			file.getVersions().add(medFile);
			file.getVersions().add(lowFile);
		} else {
			//Prepare player URLs
			String lowQ = StringUtils.replace(video_url, ".smil", "");
			lowQ = StringUtils.replace(lowQ, ".mp4", "_240px.mp4");
			EdoFile lowFile = new EdoFile();
			lowFile.setDownloadUrl(lowQ);
			lowFile.setHeight(240f);
			
			String medQ = StringUtils.replace(lowQ, "240px", "360px");
			EdoFile medFile = new EdoFile();
			medFile.setDownloadUrl(medQ);
			medFile.setHeight(360f);
			
			String highQ = StringUtils.replace(lowQ, "240px", "720px");
			EdoFile highFile = new EdoFile();
			highFile.setDownloadUrl(highQ);
			highFile.setHeight(720f);
			
			file.getVersions().add(highFile);
			file.getVersions().add(medFile);
			file.getVersions().add(lowFile);
		}
		return file;
	}
}
