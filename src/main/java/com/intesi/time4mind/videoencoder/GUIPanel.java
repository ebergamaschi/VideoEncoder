package com.intesi.time4mind.videoencoder;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class GUIPanel extends JPanel
{
	private static final long	serialVersionUID	= 1L;

	private GridBagConstraints	constraints;
	private Font				font;
	private Font				headerFont;

	private JLabel				loader;

	public GUIPanel() {
		setBackground(Color.white);
		setBorder(new LineBorder(Color.BLACK));
		setLayout(new GridBagLayout());

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(5, 5, 5, 5);

		headerFont = new Font("Arial", Font.BOLD, 14);
		font = new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 12);
	}

	public void writeHeader(String text)
	{
		JLabel l = new JLabel(text);
		l.setFont(headerFont);
		add(l, constraints);
		constraints.gridy++;
	}

	public void writeText(String text)
	{
		JLabel l = new JLabel(text);
		l.setFont(font);
		add(l, constraints);
		constraints.gridy++;
	}

	public void startLoader(String text, ImageIcon icon)
	{
		loader = new JLabel(text, icon, SwingConstants.CENTER);
		loader.setFont(font);
		add(loader, constraints);
		constraints.gridy++;
	}

	public void stopLoader()
	{
		remove(loader);
		constraints.gridy--;
	}

	public void addCopyButton(String text, final String data)
	{
		JButton b = new JButton(text);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				StringSelection selection = new StringSelection(data);
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				c.setContents(selection, null);
			}
		});
		add(b, constraints);
		constraints.gridy++;
	}

	public void refresh()
	{
		revalidate();
		repaint();
	}

}
