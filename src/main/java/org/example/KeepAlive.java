package org.example;

import java.util.concurrent.CountDownLatch;

/** Holds the calling thread so the JVM stays up while only daemon bot threads are running. */
final class KeepAlive
{
	private final CountDownLatch latch = new CountDownLatch(1);

	void await() throws InterruptedException
	{
		latch.await();
	}

	void release()
	{
		latch.countDown();
	}
}
