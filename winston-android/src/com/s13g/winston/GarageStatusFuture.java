/*
 * Copyright 2014 Sascha Haeberling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.s13g.winston;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.s13g.winston.R;
import com.s13g.winston.GarageStatusFuture.GarageStatus;

/**
 * A future that listens to two reed status futures to determine the status of
 * one garage door.
 */
public class GarageStatusFuture implements Future<GarageStatus> {
	public static enum GarageStatus {
		OPEN, MOVING, CLOSED, ERROR;

		public static int toStringId(GarageStatus status) {
			switch (status) {
			case OPEN:
				return R.string.status_open;
			case MOVING:
				return R.string.status_moving;
			case CLOSED:
				return R.string.status_closed;
			case ERROR:
				return R.string.status_error;
			default:
				return R.string.status_unknown;
			}
		}
	}

	private final Future<Boolean> mOpenReed;
	private final Future<Boolean> mClosedReed;

	public GarageStatusFuture(Future<Boolean> openReed,
			Future<Boolean> closedReed) {
		mOpenReed = openReed;
		mClosedReed = closedReed;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return mOpenReed.cancel(mayInterruptIfRunning)
				| mClosedReed.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return mOpenReed.isCancelled() && mClosedReed.isCancelled();
	}

	@Override
	public boolean isDone() {
		return mOpenReed.isDone() && mClosedReed.isDone();
	}

	@Override
	public GarageStatus get() throws InterruptedException, ExecutionException {
		boolean isOpen = mOpenReed.get();
		boolean isClosed = mClosedReed.get();

		if (isOpen && isClosed) {
			return GarageStatus.ERROR;
		} else if (isOpen) {
			return GarageStatus.OPEN;
		} else if (isClosed) {
			return GarageStatus.CLOSED;
		} else {
			return GarageStatus.MOVING;
		}
	}

	@Override
	public GarageStatus get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}

}
