package org.kramerlab.coffer.api.impl.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.kramerlab.coffer.api.impl.DepictService;
import org.mg.javalib.gui.property.ColorGradient;
import org.mg.javalib.util.StringUtil;
import org.mg.javalib.util.SwingUtil;

public class ActiveImageIcon extends ImageIcon
{
	private static final long serialVersionUID = 1L;

	int size;
	Color col;
	ImageIcon img;

	public ActiveImageIcon(int size, double probability, boolean drawHelp)
	{
		this.size = size;
		this.col = new ColorGradient(DepictService.ACTIVE_BRIGHT, DepictService.NEUTRAL_BRIGHT,
				DepictService.INACTIVE_BRIGHT).getColor(probability);
		if (drawHelp)
			img = new ImageIcon(
					"/home/martin/workspace/CFPService/CFPService-webapp/src/main/webapp/img/help14.png");
	}

	@Override
	public int getIconWidth()
	{
		return size;
	}

	@Override
	public int getIconHeight()
	{
		return size;
	}

	@Override
	public Image getImage()
	{
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		paintIcon(null, img.getGraphics(), 0, 0);
		return img;
	}

	@Override
	public synchronized void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2 = (Graphics2D) g;
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHints(rh);

		g.setColor(Color.GRAY);
		g.fillOval(0, 0, size, size);
		g.setColor(col);
		g.fillOval(1, 1, size - 2, size - 2);
		if (img != null)
		{
			g.setColor(new Color(255, 255, 255, 200));
			g.fillOval(4, 1, size - 8, size - 2);
			img.paintIcon(c, g, 0, 0);
		}
	}

	public static void main(String[] args)
	{
		JPanel p = new JPanel(new GridLayout(11, 1));
		p.setBackground(Color.WHITE);
		for (int i = 0; i < 11; i++)
		{
			double prob = 0 + 0.1 * i;
			JLabel l = new JLabel("prob " + StringUtil.formatDouble(prob));
			l.setIcon(new ActiveImageIcon(14, prob, true));
			p.add(l);
		}
		SwingUtil.showInFrame(p);
		SwingUtil.waitWhileWindowsVisible();
		System.exit(0);
	}
}
