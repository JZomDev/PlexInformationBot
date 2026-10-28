package org.example.announce;

import java.io.IOException;
import java.util.List;

/** Supplies items that currently qualify for announcement. */
public interface ArrSource
{
	/** False when the service behind this type ("movie" or "season") has no address or key saved. */
	boolean configured(String type);

	List<Item> movies() throws IOException;

	List<Item> seasons() throws IOException;
}
