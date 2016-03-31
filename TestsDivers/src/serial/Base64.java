package serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class Base64 {
	private static final int END_OF_INPUT = -1;
	private static final int NON_BASE_64 = -1;
	private static final int NON_BASE_64_WHITESPACE = -2;
	private static final int NON_BASE_64_PADDING = -3;
	private static final byte[] base64Chars = {
		'A','B','C','D','E','F','G','H',
		'I','J','K','L','M','N','O','P',
		'Q','R','S','T','U','V','W','X',
		'Y','Z','a','b','c','d','e','f',
		'g','h','i','j','k','l','m','n',
		'o','p','q','r','s','t','u','v',
		'w','x','y','z','0','1','2','3',
		'4','5','6','7','8','9','+','/',
	};
	private static final byte[] reverseBase64Chars = new byte[0x100];
	static {
		// Fill in NON_BASE_64 for all characters to start with
		for (int i=0; i<reverseBase64Chars.length; i++){
			reverseBase64Chars[i] = NON_BASE_64;
		}
		// For characters that are base64Chars, adjust
		// the reverse lookup table.
		for (byte i=0; i < base64Chars.length; i++){
			reverseBase64Chars[base64Chars[i]] = i;
		}
		reverseBase64Chars[' '] = NON_BASE_64_WHITESPACE;
		reverseBase64Chars['\n'] = NON_BASE_64_WHITESPACE;
		reverseBase64Chars['\r'] = NON_BASE_64_WHITESPACE;
		reverseBase64Chars['\t'] = NON_BASE_64_WHITESPACE;
		reverseBase64Chars['\f'] = NON_BASE_64_WHITESPACE;
		reverseBase64Chars['='] = NON_BASE_64_PADDING;
	}


	public static StringBuffer encode(byte[] bytes, StringBuffer out){
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		// calculate the length of the resulting output.
		// in general it will be 4/3 the size of the input
		// but the input length must be divisible by three.
		// If it isn't the next largest size that is divisible
		// by three is used.
		int mod;
		int length = bytes.length;
		if ((mod = length % 3) != 0){
			length += 3 - mod;
		}
		length = length * 4 / 3;
		encode(in, out);
		return out;
	}

	public static void encode(InputStream in, StringBuffer out) {
		// Base64 encoding converts three bytes of input to
		// four bytes of output
		int[] inBuffer = new int[3];
		try {
		boolean done = false;
		while (!done && (inBuffer[0] = in.read()) != END_OF_INPUT){
			// Fill the buffer
			inBuffer[1] = in.read();
			inBuffer[2] = in.read();

			// Calculate the out Buffer
			// The first byte of our in buffer will always be valid
			// but we must check to make sure the other two bytes
			// are not END_OF_INPUT before using them.
			// The basic idea is that the three bytes get split into
			// four bytes along these lines:
			//      [AAAAAABB] [BBBBCCCC] [CCDDDDDD]
			// [xxAAAAAA] [xxBBBBBB] [xxCCCCCC] [xxDDDDDD]
			// bytes are considered to be zero when absent.
			// the four bytes are then mapped to common ASCII symbols

			// A's: first six bits of first byte
			out.append(base64Chars[ inBuffer[0] >> 2 ]);
			if (inBuffer[1] != END_OF_INPUT){
				// B's: last two bits of first byte, first four bits of second byte
				out.append(base64Chars [(( inBuffer[0] << 4 ) & 0x30) | (inBuffer[1] >> 4) ]);
				if (inBuffer[2] != END_OF_INPUT){
					// C's: last four bits of second byte, first two bits of third byte
					out.append(base64Chars [((inBuffer[1] << 2) & 0x3c) | (inBuffer[2] >> 6) ]);
					// D's: last six bits of third byte
					out.append(base64Chars [inBuffer[2] & 0x3F]);
				} else {
					// C's: last four bits of second byte
					out.append(base64Chars [((inBuffer[1] << 2) & 0x3c)]);
					// an equals sign for a character that is not a Base64 character
					out.append('=');
					done = true;
				}
			} else {
				// B's: last two bits of first byte
				out.append(base64Chars [(( inBuffer[0] << 4 ) & 0x30)]);
				// an equal signs for characters that is not a Base64 characters
				out.append('=');
				out.append('=');
				done = true;
			}
		}
		} catch(Exception e) {}
	}

	public static byte[] decode(String s){
		byte[] bytes = null;
		try { bytes = s.getBytes("UTF-8"); } catch (UnsupportedEncodingException e) { }
		if (bytes == null) return null;
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		// calculate the length of the resulting output.
		// in general it will be at most 3/4 the size of the input
		// but the input length must be divisible by four.
		// If it isn't the next largest size that is divisible
		// by four is used.
		int mod;
		int length = bytes.length;
		if ((mod = length % 4) != 0){
			length += 4 - mod;
		}
		length = length * 3 / 4;
		ByteArrayOutputStream out = new ByteArrayOutputStream(length);
		try { decode(in, out); } catch (Exception x){	}
		return out.toByteArray();
	}

	private static void decode(InputStream in, OutputStream out) {
		// Base64 decoding converts four bytes of input to three bytes of output
		int[] inBuffer = new int[4];

		// read bytes un-mapping them from their ASCII encoding in the process
		// we must read at least two bytes to be able to output anything
		boolean done = false;
		try {
		while (!done && (inBuffer[0] = readBase64(in)) != END_OF_INPUT
			&& (inBuffer[1] = readBase64(in)) != END_OF_INPUT){
			// Fill the buffer
			inBuffer[2] = readBase64(in);
			inBuffer[3] = readBase64(in);

			// Calculate the output
			// The first two bytes of our in buffer will always be valid
			// but we must check to make sure the other two bytes
			// are not END_OF_INPUT before using them.
			// The basic idea is that the four bytes will get reconstituted
			// into three bytes along these lines:
			// [xxAAAAAA] [xxBBBBBB] [xxCCCCCC] [xxDDDDDD]
			//      [AAAAAABB] [BBBBCCCC] [CCDDDDDD]
			// bytes are considered to be zero when absent.

			// six A and two B
			out.write(inBuffer[0] << 2 | inBuffer[1] >> 4);
			if (inBuffer[2] != END_OF_INPUT){
				// four B and four C
				out.write(inBuffer[1] << 4 | inBuffer[2] >> 2);
				if (inBuffer[3] != END_OF_INPUT){
					// two C and six D
					out.write(inBuffer[2] << 6 | inBuffer[3]);
				} else {
					done = true;
				}
			} else {
				done = true;
			}
		}
		out.flush();
		} catch (Exception e){}
	}

	private static final int readBase64(InputStream in) throws IOException {
		int read;
		do {
			read = in.read();
			if (read == END_OF_INPUT) return END_OF_INPUT;
			read = reverseBase64Chars[(byte)read];
		} while (read <= NON_BASE_64);
		return read;
	}

}
