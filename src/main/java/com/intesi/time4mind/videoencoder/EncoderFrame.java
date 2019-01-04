package com.intesi.time4mind.videoencoder;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegStream;

public class EncoderFrame
{
	private final static Logger	logger	= LoggerFactory.getLogger(EncoderFrame.class);
	private static Properties	prop;

	public static void main(String[] args)
	{
		// load local resources
		ClassLoader classLoader = EncoderFrame.class.getClassLoader();
		PropertyConfigurator.configure(classLoader.getResourceAsStream("resources/log4j.properties"));
		ImageIcon icon = new ImageIcon(classLoader.getResource("resources/img/icon.png"));
		ImageIcon loaderIcon = new ImageIcon(classLoader.getResource("resources/img/giphy.gif"));
		loadProperties(classLoader);

		String path = null;
		try {
			path = EncoderFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			int startIdx = path.startsWith("/") ? 1 : 0;
			path = path.substring(startIdx, path.lastIndexOf("/"));
		} catch (Exception e) {
			logger.warn("Application location not found.", e);
		}
		String ffmpegPath = path + "/ffmpeg.exe";
		String ffprobePath = path + "/ffprobe.exe";
		logger.debug("ffmpeg installation path: " + ffmpegPath);
		logger.debug("ffprobe installation path: " + ffprobePath);

		logger.info("Application start");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.warn("OS look and feel not loaded.", e);
		}
		JFileChooser b = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		b.setDialogTitle(prop.getProperty("dialog.title"));
		b.setFileSelectionMode(JFileChooser.FILES_ONLY);
		b.setAcceptAllFileFilterUsed(false);
		b.addChoosableFileFilter(new FileNameExtensionFilter(prop.getProperty("dialog.filter.descr"), prop.getProperty("dialog.filter.ext")));

		int returnValue = b.showOpenDialog(null);
		if (returnValue != JFileChooser.APPROVE_OPTION) {
			logger.warn("Application shutdown");
			System.exit(0);
		}

		logger.info("File correctly selected");

		// create custom panel
		GUIPanel p = new GUIPanel();

		// create main frame
		JFrame f = new JFrame();
		f.setTitle(prop.getProperty("frame.title"));
		f.setIconImage(icon.getImage());
		f.setSize(400, 500);
		f.setLayout(new BorderLayout());
		f.setVisible(true);
		f.setResizable(false);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(p);

		// start video encoding
		logger.info("Initialize video encoder");
		p.writeHeader(prop.getProperty("frame.enc.start"));
		File file = b.getSelectedFile();
		p.writeText("File: " + file.getName());
		p.writeText("");
		VideoEncoder encoder = null;
		try {
			encoder = new VideoEncoder(file.getAbsolutePath(), ffmpegPath, ffprobePath);
		} catch (Exception e) {
			logger.error("VideoEncoder not initialized.", e);
		}

		logger.info("Preprocess input video");
		FFmpegStream stream = null;
		try {
			stream = encoder.preprocess();
			p.writeHeader(prop.getProperty("frame.enc.inputSettings"));
			p.writeText(prop.getProperty("frame.enc.width") + stream.width);
			p.writeText(prop.getProperty("frame.enc.height") + stream.height);
			p.writeText(prop.getProperty("frame.enc.ratio") + stream.display_aspect_ratio);
			p.writeText(prop.getProperty("frame.enc.bitRate") + stream.bit_rate);
			p.writeText("");
			p.refresh();
		} catch (Exception e) {
			logger.error("VideoEncoder source not preprocessed.", e);
		}

		logger.info("Create encoder builder");
		try {
			FFmpegOutputBuilder builder = encoder.getBuilder(stream);
			p.writeHeader(prop.getProperty("frame.enc.outputSettings"));
			p.writeText(prop.getProperty("frame.enc.width") + builder.video_width);
			p.writeText(prop.getProperty("frame.enc.height") + builder.video_height);
			long bitRate = (builder.video_bit_rate > 0) ? builder.video_bit_rate : stream.bit_rate;
			p.writeText(prop.getProperty("frame.enc.bitRate") + bitRate);
			p.writeText("");
			p.refresh();
		} catch (Exception e) {
			logger.error("VideoEncoder builder not created.", e);
		}

		long start = System.currentTimeMillis();
		logger.info("Compress video");
		try {
			p.startLoader(prop.getProperty("frame.enc.loading"), loaderIcon);
			p.refresh();
			encoder.compress();
		} catch (Exception e) {
			logger.error("VideoEncoder not compressed.", e);
		}
		logger.info(String.format("Video processed in %d ms", System.currentTimeMillis() - start));

		p.stopLoader();
		p.writeHeader(prop.getProperty("frame.complete"));
		p.writeText(prop.getProperty("frame.upload"));
		p.writeText(encoder.getDestinationFile());
		p.addCopyButton(prop.getProperty("frame.copyButton"), encoder.getDestinationFile());
		p.refresh();
	}

	private static void loadProperties(ClassLoader classLoader)
	{
		prop = new Properties();

		InputStream is = null;
		try {
			is = classLoader.getResourceAsStream("resources/settings.properties");
			prop.load(is);
		} catch (Exception e) {
			logger.error("Application settings not loaded.", e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
