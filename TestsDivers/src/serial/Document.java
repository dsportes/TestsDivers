package serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import serial.DocumentMeta.DMException;

public abstract class Document {

	public static Class<?> forName(String name) throws DMException {
		return DocumentMeta.forName(name);
	}
	
	public static void register(Class<?>... documentClasses) throws DMException{
		DocumentMeta.register(documentClasses);
	}
	public static Document newDocument(Class<?> clazz, String id) throws DMException {
		return DocumentMeta.newDocument(clazz, id);
	}
	public static Document newDocument(String jsonAll, boolean full) throws Exception {
		return DocumentMeta.newDocument(jsonAll, full);
	}

	transient DocumentMeta _meta;
	public Document.Item getItem(Class<?> clazz, String id) throws Exception {
		return _meta.getItem(clazz, id);
	}
	public Set<String> getIds(Class<?> clazz) {
		return _meta.getIds(clazz);
	}
	public String serialize(long vmin, long stamp){
		return _meta.serialize(vmin, stamp);
	}
	public long version() { return _meta.version(); }
	public long stamp() { return _meta.stamp(); }
	public String id() { return _meta.id(); }

	public static abstract class Item {
		transient DocumentMeta.ItemMeta _meta;
		public void save(){	_meta.save(); }
		public void deleted(){	_meta.delete(); }
		public DocumentMeta.Status modified() { return _meta.status(); }
		public long version() { return _meta.version(); }
		public String id() { return _meta.id(); }
	}
	
	public static byte[] Gzip(byte[] bytes) {
		try {
			int max = 100;
			byte[] bytes2 = null;
			long t5 = System.currentTimeMillis();
			for(int i = 0; i < max; i++) {
				ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
				GZIPOutputStream zos = new GZIPOutputStream(bos2, 128 * 1024);
				zos.write(bytes);
				zos.close();
				bytes2 = bos2.toByteArray();
			}
			long t6 = System.currentTimeMillis();
			System.out.println("GZIP : " + (t6 - t5) + "ms. " + bytes.length + " / " + bytes2.length);
			return bytes2;
		} catch(Exception e){
			return new byte[0];
		}
	}

	public static byte[] Gunzip(byte[] bytes) {
		try {
			int max = 100;
			byte[] bytes2 = null;
			byte[] buf = new byte[4096];
			long t5 = System.currentTimeMillis();
			for(int i = 0; i < max; i++) {
				ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
				ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				GZIPInputStream gzis = new GZIPInputStream(bis, 128 * 1024);
				int len;
		        while ((len = gzis.read(buf)) > 0) {
		        	bos2.write(buf, 0, len);
		        }
				gzis.close();
				bytes2 = bos2.toByteArray();
			}
			long t6 = System.currentTimeMillis();
			System.out.println("GUNZIP : " + (t6 - t5) + "ms. " + bytes.length + " / " + bytes2.length);
			return bytes2;
		} catch(Exception e){
			return new byte[0];
		}
	}

}
