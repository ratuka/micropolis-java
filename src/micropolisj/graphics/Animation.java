package micropolisj.graphics;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.*;
import java.util.*;
import javax.xml.stream.*;
import static micropolisj.XML_Helper.*;

public class Animation extends TileImage
{
	static final int DEFAULT_DURATION = 125;

	public List<Frame> frames = new ArrayList<Frame>();
	public int totalDuration;

	public static Animation load(File aniFile, LoaderContext ctx)
		throws IOException
	{
		FileInputStream fis = new FileInputStream(aniFile);

		try {

			XMLStreamReader in = XMLInputFactory.newInstance().createXMLStreamReader(fis, "UTF-8");
			in.nextTag();
			if (!(in.getEventType() == XMLStreamConstants.START_ELEMENT &&
				in.getLocalName().equals("micropolis-animation"))) {
				throw new IOException("Unrecognized file format");
			}

			Animation a = read(in, ctx);

			in.close();

			return a;
		}
		catch (XMLStreamException e)
		{
			throw new IOException(aniFile.toString()+": "+e.getMessage(), e);
		}
		finally
		{
			fis.close();
		}
	}

	public static Animation read(XMLStreamReader in, LoaderContext ctx)
		throws XMLStreamException
	{
		Animation a = new Animation();
		a.load(in, ctx);
		return a;
	}

	public void addFrame(TileImage img, int duration)
	{
		totalDuration += duration;
		Frame f = new Frame(img, totalDuration);
		frames.add(f);
	}

	void load(XMLStreamReader in, LoaderContext ctx)
		throws XMLStreamException
	{
		while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
			assert in.isStartElement();

			String tagName = in.getLocalName();
			if (tagName.equals("frame")) {

				String tmp = in.getAttributeValue(null, "duration");
				int duration = tmp != null ? Integer.parseInt(tmp) : DEFAULT_DURATION;

				String text = in.getElementText();
				TileImage frameImage;
				try {
					frameImage = ctx.parseFrameSpec(text);
				}
				catch (IOException e) {
					throw new XMLStreamException("Unable to load frame image: "+text, e);
				}

				addFrame( frameImage, duration );
			}
			else {
				// unrecognized element
				skipToEndElement(in);
			}
		}
	}

	public class Frame
	{
		public final TileImage frame;
		public final int duration;

		public Frame(TileImage frame, int duration)
		{
			this.frame = frame;
			this.duration = duration;
		}
	}

	private TileImage getDefaultImage()
	{
		return frames.get(0).frame;
	}

	@Override
	public void drawFragment(Graphics2D gr, int destX, int destY, int srcX, int srcY)
	{
		// Warning: drawing without considering the animation
		getDefaultImage().drawFragment(gr, destX, destY, srcX, srcY);
	}
}
