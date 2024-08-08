/*
 * This file is part of the L2J BrUnity project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jbr_unity.gameserver.model.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jbr_unity.commons.util.Rnd;
import org.l2jbr_unity.gameserver.instancemanager.CaptchaManager.PlayerData;
import org.l2jbr_unity.gameserver.model.actor.Player;
import org.l2jbr_unity.gameserver.network.serverpackets.PledgeCrest;
import org.l2jbr_unity.gameserver.util.DDSConverter;

public class Captcha
{
	private static final char[] CAPTCHA_TEXT_POSSIBILITIES =
	{
		'A',
		'B',
		'C',
		'D',
		'E',
		'F',
		'G',
		'H',
		'K',
		'L',
		'M',
		'P',
		'R',
		'S',
		'T',
		'U',
		'W',
		'X',
		'Y',
		'Z'
	};
	private static final int CAPTCHA_WORD_LENGTH = 5;
	
	private static final int CAPTCHA_MIN_ID = 19000;
	private static final int CAPTCHA_MAX_ID = 25000;
	
	char[] captchaText;
	int captchaId;
	
	protected Captcha()
	{
	}
	
	public void generateCaptcha(PlayerData data, Player target)
	{
		final int cId = generateRandomCaptchaId();
		final char[] cText = generateCaptchaText();
		final BufferedImage image = generateCaptcha(cText);
		final byte[] imageData = DDSConverter.convertToDDS(image).array();
		
		data.image = image;
		final PledgeCrest packet = new PledgeCrest(cId, imageData);
		target.sendPacket(packet);
		
		data.captchaId = cId;
		data.captchaText = String.valueOf(cText);
	}
	
	public synchronized Map<Integer, ImageData> createImageList()
	{
		final Map<Integer, ImageData> imageMap = new ConcurrentHashMap<>();
		for (int i = 0; i < 1000; i++)
		{
			do
			{
				captchaId = generateRandomCaptchaId();
				captchaText = generateCaptchaText();
			}
			while ((!imageMap.isEmpty() && imageMap.values().stream().anyMatch(txt -> txt.captchaText.equals(String.valueOf(captchaText)))) || imageMap.values().stream().anyMatch(s -> s.captchaID == captchaId));
			
			final ImageData dt = new ImageData();
			dt.captchaID = captchaId;
			dt.captchaText = String.valueOf(captchaText);
			dt.image = generateCaptcha(captchaText);
			imageMap.put(i, dt);
		}
		return imageMap;
	}
	
	private static char[] generateCaptchaText()
	{
		final char[] text = new char[CAPTCHA_WORD_LENGTH];
		for (int i = 0; i < CAPTCHA_WORD_LENGTH; i++)
		{
			text[i] = CAPTCHA_TEXT_POSSIBILITIES[Rnd.get(CAPTCHA_TEXT_POSSIBILITIES.length)];
		}
		return text;
	}
	
	private static int generateRandomCaptchaId()
	{
		return Rnd.get(CAPTCHA_MIN_ID, CAPTCHA_MAX_ID);
	}
	
	public static BufferedImage generateCaptcha(char[] text)
	{
		final Color textColor = new Color(38, 213, 30);
		final Color circleColor = new Color(73, 100, 151);
		final Font textFont = new Font("comic sans ms", Font.BOLD, 24);
		final int charsToPrint = 5;
		final int width = 256;
		final int height = 64;
		final int circlesToDraw = 8;
		final float horizMargin = 20.0f;
		final double rotationRange = 0.7; // this is radians
		final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		final Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		
		// Draw an oval
		g.setColor(new Color(30, 31, 31));
		g.fillRect(0, 0, width, height);
		
		g.setColor(circleColor);
		for (int i = 0; i < circlesToDraw; i++)
		{
			final int circleRadius = (int) ((Math.random() * height) / 2.0);
			final int circleX = (int) ((Math.random() * width) - circleRadius);
			final int circleY = (int) ((Math.random() * height) - circleRadius);
			g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
		}
		
		g.setColor(textColor);
		g.setFont(textFont);
		
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int maxAdvance = fontMetrics.getMaxAdvance();
		final int fontHeight = fontMetrics.getHeight();
		
		final float spaceForLetters = (-horizMargin * 2.0F) + width;
		final float spacePerChar = spaceForLetters / (charsToPrint - 1.0f);
		
		for (int i = 0; i < charsToPrint; i++)
		{
			final char characterToShow = text[i];
			
			// This is a separate canvas used for the character so that we can rotate it independently.
			final int charWidth = fontMetrics.charWidth(characterToShow);
			final int charDim = Math.max(maxAdvance, fontHeight);
			final int halfCharDim = charDim / 2;
			
			final BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D charGraphics = charImage.createGraphics();
			charGraphics.translate(halfCharDim, halfCharDim);
			final double angle = (Math.random() - 0.5) * rotationRange;
			charGraphics.transform(AffineTransform.getRotateInstance(angle));
			charGraphics.translate(-halfCharDim, -halfCharDim);
			charGraphics.setColor(textColor);
			charGraphics.setFont(textFont);
			
			final int charX = (int) ((0.5 * charDim) - (0.5 * charWidth));
			charGraphics.drawString(String.valueOf(characterToShow), charX, ((charDim - fontMetrics.getAscent()) / 2) + fontMetrics.getAscent());
			
			final float x = (horizMargin + (spacePerChar * i)) - (charDim / 2.0f);
			final int y = (height - charDim) / 2;
			g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);
			
			charGraphics.dispose();
		}
		
		g.dispose();
		
		return bufferedImage;
	}
	
	public static Captcha getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Captcha INSTANCE = new Captcha();
	}
}
