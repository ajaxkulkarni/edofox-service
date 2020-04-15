package com.rns.web.edo.service.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

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
		String oldCommand = " -y -f concat -safe 0 -i {folderLocation}list.txt -c copy -c:v libvpx {outputLocation}";
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

		oldCommand = StringUtils.replace(oldCommand, "{folderLocation}", folderLocation);
		oldCommand = StringUtils.replace(oldCommand, "{outputLocation}", outputFolder);
		
		//LoggingUtil.logMessage("Running command with runtime .." + oldCommand);
		
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		// -- Linux --

		// Run a shell command
		processBuilder.command("ffmpeg", "-y", "-f", "concat", "-safe", "0","-i",
				folderLocation + "list.txt", "-c", "copy", "-c:v", "libvpx", outputFolder);
		
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
}
