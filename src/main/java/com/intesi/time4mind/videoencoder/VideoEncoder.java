package com.intesi.time4mind.videoencoder;

import java.io.File;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

public class VideoEncoder
{
	private static String				source;
	private static String				dest;
	private static FFmpeg				ffmpeg;
	private static FFprobe				ffprobe;

	private static FFmpegProbeResult	probeResult;
	private static FFmpegBuilder		builder;

	public VideoEncoder(String filePath, String ffmpegPath, String ffprobePath) throws Exception {
		source = filePath;
		String path = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1);
		String fileName = filePath.replace(path, "");
		dest = String.format("%scompress_%s", path, fileName);

		ffmpeg = new FFmpeg(ffmpegPath);
		ffprobe = new FFprobe(ffprobePath);
	}

	public FFmpegStream preprocess() throws Exception
	{
		probeResult = ffprobe.probe(source);
		FFmpegStream stream = probeResult.getStreams().get(0);

		return stream;
	}

	public FFmpegOutputBuilder getBuilder(FFmpegStream stream) throws Exception
	{
		long maxBitRate = 1000 * 1024; // 1000 kbps
		long bitRate = stream.bit_rate;
		int width = stream.width;
		int height = stream.height;

		// String ratio = stream.display_aspect_ratio;
		// // 16:9
		// if (("16:9".equals(ratio) || "0:1".equals(ratio)) && width > 960) {
		// width = 960;
		// height = 540;
		// }
		// // 4:3
		// else if ("4:3".equals(ratio) && width > 640) {
		// width = 640;
		// height = 480;
		// }

		// 16:9
		if ((width / height == 16 / 9) && width > 960) {
			width = 960;
			height = 540;
		}
		// 4:3
		else if ((width / height == 4 / 3) && width > 640) {
			width = 640;
			height = 480;
		}

		builder = new FFmpegBuilder();
		builder.setInput(probeResult); // Filename or FFmpegProbeResult
		builder.overrideOutputFiles(true); // Override the output if it exists
		builder.setFormat("mp4"); // Format is inferred from filename, or can be set
		FFmpegOutputBuilder outputBuilder = builder.addOutput(dest); // Filename for the destination
		outputBuilder.disableSubtitle(); // No subtiles
		outputBuilder.setAudioChannels(1); // Mono audio
		outputBuilder.setAudioCodec("aac"); // using the aac codec
		outputBuilder.setAudioBitRate(32 * 1024); // at 32 kbit/s
		outputBuilder.setVideoCodec("libx264"); // Video using x264
		outputBuilder.setVideoFrameRate(24, 1); // at 24 frames per second
		outputBuilder.setVideoResolution(width, height); // at 640x480 resolution
		if (bitRate > maxBitRate) {
			outputBuilder.setVideoBitRate(maxBitRate);
		}
		outputBuilder.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL); // Allow FFmpeg to use experimental specs
		outputBuilder.done();

		return outputBuilder;
	}

	public void compress() throws Exception
	{
		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
		executor.createJob(builder).run();
	}

	public String getDestinationFile()
	{
		return dest;
	}

}
