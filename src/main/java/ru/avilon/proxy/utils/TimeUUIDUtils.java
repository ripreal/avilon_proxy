package ru.avilon.proxy.utils;

import java.util.UUID;

import com.eaio.uuid.UUIDGen;

public class TimeUUIDUtils {

	public static UUID ZERO_TIME_UUID = getTimeUUID(0l);
	public static UUID MAX_TIME_UUID = getTimeUUID(System.currentTimeMillis() * 2);

	static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

	public static long getTimeFromUUID(String uuid) {
		return getTimeFromUUID(UUID.fromString(uuid));
	}
	
	public static long getTimeFromUUID(UUID uuid) {
		return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
	}
	
	public static java.util.UUID getTimeUUID(long time) {
		return new java.util.UUID(createTime(time), UUIDGen.getClockSeqAndNode());
	}
	public static java.util.UUID getCurrentTimeUUID() {
		return getTimeUUID(System.currentTimeMillis());
	}

	private static long createTime(long currentTime) {
		long time;

		// UTC time
		long timeToUse = (currentTime * 10000) + NUM_100NS_INTERVALS_SINCE_UUID_EPOCH;

		// time low
		time = timeToUse << 32;

		// time mid
		time |= (timeToUse & 0xFFFF00000000L) >> 16;

		// time hi and version
		time |= 0x1000 | ((timeToUse >> 48) & 0x0FFF); // version 1
		return time;
	}

}
