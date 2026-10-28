package org.example.announce;

import java.io.IOException;
import java.util.List;

/** Supplies items that currently qualify for announcement. */
public interface ArrSource
{
	List<Item> movies() throws IOException;

	List<Item> seasons() throws IOException;
}
