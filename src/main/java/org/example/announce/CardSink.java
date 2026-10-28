package org.example.announce;

/** Posts an announcement card. */
public interface CardSink
{
	/** Returns false on failure; an unconfirmed send (interrupted or timed out) counts as posted. */
	boolean post(Item item);
}
