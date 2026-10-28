package org.example.announce;

/** Posts an announcement card. */
public interface CardSink
{
	/** Returns false on failure. */
	boolean post(Item item);
}
