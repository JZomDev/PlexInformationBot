package org.example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class KeepAliveTest
{
	@Test
	void awaitBlocksUntilReleased() throws Exception
	{
		KeepAlive keepAlive = new KeepAlive();
		Thread waiter = new Thread(() ->
		{
			try
			{
				keepAlive.await();
			}
			catch (InterruptedException ignored)
			{
			}
		});
		waiter.start();
		waiter.join(200);
		assertTrue(waiter.isAlive());
		keepAlive.release();
		waiter.join(TimeUnit.SECONDS.toMillis(5));
		assertFalse(waiter.isAlive());
	}
}
