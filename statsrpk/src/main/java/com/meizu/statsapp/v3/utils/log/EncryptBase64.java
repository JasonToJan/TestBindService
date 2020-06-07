package com.meizu.statsapp.v3.utils.log;

import java.nio.charset.Charset;

public class EncryptBase64 {
	private static final char[] base64_table = { 'A', 'B', 'C', 'D', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', '+', '/' };

	private static final char last2byte = (char) Integer
			.parseInt("00000011", 2);
	private static final char last4byte = (char) Integer
			.parseInt("00001111", 2);
	private static final char last6byte = (char) Integer
			.parseInt("00111111", 2);

	private String private_key;
	private char[] private_base64_table;
	private char[] mBase64Table;
	private int offset = 0;

	public EncryptBase64() {
		mBase64Table = base64_table;
	}

	public EncryptBase64(String key) {
		private_key = key;
		initPrivateTable();
	}

	public String encode(byte[] contents) {
		if (contents == null || contents.length == 0)
			return null;
		StringBuilder _sb = new StringBuilder((contents.length + 2) / 3 * 4);
		int b1, b2, b3;
		int i = 0;
		int len = contents.length;
		while (i < len) {
			b1 = contents[i++] & 0xFF;
			if (i == len) {
				_sb.append(mBase64Table[b1 >>> 2]);
				_sb.append(mBase64Table[(b1 & last2byte) << 4]);
				_sb.append("==");
				break;
			}
			b2 = contents[i++] & 0xFF;
			if (i == len) {
				_sb.append(mBase64Table[b1 >>> 2]);
				_sb.append(mBase64Table[((b1 & last2byte) << 4) | (b2 >>> 4)]);
				_sb.append(mBase64Table[(b2 & last4byte) << 2]);
				_sb.append("=");
				break;
			}
			b3 = contents[i++] & 0xFF;
			_sb.append(mBase64Table[b1 >>> 2]);
			_sb.append(mBase64Table[((b1 & last2byte) << 4) | (b2 >>> 4)]);
			_sb.append(mBase64Table[((b2 & last4byte) << 2) | (b3 >>> 6)]);
			_sb.append(mBase64Table[b3 & last6byte]);

		}
		return _sb.toString();
	}

	public String decode(byte[] data, String charset) {
		int len = data.length;
		StringBuilder _sb = new StringBuilder(len * 3 / 4);
		int i = 0;		
		int[] b = new int[4];		
		while (i < len) {
			for(int j = 0; j < 4; ++j) {
				b[j] = base64_to_256(data[i++]);
				if(b[j] == 0x40)
					continue;				
			}
			_sb.append((char) (b[0] << 2 | b[1] >>> 4));
			if(b[2] != 0x40) {
				_sb.append((char) ((b[1] & last4byte) << 4 | b[2] >>> 2));
			}
			if(b[3] != 0x40) {
				_sb.append((char) ((b[2] & last2byte) << 6 | b[3]));
			}
		}
		return new String(
				_sb.toString().getBytes(Charset.forName("ISO8859-1")),
				Charset.forName(charset));
	}
	
	public byte[] decode(byte[] data, int len) {
		StringBuilder _sb = new StringBuilder(len * 3 / 4);
		int i = 0;		
		int[] b = new int[4];
		while (i < len) {
			for(int j = 0; j < 4; ++j) {
				b[j] = base64_to_256(data[i++]);
				if(b[j] == 0x40)
					continue;				
			}
			_sb.append((char) (b[0] << 2 | b[1] >>> 4));
			if(b[2] != 0x40) {
				_sb.append((char) ((b[1] & last4byte) << 4 | b[2] >>> 2));
			}
			if(b[3] != 0x40) {
				_sb.append((char) ((b[2] & last2byte) << 6 | b[3]));
			}
		}
		return _sb.toString().getBytes(Charset.forName("ISO8859-1"));
	}

	private void initPrivateTable() {
		private_base64_table = new char[base64_table.length];
		offset = (int) private_key.charAt(0) % 13;
		for (int i = 0; i < base64_table.length; ++i) {
			private_base64_table[i] = base64_table[(i + offset)
					% base64_table.length];
		}
		mBase64Table = private_base64_table;
	}

	private int base64_to_256(byte base64) {
		if (base64 >= 'A' && base64 <= 'Z') {
			return (base64 - 'A' + (base64_table.length - offset))
					% base64_table.length;
		} else if (base64 >= 'a' && base64 <= 'z') {
			return (base64 - 'a' + 26 + (base64_table.length - offset))
					% base64_table.length;
		} else if (base64 >= '0' && base64 <= '9') {
			return (base64 - '0' + 52 + (base64_table.length - offset))
					% base64_table.length;
		} else if (base64 == '+') {
			return (62 + (base64_table.length - offset)) % base64_table.length;
		} else if (base64 == '/') {
			return (63 + (base64_table.length - offset)) % base64_table.length;
		}
		return 0x40;
	}
}
