package com.rns.web.edo.service.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import com.clickntap.vimeo.Vimeo;
import com.clickntap.vimeo.VimeoException;
import com.clickntap.vimeo.VimeoResponse;

public class VideoUtil {

	public static String FFMPEG_BIN = "F:\\Resoneuronance\\Setups\\ffmpeg-20200403-52523b6-win64-static\\bin\\";


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
		//LoggingUtil.logMessage("Adding domain to privacy " + defaultDomain, LoggingUtil.videoLogger);
		//TODO vimeo.addVideoPrivacyDomain(videoEndPoint, defaultDomain);
		
	    //delete video
	    //TODO vimeo.removeVideo(videoEndPoint);
	    return info;
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
				/*InputStream content = res.getEntity().getContent();
				if(content != null) {
				    int read;
					byte[] bytes = new byte[1024];
					
					while ((read = content.read(bytes)) != -1) {
						fileOutputStream.write(bytes, 0, read);
					}
					fileOutputStream.close();
				}
				return outputFile;*/
			} 
			
		} catch (Exception e) {
			LoggingUtil.logError(ExceptionUtils.getStackTrace(e));
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
}
