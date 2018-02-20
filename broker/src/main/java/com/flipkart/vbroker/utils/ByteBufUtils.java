package com.flipkart.vbroker.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author govind.ajith
 */
public class ByteBufUtils {

	/**
	 * Utility to get bytes out of ByteBuffer.
	 *
	 * @param byteBuffer
	 * @return
	 */
	public static byte[] getBytes(ByteBuffer byteBuffer) {

		int length = byteBuffer.remaining();
		if (byteBuffer.hasArray()) {
			byte[] bytes = byteBuffer.array();
			int offset = byteBuffer.arrayOffset() + byteBuffer.position();
			return Arrays.copyOfRange(bytes, offset, offset + length);
		} else {
			byte[] bytes = new byte[length];
			byteBuffer.duplicate().get(bytes);
			return bytes;
		}
	}

}
